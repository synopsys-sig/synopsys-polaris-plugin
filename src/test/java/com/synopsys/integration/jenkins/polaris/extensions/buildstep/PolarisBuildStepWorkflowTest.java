package com.synopsys.integration.jenkins.polaris.extensions.buildstep;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.synopsys.integration.jenkins.extensions.ChangeBuildStatusTo;
import com.synopsys.integration.jenkins.polaris.workflow.PolarisWorkflowStepFactory;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Node;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AbstractBuild.class, FilePath.class })
public class PolarisBuildStepWorkflowTest {
    private PolarisWorkflowStepFactory polarisWorkflowStepFactory;
    private AbstractBuild<?, ?> build;

    // TODO: This class is incomplete -- rotte APR 2020

    @Before
    public void setUpMocks() {
        polarisWorkflowStepFactory = Mockito.mock(PolarisWorkflowStepFactory.class);
        build = PowerMockito.mock(AbstractBuild.class);
        final FilePath workspace = PowerMockito.mock(FilePath.class);
        final Node node = Mockito.mock(Node.class);
        Mockito.when(build.getBuiltOn()).thenReturn(node);
        Mockito.when(build.getWorkspace()).thenReturn(workspace);
    }

    @Test
    public void testPreserveNullTimeout() throws Throwable {
        final WaitForIssues waitForIssues = new WaitForIssues();
        waitForIssues.setBuildStatusForIssues(ChangeBuildStatusTo.SUCCESS);
        waitForIssues.setJobTimeoutInMinutes(null);
        final PolarisBuildStepWorkflow polarisBuildStepWorkflow = new PolarisBuildStepWorkflow("polarisCliName", "polarisArguments", waitForIssues, polarisWorkflowStepFactory, build);

        polarisBuildStepWorkflow.createWorkflow();

        Mockito.verify(polarisWorkflowStepFactory).createStepGetTotalIssueCount(null);
    }

}
