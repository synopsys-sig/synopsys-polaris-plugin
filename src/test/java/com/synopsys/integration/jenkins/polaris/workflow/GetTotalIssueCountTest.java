package com.synopsys.integration.jenkins.polaris.workflow;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.polaris.common.cli.PolarisCliResponseUtility;
import com.synopsys.integration.polaris.common.cli.model.IssueSummary;
import com.synopsys.integration.polaris.common.cli.model.PolarisCliResponseModel;
import com.synopsys.integration.polaris.common.cli.model.ScanInfo;
import com.synopsys.integration.polaris.common.service.JobService;
import com.synopsys.integration.polaris.common.service.PolarisService;
import com.synopsys.integration.stepworkflow.SubStepResponse;

public class GetTotalIssueCountTest {

    public static final Integer ANALYSIS_ISSUE_COUNT = Integer.valueOf(7);

    @Test
    public void testResultsAreReady() throws IOException {

        final String jsonContent = FileUtils.readFileToString(new File("src/test/resources/workspace/.synopsys/polaris/cli-scan.json"));

        final JenkinsIntLogger logger = Mockito.mock(JenkinsIntLogger.class);
        final PolarisCliResponseUtility polarisCliResponseUtility = Mockito.mock(PolarisCliResponseUtility.class);
        final PolarisService polarisService = Mockito.mock(PolarisService.class);
        final JobService jobService = Mockito.mock(JobService.class);
        final int jobTimeoutInMinutes = 0;

        final SubStepResponse<String> previousResponse;
        previousResponse = Mockito.mock(SubStepResponse.class);
        Mockito.when(previousResponse.isFailure()).thenReturn(false);
        Mockito.when(previousResponse.isSuccess()).thenReturn(true);
        Mockito.when(previousResponse.hasData()).thenReturn(true);
        Mockito.when(previousResponse.getData()).thenReturn(jsonContent);

        // final PolarisCliResponseModel polarisCliResponseModel = polarisCliResponseUtility.getPolarisCliResponseModelFromString(rawJson);
        final PolarisCliResponseModel polarisCliResponseModel = Mockito.mock(PolarisCliResponseModel.class);
        Mockito.when(polarisCliResponseUtility.getPolarisCliResponseModelFromString(jsonContent)).thenReturn(polarisCliResponseModel);

        // final IssueSummary issueSummary = polarisCliResponseModel.getIssueSummary();
        final IssueSummary issueSummary = Mockito.mock(IssueSummary.class);
        Mockito.when(polarisCliResponseModel.getIssueSummary()).thenReturn(issueSummary);
        // final ScanInfo scanInfo = polarisCliResponseModel.getScanInfo();
        final ScanInfo scanInfo = Mockito.mock(ScanInfo.class);
        Mockito.when(polarisCliResponseModel.getScanInfo()).thenReturn(scanInfo);
        // issueSummary.getTotalIssueCount()
        Mockito.when(issueSummary.getTotalIssueCount()).thenReturn(ANALYSIS_ISSUE_COUNT);

        // test
        final GetTotalIssueCount getTotalIssueCount = new GetTotalIssueCount(logger, polarisCliResponseUtility, polarisService, jobService, jobTimeoutInMinutes);
        final SubStepResponse<Integer> response = getTotalIssueCount.run(previousResponse);

        // verify
        assertEquals(ANALYSIS_ISSUE_COUNT, response.getData());
    }
}
