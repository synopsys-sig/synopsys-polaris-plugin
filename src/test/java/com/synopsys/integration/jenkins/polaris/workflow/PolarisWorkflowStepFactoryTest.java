package com.synopsys.integration.jenkins.polaris.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.synopsys.integration.builder.BuilderPropertyKey;
import com.synopsys.integration.jenkins.JenkinsVersionHelper;
import com.synopsys.integration.jenkins.polaris.extensions.global.PolarisGlobalConfig;
import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;
import com.synopsys.integration.stepworkflow.SubStepResponse;
import com.synopsys.integration.stepworkflow.jenkins.RemoteSubStep;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.ArgumentListBuilder;
import jenkins.model.GlobalConfiguration;

// TODO Not clear writing tests for the factory is a good use of time.
// Challenges to testing the factory: Because it's a factory, it's hard to verify what it does.
// And the objects it creates are hard to interrogate.
// Testing the run methods is much better done by unit tests, so it's kinda dumb.
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GlobalConfiguration.class, PolarisCli.class, PolarisWorkflowStepFactory.class })
public class PolarisWorkflowStepFactoryTest {

    private static final String POLARIS_CLI_NAME = "testpolariscli";
    private static final String POLARIS_ARGUMENTS = "test polaris arguments";
    public static final String POLARIS_CLI_HOME = "/polaris/cli/home";
    private static PolarisWorkflowStepFactory factory;
    private static ByteArrayOutputStream logOutputStream;
    private static Map<String, String> testEnvVarsMap;
    private static EnvVars envVars;
    private static Node node;
    private static TaskListener listener;
    private static Launcher launcher;
    private static VirtualChannel channel;
    private static final String WORKSPACE_REMOTE = "test workspace remoate";
    private static FilePath workspace;
    private static SubStepResponse<String> successfulResponseWithCliHome;

    @BeforeClass
    public static void setup() throws Exception {

        node = Mockito.mock(Node.class);
        workspace = Mockito.mock(FilePath.class);
        Mockito.when(workspace.getRemote()).thenReturn(WORKSPACE_REMOTE);
        testEnvVarsMap = new HashMap<>();
        testEnvVarsMap.put("envvarkey", "env var value");
        envVars = new EnvVars(testEnvVarsMap);
        launcher = Mockito.mock(Launcher.class);
        channel = Mockito.mock(VirtualChannel.class);
        Mockito.when(launcher.getChannel()).thenReturn(channel);
        listener = Mockito.mock(TaskListener.class);

        logOutputStream = new ByteArrayOutputStream();
        final PrintStream logPrintStream = new PrintStream(logOutputStream);
        Mockito.when(listener.getLogger()).thenReturn(logPrintStream);

        // new JenkinsVersionHelper()
        final JenkinsVersionHelper jenkinsVersionHelper = Mockito.mock(JenkinsVersionHelper.class);
        Mockito.when(jenkinsVersionHelper.getPluginVersion("synopsys-polaris")).thenReturn("1.2.3");
        PowerMockito.whenNew(JenkinsVersionHelper.class)
            .withNoArguments()
            .thenReturn(jenkinsVersionHelper);

        factory = new PolarisWorkflowStepFactory(node, workspace, envVars, launcher, listener);

        successfulResponseWithCliHome = Mockito.mock(SubStepResponse.class);
        Mockito.when(successfulResponseWithCliHome.isFailure()).thenReturn(false);
        Mockito.when(successfulResponseWithCliHome.isSuccess()).thenReturn(true);
        Mockito.when(successfulResponseWithCliHome.hasData()).thenReturn(true);
        Mockito.when(successfulResponseWithCliHome.getData()).thenReturn(POLARIS_CLI_HOME);
    }

    // TODO TEMP ignored
    @Ignore
    @Test
    public void testCreateStepCreatePolarisEnvironment() throws Exception {

        // Test factory method
        final CreatePolarisEnvironment createPolarisEnvironment = factory.createStepCreatePolarisEnvironment();

        // Setup to test factory-created object
        PowerMockito.mockStatic(GlobalConfiguration.class);
        final ExtensionList<GlobalConfiguration> registeredDescriptors = Mockito.mock(ExtensionList.class);
        Mockito.when(GlobalConfiguration.all()).thenReturn(registeredDescriptors);
        final PolarisGlobalConfig polarisGlobalConfig = Mockito.mock(PolarisGlobalConfig.class);
        Mockito.when(registeredDescriptors.get(PolarisGlobalConfig.class)).thenReturn(polarisGlobalConfig);
        final PolarisServerConfigBuilder polarisServerConfigBuilder = Mockito.mock(PolarisServerConfigBuilder.class);
        Mockito.when(polarisGlobalConfig.getPolarisServerConfigBuilder()).thenReturn(polarisServerConfigBuilder);
        final BuilderPropertyKey builderPropertyKey = new BuilderPropertyKey("globalconfigkey");
        final Map<BuilderPropertyKey, String> builderProperties = new HashMap<>();
        builderProperties.put(builderPropertyKey, "global config value");
        Mockito.when(polarisServerConfigBuilder.getProperties()).thenReturn(builderProperties);
        final PolarisServerConfig polarisServerConfig = Mockito.mock(PolarisServerConfig.class);
        Mockito.when(polarisServerConfigBuilder.build()).thenReturn(polarisServerConfig);

        // Test factory-created object
        final SubStepResponse<Object> response =  createPolarisEnvironment.run();

        // verify the results from factory-created object
        assertTrue(response.isSuccess());
        final String logContents = logOutputStream.toString();
        assertTrue(logContents.contains("Synopsys Polaris for Jenkins"));
        assertTrue(logContents.contains("1.2.3"));
    }

