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
package com.synopsys.integration.jenkins.polaris.extensions.buildstep;

import java.io.IOException;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.stepworkflow.StepWorkflow;
import com.synopsys.integration.stepworkflow.StepWorkflowResponse;

import hudson.model.AbstractBuild;
import hudson.model.Result;

public class PolarisBuildStepWorker {
    private final PolarisWorkflowStepFactory polarisWorkflowStepFactory;

    public PolarisBuildStepWorker(final PolarisWorkflowStepFactory polarisWorkflowStepFactory) {
        this.polarisWorkflowStepFactory = polarisWorkflowStepFactory;
    }

    public boolean perform() throws InterruptedException, IOException {
        final JenkinsIntLogger logger = polarisWorkflowStepFactory.getOrCreateJenkinsIntLogger();
        return StepWorkflow
                .first(polarisWorkflowStepFactory.createCreatePolarisEnvironmentStep())
                .then(polarisWorkflowStepFactory.createFindPolarisCliStep())
                .then(polarisWorkflowStepFactory.createExecutePolarisCliStep())
                .run().handleResponse(response -> afterPerform(logger, response, polarisWorkflowStepFactory.getBuild()));
    }

    private boolean afterPerform(final JenkinsIntLogger logger, final StepWorkflowResponse<Integer> stepWorkflowResponse, final AbstractBuild<?, ?> build) {
        final boolean wasSuccessful = stepWorkflowResponse.wasSuccessful();
        try {
            if (!wasSuccessful) {
                throw stepWorkflowResponse.getException();
            }
        } catch (final InterruptedException e) {
            logger.error("[ERROR] Synopsys Polaris thread was interrupted.", e);
            build.setResult(Result.ABORTED);
            Thread.currentThread().interrupt();
        } catch (final IntegrationException e) {
            this.handleException(logger, build, Result.FAILURE, e);
        } catch (final Exception e) {
            this.handleException(logger, build, Result.UNSTABLE, e);
        }

        return stepWorkflowResponse.wasSuccessful();
    }

    private void handleException(final JenkinsIntLogger logger, final AbstractBuild build, final Result result, final Exception e) {
        logger.error("[ERROR] " + e.getMessage());
        logger.debug(e.getMessage(), e);
        build.setResult(result);
    }
}
