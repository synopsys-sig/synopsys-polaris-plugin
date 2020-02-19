package com.synopsys.integration.jenkins.polaris.extensions.buildstep;

import java.io.IOException;
import java.util.function.Predicate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.synopsys.integration.function.ThrowingConsumer;
import com.synopsys.integration.function.ThrowingFunction;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.polaris.workflow.CreatePolarisEnvironment;
import com.synopsys.integration.jenkins.polaris.workflow.ExecutePolarisCli;
import com.synopsys.integration.jenkins.polaris.workflow.GetTotalIssueCount;
import com.synopsys.integration.jenkins.polaris.workflow.PolarisWorkflowStepFactory;
import com.synopsys.integration.stepworkflow.StepWorkflow;
import com.synopsys.integration.stepworkflow.StepWorkflowResponse;
import com.synopsys.integration.stepworkflow.SubStep;
import com.synopsys.integration.stepworkflow.jenkins.RemoteSubStep;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Node;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ StepWorkflow.class })
public class PolarisBuildStepWorkflowTest {

    @Test
    public void test() throws IOException, InterruptedException {

        // Mock the arguments passed to the PolarisBuildStepWorkflow constructor
        final WaitForIssues waitForIssues = Mockito.mock(WaitForIssues.class);
        final PolarisWorkflowStepFactory polarisWorkflowStepFactory = Mockito.mock(PolarisWorkflowStepFactory.class);
        final AbstractBuild<?, ?> build = Mockito.mock(AbstractBuild.class);

        // Mock the objects created by those mocks
        final FilePath workspace = Mockito.mock(FilePath.class);
        Mockito.when(build.getWorkspace()).thenReturn(workspace);
        final Node node = Mockito.mock(Node.class);
        Mockito.when(build.getBuiltOn()).thenReturn(node);
        final JenkinsIntLogger logger = Mockito.mock(JenkinsIntLogger.class);
        Mockito.when(polarisWorkflowStepFactory.getOrCreateLogger()).thenReturn(logger);
        final CreatePolarisEnvironment createPolarisEnvironment = Mockito.mock(CreatePolarisEnvironment.class);
        Mockito.when(polarisWorkflowStepFactory.createStepCreatePolarisEnvironment()).thenReturn(createPolarisEnvironment);
        final ExecutePolarisCli executePolarisCli = Mockito.mock(ExecutePolarisCli.class);
        Mockito.when(polarisWorkflowStepFactory.createStepExecutePolarisCli()).thenReturn(executePolarisCli);
        final RemoteSubStep<String> findPolarisCli = Mockito.mock(RemoteSubStep.class);
        Mockito.when(polarisWorkflowStepFactory.createStepFindPolarisCli()).thenReturn(findPolarisCli);
        final RemoteSubStep<String> getPolarisCliResponseContent = Mockito.mock(RemoteSubStep.class);
        Mockito.when(polarisWorkflowStepFactory.createStepGetPolarisCliResponseContent()).thenReturn(getPolarisCliResponseContent);
        final GetTotalIssueCount getTotalIssueCount = Mockito.mock(GetTotalIssueCount.class);
        Mockito.when(polarisWorkflowStepFactory.createStepGetTotalIssueCount()).thenReturn(getTotalIssueCount);
        final SubStep<Integer, Object> responseHandler = Mockito.mock(SubStep.class);
        Mockito.when(polarisWorkflowStepFactory.createStepWithConsumer(Mockito.any(ThrowingConsumer.class))).thenReturn(responseHandler);

        // Mock the StepWorkflow and the objects it creates
        // This requires that the first workflow step = createPolarisEnvironment
        PowerMockito.mockStatic(StepWorkflow.class);
        final StepWorkflow.Builder workflowBuilder = Mockito.mock(StepWorkflow.Builder.class);
        Mockito.when(StepWorkflow.first(createPolarisEnvironment)).thenReturn(workflowBuilder);

        // This should let the creation of almost any workflow run; creation of specific steps are verified after the test
        Mockito.when(workflowBuilder.then(Mockito.any(SubStep.class))).thenReturn(workflowBuilder);
        final StepWorkflow.ConditionalBuilder conditionalBuilder = Mockito.mock(StepWorkflow.ConditionalBuilder.class);
        Mockito.when(workflowBuilder.andSometimes(Mockito.any(SubStep.class))).thenReturn(conditionalBuilder);
        Mockito.when(conditionalBuilder.then(Mockito.any(SubStep.class))).thenReturn(conditionalBuilder);
        Mockito.when(conditionalBuilder.butOnlyIf(Mockito.any(WaitForIssues.class), Mockito.any(Predicate.class))).thenReturn(workflowBuilder);
        final StepWorkflowResponse<Object> response = Mockito.mock(StepWorkflowResponse.class);
        Mockito.when(workflowBuilder.run()).thenReturn(response);
        Mockito.when(response.handleResponse(Mockito.any(ThrowingFunction.class))).thenReturn(Boolean.TRUE);

        // Test
        final PolarisBuildStepWorkflow polarisBuildStepWorkflow = new PolarisBuildStepWorkflow(waitForIssues, polarisWorkflowStepFactory, build);
        polarisBuildStepWorkflow.perform();

        // Verify that the expected steps got added to workflow
        Mockito.verify(workflowBuilder).then(findPolarisCli);
        Mockito.verify(workflowBuilder).then(executePolarisCli);
        Mockito.verify(workflowBuilder).andSometimes(getPolarisCliResponseContent);
        Mockito.verify(conditionalBuilder).then(getTotalIssueCount);
        Mockito.verify(conditionalBuilder).then(responseHandler);
        Mockito.verify(conditionalBuilder).butOnlyIf(Mockito.eq(waitForIssues), Mockito.any(Predicate.class));
    }
}
