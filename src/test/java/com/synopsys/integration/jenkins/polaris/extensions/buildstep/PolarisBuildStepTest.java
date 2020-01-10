package com.synopsys.integration.jenkins.polaris.extensions.buildstep;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.JDK;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

@PowerMockIgnore({"javax.crypto.*", "javax.net.ssl.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({AbstractBuild.class, JDK.class, Launcher.class, Launcher.ProcStarter.class, PolarisCli.class})
public class PolarisBuildStepTest {
    private static final String POLARIS_ARGUMENTS = "--detect.docker.passthrough.service.timeout=240000 --detect.cleanup=false --detect.source.path=$JAVA_HOME --detect.project.name=\"Test Project'\"";
    private static final String WORKSPACE_REL_PATH = "out/test/PolarisBuildStepTest/testPerform/workspace";
    private static final String javaHomePath = System.getenv("JAVA_HOME");

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void test() throws IOException, InterruptedException {
        final AbstractBuild<FreeStyleProject, FreeStyleBuild> build = PowerMockito.mock(AbstractBuild.class);
        final Launcher launcher = PowerMockito.mock(Launcher.class);
        final BuildListener buildListener = PowerMockito.mock(BuildListener.class);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(baos, true, "UTF-8");
        Mockito.when(buildListener.getLogger()).thenReturn(printStream);

        final FilePath workspaceFilePath = new FilePath(new File(WORKSPACE_REL_PATH));
        Mockito.when(build.getWorkspace()).thenReturn(workspaceFilePath);

        final Node node = Mockito.mock(Node.class);
        Mockito.when(build.getBuiltOn()).thenReturn(node);

        final EnvVars envVars = Mockito.mock(EnvVars.class);
        Mockito.when(build.getEnvironment(buildListener)).thenReturn(envVars);

        final FreeStyleProject project = PowerMockito.mock(FreeStyleProject.class);
        final JDK jdk = PowerMockito.mock(JDK.class);
        Mockito.when(build.getProject()).thenReturn(project);
        Mockito.when(project.getJDK()).thenReturn(jdk);
        Mockito.when(jdk.forNode(Mockito.any(Node.class), Mockito.any(TaskListener.class))).thenReturn(jdk);
        Mockito.when(jdk.getHome()).thenReturn("/tmp/jdk");

        final VirtualChannel channel = PowerMockito.mock(hudson.remoting.VirtualChannel.class);
        Mockito.when(launcher.getChannel()).thenReturn(channel);

        PowerMockito.mockStatic(PolarisCli.class);
        final PolarisCli polarisCli = PowerMockito.mock(PolarisCli.class);
        Mockito.when(PolarisCli.findInstanceWithName("testPolarisCliName")).thenReturn(Optional.of(polarisCli));

        // polarisCli.forEnvironment(envVars);
        Mockito.when(polarisCli.forEnvironment(Mockito.any(EnvVars.class))).thenReturn(polarisCli);
        // polarisCli.forNode(node, listener);
        Mockito.when(polarisCli.forNode(Mockito.any(Node.class), Mockito.any(TaskListener.class))).thenReturn(polarisCli);

        final PolarisBuildStep polarisBuildStep = new PolarisBuildStep("testPolarisCliName", POLARIS_ARGUMENTS);
        polarisBuildStep.perform(build, launcher, buildListener);
    }

}
