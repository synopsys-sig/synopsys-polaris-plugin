package com.synopsys.integration.jenkins.polaris.extensions.buildstep;

import java.io.File;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.synopsys.integration.jenkins.extensions.ChangeBuildStatusTo;
import com.synopsys.integration.jenkins.polaris.workflow.PolarisWorkflowStepFactory;
import com.synopsys.integration.stepworkflow.StepWorkflow;
import com.synopsys.integration.stepworkflow.StepWorkflowResponse;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Node;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AbstractBuild.class, StepWorkflow.class })
public class PolarisBuildStepWorkflowTest {
    private StepWorkflowResponse stepWorkflowResponse;
    private PolarisWorkflowStepFactory polarisWorkflowStepFactory;
    private AbstractBuild<?, ?> build;

    // TODO: This class is incomplete -- rotte APR 2020

    @Before
    public void setUpMocks() throws Throwable {
        // This class assumes all substeps are tested and working as expected.
        polarisWorkflowStepFactory = Mockito.mock(PolarisWorkflowStepFactory.class);

        PowerMockito.mockStatic(StepWorkflow.class);
        final StepWorkflow.Builder stepWorkflowBuilder = Mockito.mock(StepWorkflow.Builder.class);
        Mockito.when(StepWorkflow.first(Mockito.any())).thenReturn(stepWorkflowBuilder);
        Mockito.when(stepWorkflowBuilder.then(Mockito.any())).thenReturn(stepWorkflowBuilder);

        final StepWorkflow.ConditionalBuilder conditionalBuilder = Mockito.mock(StepWorkflow.ConditionalBuilder.class);
        Mockito.when(stepWorkflowBuilder.andSometimes(Mockito.any())).thenReturn(conditionalBuilder);
        Mockito.when(conditionalBuilder.then(Mockito.any())).thenReturn(conditionalBuilder);
        Mockito.when(conditionalBuilder.butOnlyIf(Mockito.any(Object.class), Mockito.any(Predicate.class))).thenReturn(stepWorkflowBuilder);

        stepWorkflowResponse = Mockito.mock(StepWorkflowResponse.class);
        Mockito.when(stepWorkflowResponse.handleResponse(Mockito.any())).thenCallRealMethod();
        Mockito.when(stepWorkflowBuilder.run()).thenReturn(stepWorkflowResponse);

        build = PowerMockito.mock(AbstractBuild.class);
        final FilePath workspace = new FilePath(new File("."));
        final Node node = Mockito.mock(Node.class);
        Mockito.when(build.getBuiltOn()).thenReturn(node);
        Mockito.when(build.getWorkspace()).thenReturn(workspace);
    }

    @Test
    public void testNullTimeout() throws Throwable {
        Mockito.when(stepWorkflowResponse.wasSuccessful()).thenReturn(true);

        WaitForIssues waitForIssues = new WaitForIssues();
        waitForIssues.setBuildStatusForIssues(ChangeBuildStatusTo.SUCCESS);
        waitForIssues.setJobTimeoutInMinutes(null);
        PolarisBuildStepWorkflow polarisBuildStepWorkflow = new PolarisBuildStepWorkflow("polarisCliName", "polarisArguments", waitForIssues, polarisWorkflowStepFactory, build);
        polarisBuildStepWorkflow.perform();

        Mockito.verify(polarisWorkflowStepFactory).createStepGetTotalIssueCount(null);
    }

}
