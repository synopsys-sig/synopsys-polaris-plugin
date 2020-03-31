/**
 * synopsys-polaris
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.jenkins.polaris.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.polaris.common.PolarisCliResponseVersion;
import com.synopsys.integration.polaris.common.api.query.model.CountV0;
import com.synopsys.integration.polaris.common.api.query.model.CountV0Attributes;
import com.synopsys.integration.polaris.common.api.query.model.CountV0Resources;
import com.synopsys.integration.polaris.common.cli.PolarisCliResponseUtility;
import com.synopsys.integration.polaris.common.cli.model.BlackDuckScaToolInfo;
import com.synopsys.integration.polaris.common.cli.model.CoverityToolInfo;
import com.synopsys.integration.polaris.common.cli.model.IssueSummary;
import com.synopsys.integration.polaris.common.cli.model.PolarisCliResponseModel;
import com.synopsys.integration.polaris.common.cli.model.ScanInfo;
import com.synopsys.integration.polaris.common.exception.PolarisIntegrationException;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.service.JobService;
import com.synopsys.integration.polaris.common.service.PolarisService;
import com.synopsys.integration.stepworkflow.SubStep;
import com.synopsys.integration.stepworkflow.SubStepResponse;

public class GetTotalIssueCount implements SubStep<String, Integer> {
    public static final Integer SUPPORTED_POLARIS_CLI_RESPONSE_MAJOR_VERSION = 1;
    public static final String STEP_EXCEPTION_PREFIX = "Issue count for most recent Polaris Analysis could not be determined: ";
    private final JenkinsIntLogger logger;
    private final PolarisService polarisService;
    private final JobService jobService;
    private final Integer jobTimeoutInMinutes;

    public GetTotalIssueCount(final JenkinsIntLogger logger, final PolarisService polarisService, final JobService jobService, final int jobTimeoutInMinutes) {
        this.logger = logger;
        this.polarisService = polarisService;
        this.jobService = jobService;
        this.jobTimeoutInMinutes = jobTimeoutInMinutes;
    }

    @Override
    public SubStepResponse<Integer> run(final SubStepResponse<? extends String> previousResponse) {
        if (previousResponse.isFailure() || !previousResponse.hasData()) {
            return SubStepResponse.FAILURE(previousResponse);
        }

        final PolarisCliResponseUtility polarisCliResponseUtility = PolarisCliResponseUtility.defaultUtility(logger);
        final String rawJson = previousResponse.getData();
        final PolarisCliResponseModel polarisCliResponseModel = polarisCliResponseUtility.getPolarisCliResponseModelFromString(rawJson);

        try {
            final String versionString = polarisCliResponseModel.getVersion();
            final PolarisCliResponseVersion version = PolarisCliResponseVersion.parse(versionString)
                                                          .orElseThrow(() -> new PolarisIntegrationException(STEP_EXCEPTION_PREFIX + "Version string " + versionString + " is not a valid version. "
                                                                                                                 + "Please verify that you are using a supported version of the Polaris CLI."));
            if (!SUPPORTED_POLARIS_CLI_RESPONSE_MAJOR_VERSION.equals(version.getMajor())) {
                throw new PolarisIntegrationException(
                    STEP_EXCEPTION_PREFIX + "The version of the cli-scan.json is " + versionString + " and this version of Synopsys Polaris for Jenkins only supports major version " + SUPPORTED_POLARIS_CLI_RESPONSE_MAJOR_VERSION
                        + ". Please verify that you are using a supported version of the Polaris CLI.");
            }

            final IssueSummary issueSummary = polarisCliResponseModel.getIssueSummary();
            final ScanInfo scanInfo = polarisCliResponseModel.getScanInfo();

            if (issueSummary != null) {
                logger.debug("Found total issue count in cli-scan.json, scan must have been run with -w");
                return SubStepResponse.SUCCESS(issueSummary.getTotalIssueCount());
            }

            if (jobTimeoutInMinutes < 0) {
                throw new PolarisIntegrationException("Issue count for most recent Polaris Analysis could not be determined: job timeout must be a positive number if the Polaris CLI is being run without -w");
            }

            final String issueApiUrl = Optional.ofNullable(scanInfo)
                                           .map(ScanInfo::getIssueApiUrl)
                                           .filter(StringUtils::isNotBlank)
                                           .orElseThrow(() -> new PolarisIntegrationException(
                                               "Synopsys Polaris for Jenkins cannot find the total issue count or issue api url in the cli-scan.json. Please ensure that you are using a supported version of the Polaris CLI."
                                           ));

            logger.debug("Found issue api url, polling for job status");
            final List<String> jobStatusUrls = new ArrayList<>();

            Optional.ofNullable(polarisCliResponseModel.getBlackDuckScaToolInfo())
                .map(BlackDuckScaToolInfo::getJobStatusUrl)
                .ifPresent(jobStatusUrls::add);

            Optional.ofNullable(polarisCliResponseModel.getCoverityToolInfo())
                .map(CoverityToolInfo::getJobStatusUrl)
                .ifPresent(jobStatusUrls::add);

            for (final String jobStatusUrl : jobStatusUrls) {
                jobService.waitForJobStateIsCompletedOrDieByUrl(jobStatusUrl, jobTimeoutInMinutes, JobService.DEFAULT_WAIT_INTERVAL_IN_SECONDS);
            }

            final CountV0Resources countV0Resources = polarisService.get(CountV0Resources.class, PolarisRequestFactory.createDefaultBuilder().uri(issueApiUrl).build());
            final Integer totalIssues = countV0Resources.getData().stream()
                                            .map(CountV0::getAttributes)
                                            .map(CountV0Attributes::getValue)
                                            .filter(Objects::nonNull)
                                            .reduce(0, Integer::sum);

            return SubStepResponse.SUCCESS(totalIssues);

        } catch (final InterruptedException | IntegrationException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return SubStepResponse.FAILURE(e);
        }
    }

}
