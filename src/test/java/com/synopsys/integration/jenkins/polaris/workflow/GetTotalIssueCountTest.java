package com.synopsys.integration.jenkins.polaris.workflow;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.polaris.common.api.query.model.CountV0;
import com.synopsys.integration.polaris.common.api.query.model.CountV0Attributes;
import com.synopsys.integration.polaris.common.api.query.model.CountV0Resources;
import com.synopsys.integration.polaris.common.cli.PolarisCliResponseUtility;
import com.synopsys.integration.polaris.common.cli.model.BlackDuckScaToolInfo;
import com.synopsys.integration.polaris.common.cli.model.IssueSummary;
import com.synopsys.integration.polaris.common.cli.model.PolarisCliResponseModel;
import com.synopsys.integration.polaris.common.cli.model.ScanInfo;
import com.synopsys.integration.polaris.common.service.JobService;
import com.synopsys.integration.polaris.common.service.PolarisService;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.stepworkflow.SubStepResponse;

public class GetTotalIssueCountTest {

    private static final int JOB_TIMEOUT_IN_MINUTES = 0;
    private static final Integer ANALYSIS_ISSUE_COUNT = Integer.valueOf(7);
    private static final String ISSUE_URL = "https://issue/url";
    private static final String JOB_STATUS_URL = "https://job/status/url";
    private static final String POLARIS_CLI_SCAN_JSON_RELPATH = "src/test/resources/workspace/.synopsys/polaris/cli-scan.json";

    private static PolarisCliResponseModel polarisCliResponseModel;
    private static JenkinsIntLogger logger;
    private static PolarisCliResponseUtility polarisCliResponseUtility;
    private static PolarisService polarisService;
    private static JobService jobService;
    private static SubStepResponse<String> previousResponse;
    private static ScanInfo scanInfo;

    @BeforeClass
    public static void setup() throws IOException {
        final String jsonContent = FileUtils.readFileToString(new File(POLARIS_CLI_SCAN_JSON_RELPATH));

        logger = Mockito.mock(JenkinsIntLogger.class);
        polarisCliResponseUtility = Mockito.mock(PolarisCliResponseUtility.class);
        polarisService = Mockito.mock(PolarisService.class);
        jobService = Mockito.mock(JobService.class);

        previousResponse = Mockito.mock(SubStepResponse.class);
        Mockito.when(previousResponse.isFailure()).thenReturn(false);
        Mockito.when(previousResponse.isSuccess()).thenReturn(true);
        Mockito.when(previousResponse.hasData()).thenReturn(true);
        Mockito.when(previousResponse.getData()).thenReturn(jsonContent);

        polarisCliResponseModel = Mockito.mock(PolarisCliResponseModel.class);
        Mockito.when(polarisCliResponseUtility.getPolarisCliResponseModelFromString(jsonContent)).thenReturn(polarisCliResponseModel);

        scanInfo = Mockito.mock(ScanInfo.class);
        Mockito.when(polarisCliResponseModel.getScanInfo()).thenReturn(scanInfo);
    }

    @Test
    public void testResultsAreReady() {
        final IssueSummary issueSummary = Mockito.mock(IssueSummary.class);
        Mockito.when(polarisCliResponseModel.getIssueSummary()).thenReturn(issueSummary);

        Mockito.when(issueSummary.getTotalIssueCount()).thenReturn(ANALYSIS_ISSUE_COUNT);

        // test
        final GetTotalIssueCount getTotalIssueCount = new GetTotalIssueCount(logger, polarisCliResponseUtility, polarisService, jobService, JOB_TIMEOUT_IN_MINUTES);
        final SubStepResponse<Integer> response = getTotalIssueCount.run(previousResponse);

        // verify
        assertEquals(ANALYSIS_ISSUE_COUNT, response.getData());
    }

    @Test
    public void testResultsNotReady() throws InterruptedException, IntegrationException {
        Mockito.when(polarisCliResponseModel.getIssueSummary()).thenReturn(null);
        Mockito.when(scanInfo.getIssueApiUrl()).thenReturn(ISSUE_URL);
        final BlackDuckScaToolInfo blackDuckScaToolInfo = Mockito.mock(BlackDuckScaToolInfo.class);
        Mockito.when(polarisCliResponseModel.getBlackDuckScaToolInfo()).thenReturn(blackDuckScaToolInfo);
        Mockito.when(blackDuckScaToolInfo.getJobStatusUrl()).thenReturn(JOB_STATUS_URL);

        //////////// TODO: Mockito.when(jobService.waitForJobToCompleteByUrl(JOB_STATUS_URL, JOB_TIMEOUT_IN_MINUTES, JobService.DEFAULT_WAIT_INTERVAL_IN_SECONDS)).thenReturn(Boolean.TRUE);
        final CountV0Resources countV0Resources = Mockito.mock(CountV0Resources.class);

        // TODO should this be more specific about the request?
        Mockito.when(polarisService.get(Mockito.eq(CountV0Resources.class), Mockito.any(Request.class))).thenReturn(countV0Resources);

        final List<CountV0> counts = new ArrayList<>();
        final CountV0 count = Mockito.mock(CountV0.class);
        counts.add(count);
        Mockito.when(countV0Resources.getData()).thenReturn(counts);
        final CountV0Attributes countV0Attributes = Mockito.mock(CountV0Attributes.class);
        Mockito.when(count.getAttributes()).thenReturn(countV0Attributes);
        Mockito.when(countV0Attributes.getValue()).thenReturn(ANALYSIS_ISSUE_COUNT);

        // test
        final GetTotalIssueCount getTotalIssueCount = new GetTotalIssueCount(logger, polarisCliResponseUtility, polarisService, jobService, JOB_TIMEOUT_IN_MINUTES);
        final SubStepResponse<Integer> response = getTotalIssueCount.run(previousResponse);

        // verify
        assertEquals(ANALYSIS_ISSUE_COUNT, response.getData());
    }
}
