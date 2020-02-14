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
package com.synopsys.integration.jenkins.polaris.substeps;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.polaris.common.api.query.model.CountV0;
import com.synopsys.integration.polaris.common.api.query.model.CountV0Attributes;
import com.synopsys.integration.polaris.common.api.query.model.CountV0Resources;
import com.synopsys.integration.polaris.common.cli.PolarisCliResponseUtility;
import com.synopsys.integration.polaris.common.cli.model.IssueSummary;
import com.synopsys.integration.polaris.common.cli.model.PolarisCliResponseModel;
import com.synopsys.integration.polaris.common.cli.model.ScanInfo;
import com.synopsys.integration.polaris.common.exception.PolarisIntegrationException;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.service.PolarisService;
import com.synopsys.integration.stepworkflow.SubStep;
import com.synopsys.integration.stepworkflow.SubStepResponse;

public class GetTotalIssueCount implements SubStep<String, Integer> {
    private final JenkinsIntLogger logger;
    private final PolarisService polarisService;

    public GetTotalIssueCount(final JenkinsIntLogger logger, final PolarisService polarisService) {
        this.logger = logger;
        this.polarisService = polarisService;
    }

    @Override
    public SubStepResponse<Integer> run(final SubStepResponse<? extends String> previousResponse) {
        if (previousResponse.isFailure() || !previousResponse.hasData()) {
            return SubStepResponse.FAILURE(previousResponse);
        }

        final PolarisCliResponseUtility polarisCliResponseUtility = PolarisCliResponseUtility.defaultUtility(logger);
        final PolarisCliResponseModel polarisCliResponseModel = polarisCliResponseUtility.getPolarisCliResponseModelFromString(previousResponse.getData());
        final IssueSummary issueSummary = polarisCliResponseModel.getIssueSummary();
        final ScanInfo scanInfo = polarisCliResponseModel.getScanInfo();

        if (issueSummary != null) {
            logger.debug("Found total issue count, scan must have been run with -w");
            return SubStepResponse.SUCCESS(issueSummary.getTotalIssueCount());
        }

        try {
            if (scanInfo == null || StringUtils.isBlank(scanInfo.getIssueApiUrl())) {
                throw new PolarisIntegrationException("Synopsys Polaris for Jenkins cannot find the total issue count or issue api url in the cli-scan.json. Please ensure that you are using a supported version of the Polaris CLI.");
            }

            logger.debug("Found issue api url, polling for issues");
            final CountV0Resources countV0Resources = polarisService.get(CountV0Resources.class, PolarisRequestFactory.createDefaultBuilder().uri(scanInfo.getIssueApiUrl()).build());
            final List<CountV0> countV0s = countV0Resources.getData();
            return SubStepResponse.SUCCESS(countV0s.stream()
                                               .map(CountV0::getAttributes)
                                               .mapToInt(CountV0Attributes::getValue)
                                               .sum());

        } catch (final IntegrationException e) {
            return SubStepResponse.FAILURE(e);
        }
    }

}
