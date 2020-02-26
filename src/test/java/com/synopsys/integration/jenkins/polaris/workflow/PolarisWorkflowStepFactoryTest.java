package com.synopsys.integration.jenkins.polaris.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.model.GlobalConfiguration;

// TODO The ability to test the factory is limited by the static methods it uses.
// PowerMock's solution for static methods is pretty ugly.
// Next step: reduce the use of static methods in the factory.
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GlobalConfiguration.class, PolarisCli.class, PolarisWorkflowStepFactory.class })
public class PolarisWorkflowStepFactoryTest {

    private static final String POLARIS_CLI_NAME = "testpolariscli";
    private static final String POLARIS_ARGUMENTS = "test polaris arguments";
    public static final String POLARIS_CLI_HOME = "testhome";
    private static PolarisWorkflowStepFactory factory;
    private static ByteArrayOutputStream logOutputStream;
    private static EnvVars envVars;
    private static Node node;
    private static TaskListener listener;
    private static Launcher launcher;
    private static VirtualChannel channel;
    private static final String WORKSPACE_REMOTE = "test workspace remoate";

    @BeforeClass
    public static void setup() throws Exception {

        node = Mockito.mock(Node.class);
        final FilePath workspace = Mockito.mock(FilePath.class);
        Mockito.when(workspace.getRemote()).thenReturn(WORKSPACE_REMOTE);
        final Map<String, String> testEnvVarsMap = new HashMap<>();
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

        factory = new PolarisWorkflowStepFactory(POLARIS_CLI_NAME, POLARIS_ARGUMENTS, node, workspace, envVars, launcher, listener);
    }

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
//        PowerMockito.mockStatic(JenkinsVersionHelper.class);
//        Mockito.when(JenkinsVersionHelper.getPluginVersion("synopsys-polaris")).thenReturn("1.2.3");

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
        final VirtualChannel channel = Mockito.mock(VirtualChannel.class);
        Mockito.when(launcher.getChannel()).thenReturn(channel);
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
        final RemoteSubStep<String> createPolarisEnvironment = factory.createStepFindPolarisCli();

        assertEquals(remoteSubStep, createPolarisEnvironment);
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
