package com.synopsys.integration.jenkins.polaris.extensions.buildstep;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Node;

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

        final WaitForIssues waitForIssues = Mockito.mock(WaitForIssues.class);

        final PolarisBuildStep polarisBuildStep = new PolarisBuildStep();
        polarisBuildStep.setPolarisCliName("testpolariscli");
        polarisBuildStep.setPolarisArguments("test polaris cli arguments");
        polarisBuildStep.setWaitForIssues(waitForIssues);

        //polarisBuildStep.perform(build, launcher, listener);
    }
}