    @Test
    public void testCreateStepFindPolarisCli() throws Exception {

        // TODO should some/all of this mocking be moved to setup()?
        // (Some of this may break the other test.)
//        final VirtualChannel channel = Mockito.mock(VirtualChannel.class);
//        Mockito.when(launcher.getChannel()).thenReturn(channel);
        PowerMockito.mockStatic(PolarisCli.class);
        final PolarisCli polarisCli = Mockito.mock(PolarisCli.class);
        Mockito.when(PolarisCli.findInstanceWithName(POLARIS_CLI_NAME)).thenReturn(Optional.of(polarisCli));
        Mockito.when(polarisCli.getHome()).thenReturn(POLARIS_CLI_HOME);
        Mockito.when(polarisCli.forEnvironment(envVars)).thenReturn(polarisCli);
        Mockito.when(polarisCli.forNode(node, listener)).thenReturn(polarisCli);

        final GetPathToPolarisCli getPathToPolarisCli = Mockito.mock(GetPathToPolarisCli.class);
        PowerMockito.whenNew(GetPathToPolarisCli.class)
            .withArguments(POLARIS_CLI_HOME)
            .thenReturn(getPathToPolarisCli);

        final RemoteSubStep<String> remoteSubStep = Mockito.mock(RemoteSubStep.class);
        PowerMockito.whenNew(RemoteSubStep.class)
            .withArguments(channel, getPathToPolarisCli)
            .thenReturn(remoteSubStep);

        // Test factory method
        final RemoteSubStep<String> createPolarisEnvironment = factory.createStepFindPolarisCli(POLARIS_CLI_NAME);

        assertEquals(remoteSubStep, createPolarisEnvironment);
    }

    @Test
    public void testCreateStepExecutePolarisCli() throws IOException, InterruptedException {

        // Test factory method
        final ExecutePolarisCli executePolarisCli = factory.createStepExecutePolarisCli(POLARIS_ARGUMENTS);

        final ArgumentListBuilder argumentListBuilder = new ArgumentListBuilder();
        argumentListBuilder.add(POLARIS_CLI_HOME);
        argumentListBuilder.addTokenized(POLARIS_ARGUMENTS);

        final Launcher.ProcStarter procStarter = Mockito.mock(Launcher.ProcStarter.class);
        Mockito.when(launcher.launch()).thenReturn(procStarter);
        Mockito.when(procStarter.cmds(Mockito.argThat(new ArgumentListBuilderMatcher(argumentListBuilder)))).thenReturn(procStarter);
        Mockito.when(procStarter.envs(testEnvVarsMap)).thenReturn(procStarter);
        Mockito.when(procStarter.pwd(workspace)).thenReturn(procStarter);
        Mockito.when(procStarter.stdout(listener)).thenReturn(procStarter);
        Mockito.when(procStarter.quiet(true)).thenReturn(procStarter);
        Mockito.when(procStarter.join()).thenReturn(0);
        final SubStepResponse<Integer> response = executePolarisCli.run(successfulResponseWithCliHome);

        assertTrue(response.isSuccess());
        assertEquals(0L, (long) response.getData());

        Mockito.verify(launcher).launch();
    }

    @Test
    public void testCreateStepGetPolarisCliResponseContent() {

        // Test factory method
        final RemoteSubStep<String> getPolarisCliResponseContent = factory.createStepGetPolarisCliResponseContent();

        final SubStepResponse<String> resp = getPolarisCliResponseContent.run(successfulResponseWithCliHome);
        assertTrue(resp.isSuccess());
    }

    // TODO temp ignored
    @Ignore
    @Test
    public void testCreateStepGetTotalIssueCount() throws AbortException {

        // createStepGetTotalIssueCount
        // Test factory method
        final GetTotalIssueCount getTotalIssueCount = factory.createStepGetTotalIssueCount(0);

        // Test the object created
        getTotalIssueCount.run(successfulResponseWithCliHome);

    }
    // TODO There are a lot more methods to test

    private class ArgumentListBuilderMatcher implements ArgumentMatcher<ArgumentListBuilder> {
        private final ArgumentListBuilder left;

        public ArgumentListBuilderMatcher(final ArgumentListBuilder left) {
            this.left = left;
        }

        @Override
        public boolean matches(final ArgumentListBuilder right) {
            return left.toList().equals(right.toList());
        }
    }



    private void youllNeedThisLater() throws Exception {
        // You'll need this code when testing createStepGetPolarisCliResponseContent

        // new GetPolarisCliResponseContent(workspace.getRemote());
        final GetPolarisCliResponseContent getPolarisCliResponseContent = Mockito.mock(GetPolarisCliResponseContent.class);
        PowerMockito.whenNew(GetPolarisCliResponseContent.class)
            .withArguments(WORKSPACE_REMOTE)
            .thenReturn(getPolarisCliResponseContent);

        // new RemoteSubStep<>(launcher.getChannel(), getPolarisCliResponseContent);
        final RemoteSubStep<String> remoteSubStep = Mockito.mock(RemoteSubStep.class);
        PowerMockito.whenNew(RemoteSubStep.class)
            .withArguments(channel, getPolarisCliResponseContent)
            .thenReturn(remoteSubStep);

        // test

        assertEquals(remoteSubStep, "the object returned by code under test");
    }


}
