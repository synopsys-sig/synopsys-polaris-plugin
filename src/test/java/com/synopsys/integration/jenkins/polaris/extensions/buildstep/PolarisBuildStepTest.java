package com.synopsys.integration.jenkins.polaris.extensions.buildstep;

import java.io.IOException;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import com.synopsys.integration.jenkins.polaris.extensions.global.PolarisGlobalConfig;
import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.service.PolarisService;
import com.synopsys.integration.polaris.common.service.PolarisServicesFactory;
import com.synopsys.integration.stepworkflow.StepWorkflowBuilder;

import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.JDK;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.model.GlobalConfiguration;

public class PolarisBuildStepTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void test() throws IOException, InterruptedException {
        final AbstractBuild<?, ?> build = Mockito.mock(AbstractBuild.class);
        final Launcher launcher = Mockito.mock(Launcher.class);
        final BuildListener listener = Mockito.mock(BuildListener.class);

        // build.getBuiltOn(), build.getWorkspace(), build.getEnvironment(listener)
        final Node node = Mockito.mock(Node.class);
        Mockito.when(build.getBuiltOn()).thenReturn(node);

        final FilePath workspace = Mockito.mock(FilePath.class);
        Mockito.when(build.getWorkspace()).thenReturn(workspace);
        final EnvVars envVars = new EnvVars("TEST_KEY", "test value");
        Mockito.when(build.getEnvironment(listener)).thenReturn(envVars);

        //////////final EnvVars envVars = Mockito.mock(EnvVars.class);
        final BuildListener buildListener = Mockito.mock(BuildListener.class);
        Mockito.when(build.getEnvironment(buildListener)).thenReturn(envVars);

        final FreeStyleProject project = Mockito.mock(FreeStyleProject.class);
        final JDK jdk = PowerMockito.mock(JDK.class);
        //////////Mockito.when(build.getProject()).thenReturn(project);
        Mockito.when(project.getJDK()).thenReturn(jdk);
        Mockito.when(jdk.forNode(Mockito.any(Node.class), Mockito.any(TaskListener.class))).thenReturn(jdk);
        Mockito.when(jdk.getHome()).thenReturn("/tmp/jdk");

        final VirtualChannel channel = Mockito.mock(hudson.remoting.VirtualChannel.class);
        Mockito.when(launcher.getChannel()).thenReturn(channel);

        final PolarisCli polarisCli = PowerMockito.mock(PolarisCli.class);
        Mockito.when(polarisCli.getHome()).thenReturn("polarisCliHome");
        Mockito.when(polarisCli.forEnvironment(Mockito.any(EnvVars.class))).thenReturn(polarisCli);
        Mockito.when(polarisCli.forNode(Mockito.any(Node.class), Mockito.any(TaskListener.class))).thenReturn(polarisCli);

        PowerMockito.mockStatic(PolarisCli.class);
        Mockito.when(PolarisCli.findInstallationWithName("testPolarisCliName")).thenReturn(Optional.of(polarisCli));
        Mockito.when(PolarisCli.installationsExist()).thenReturn(true);

        final PolarisGlobalConfig polarisGlobalConfig = Mockito.mock(PolarisGlobalConfig.class);
        final ExtensionList extensionList = Mockito.mock(ExtensionList.class);
        PowerMockito.mockStatic(GlobalConfiguration.class);
        Mockito.when(GlobalConfiguration.all()).thenReturn(extensionList);
        Mockito.when(extensionList.get(PolarisGlobalConfig.class)).thenReturn(polarisGlobalConfig);

        final PolarisServerConfig polarisServerConfig = Mockito.mock(PolarisServerConfig.class);
        Mockito.when(polarisGlobalConfig.getPolarisServerConfig()).thenReturn(polarisServerConfig);

        final PolarisServicesFactory polarisServicesFactory = Mockito.mock(PolarisServicesFactory.class);
        Mockito.when(polarisServerConfig.createPolarisServicesFactory(Mockito.any(IntLogger.class))).thenReturn(polarisServicesFactory);

        final PolarisService polarisService = Mockito.mock(PolarisService.class);
        Mockito.when(polarisServicesFactory.createPolarisService()).thenReturn(polarisService);

        final StepWorkflowBuilder stepWorkflowBuilder = Mockito.mock(StepWorkflowBuilder.class);
//        PowerMockito.mockStatic(StepWorkflow.class);
//        Mockito.when(StepWorkflow.first(Mockito.any(SubStep.class))).thenReturn(stepWorkflowBuilder);
//        Mockito.when(stepWorkflowBuilder.then(Mockito.any(SubStep.class))).thenReturn(stepWorkflowBuilder);
//
//        final StepWorkflow.ConditionalBuilder conditionalBuilder = Mockito.mock(StepWorkflow.ConditionalBuilder.class);
//        Mockito.when(stepWorkflowBuilder.andSometimes(Mockito.any(SubStep.class))).thenReturn(conditionalBuilder);
//        Mockito.when(conditionalBuilder.then(Mockito.any(SubStep.class))).thenReturn(conditionalBuilder);
//        Mockito.when(conditionalBuilder.butOnlyIf(Mockito.any(Object.class), Mockito.any(Predicate.class))).thenReturn(stepWorkflowBuilder);
//
//        final StepWorkflowResponse stepWorkflowResponse = Mockito.mock(StepWorkflowResponse.class);
//        Mockito.when(stepWorkflowBuilder.run()).thenReturn(stepWorkflowResponse);
//        Mockito.when(stepWorkflowResponse.handleResponse(Mockito.any(ThrowingFunction.class))).thenReturn(true);

        final WaitForIssues waitForIssues = Mockito.mock(WaitForIssues.class);

        final PolarisBuildStep polarisBuildStep = new PolarisBuildStep();
        polarisBuildStep.setPolarisCliName("testpolariscli");
        polarisBuildStep.setPolarisArguments("test polaris cli arguments");
        polarisBuildStep.setWaitForIssues(waitForIssues);

        polarisBuildStep.perform(build, launcher, listener);
    }
}
