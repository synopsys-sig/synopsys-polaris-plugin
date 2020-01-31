package com.synopsys.integration.jenkins.polaris.extensions.pipeline;

import java.io.File;
import java.util.Optional;

import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;
import com.synopsys.integration.stepworkflow.StepWorkflow;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import com.synopsys.integration.stepworkflow.SubStep;
import com.synopsys.integration.stepworkflow.StepWorkflowResponse;

@PowerMockIgnore({"javax.crypto.*", "javax.net.ssl.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({PolarisCli.class, StepWorkflow.class})
public class ExecutePolarisCliStepTest {
    private static final String WORKSPACE_REL_PATH = "out/test/PolarisBuildStepTest/testPerform/workspace";
    private static final String TEST_POLARIS_CLI_NAME = "testPolarisCliName";
    private static final String TEST_POLARIS_HOME = "/tmp/polaris";

    // TODO: To improve this test (to test more of PolarisBuildStep.perform(), we could refactor PolarisBuildStep to:
    // - Separate object creation (CreatePolarisEnvironment, GetPathToPolarisCli, ExecutePolarisCli) out of ExecutePolarisCliStep,
    //   so the objects can be mocked in this test.
    //   Then we can verify that they are created correctly, and verify that StepWorkflow.first().then().then().run().getDataOrThrowException() was all
    //   done correctly (verify the arguments passed).
    @Test
    public void test() throws Exception {
        final ExecutePolarisCliStep executePolarisCliStep = new ExecutePolarisCliStep("testArgs");
        executePolarisCliStep.setPolarisCli(TEST_POLARIS_CLI_NAME);

        final StepContext stepContext = Mockito.mock(StepContext.class);
        // node = context.get(Node.class);
        final Node node = Mockito.mock(Node.class);
        Mockito.when(stepContext.get(Node.class)).thenReturn(node);
        // envVars = context.get(EnvVars.class);
        final EnvVars envVars = Mockito.mock(EnvVars.class);
        Mockito.when(stepContext.get(EnvVars.class)).thenReturn(envVars);

        final TaskListener listener = Mockito.mock(TaskListener.class);
        // listener = context.get(TaskListener.class);
        Mockito.when(stepContext.get(TaskListener.class)).thenReturn(listener);
        final Launcher launcher = Mockito.mock(Launcher.class);
        final VirtualChannel virtualChannel = Mockito.mock(VirtualChannel.class);
        Mockito.when(launcher.getChannel()).thenReturn(virtualChannel);
        Mockito.when(stepContext.get(Launcher.class)).thenReturn(launcher);

        // workspace = context.get(FilePath.class);
        final FilePath workspaceFilePath = new FilePath(new File(WORKSPACE_REL_PATH));
        Mockito.when(stepContext.get(FilePath.class)).thenReturn(workspaceFilePath);
        final ExecutePolarisCliStep.Execution stepExecution = (ExecutePolarisCliStep.Execution) executePolarisCliStep.start(stepContext);

        // PolarisCli polarisCli = PolarisCli.findInstanceWithName(ExecutePolarisCliStep.this.polarisCli)
        final PolarisCli polarisCli = PowerMockito.mock(PolarisCli.class);
        PowerMockito.mockStatic(PolarisCli.class);
        Mockito.when(PolarisCli.findInstanceWithName(TEST_POLARIS_CLI_NAME)).thenReturn(Optional.of(polarisCli));

        // polarisCli = polarisCli.forEnvironment(envVars);
        Mockito.when(polarisCli.forEnvironment(envVars)).thenReturn(polarisCli);
        Mockito.when(polarisCli.forNode(node, listener)).thenReturn(polarisCli);
        Mockito.when(polarisCli.getHome()).thenReturn(TEST_POLARIS_HOME);

        PowerMockito.mockStatic(StepWorkflow.class);
        final StepWorkflow.Builder stepWorkflowBuilder = Mockito.mock(StepWorkflow.Builder.class);
        Mockito.when(StepWorkflow.first(Mockito.any(SubStep.class))).thenReturn(stepWorkflowBuilder);
        Mockito.when(stepWorkflowBuilder.then(Mockito.any(SubStep.class))).thenReturn(stepWorkflowBuilder);

        final StepWorkflowResponse stepWorkflowResponse = Mockito.mock(StepWorkflowResponse.class);
        Mockito.when(stepWorkflowBuilder.run()).thenReturn(stepWorkflowResponse);
        Mockito.when(stepWorkflowResponse.getDataOrThrowException()).thenReturn(123);

        final Integer result = stepExecution.run();

        assertEquals(Integer.valueOf(123), result);
    }
}
