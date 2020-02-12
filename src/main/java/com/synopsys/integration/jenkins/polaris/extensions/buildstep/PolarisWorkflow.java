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

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;
import com.synopsys.integration.jenkins.polaris.substeps.CreatePolarisEnvironment;
import com.synopsys.integration.jenkins.polaris.substeps.ExecutePolarisCli;
import com.synopsys.integration.jenkins.polaris.substeps.GetPathToPolarisCli;
import com.synopsys.integration.stepworkflow.StepWorkflow;
import com.synopsys.integration.stepworkflow.StepWorkflowResponse;
import com.synopsys.integration.stepworkflow.jenkins.RemoteSubStep;
import com.synopsys.integration.util.IntEnvironmentVariables;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;

public class PolarisWorkflow {
// TODO (early) have a factory do this (lazily);
    public PolarisWorkflowSteps createPolarisWorkflowSteps(final Launcher launcher, final BuildListener listener, final JenkinsIntLogger logger, final FilePath workspace, final PolarisCli polarisCli,
            final IntEnvironmentVariables intEnvironmentVariables, final String polarisArguments) {

        final CreatePolarisEnvironment createPolarisEnvironment = new CreatePolarisEnvironment(logger, intEnvironmentVariables);
        final RemoteSubStep<String> findPolarisCli = createRemoteSubStepForGetPathToPolarisCli(launcher, polarisCli);
        final ExecutePolarisCli executePolarisCli = new ExecutePolarisCli(logger, launcher, intEnvironmentVariables, workspace, listener, polarisArguments);

        return new PolarisWorkflowSteps(createPolarisEnvironment, findPolarisCli, executePolarisCli);
    }
// TODO (early) call the factory directly from here??
    public StepWorkflow.Builder<Integer> createWorkflowFromSteps(final PolarisWorkflowSteps polarisWorkflowSteps) {
        return StepWorkflow
                   .first(polarisWorkflowSteps.getCreatePolarisEnvironment())
                   .then(polarisWorkflowSteps.getFindPolarisCli())
                   .then(polarisWorkflowSteps.getExecutePolarisCli());
    }

    public boolean executeWorkflow(final AbstractBuild<?, ?> build, final JenkinsIntLogger logger, final StepWorkflow.Builder<Integer> workflow) {
        return workflow.run().handleResponse(response -> afterPerform(logger, response, build));
    }

    private RemoteSubStep<String> createRemoteSubStepForGetPathToPolarisCli(final Launcher launcher, final PolarisCli polarisCli) {
        final GetPathToPolarisCli getPathToPolarisCli = new GetPathToPolarisCli(polarisCli.getHome());
        return RemoteSubStep.of(launcher.getChannel(), getPathToPolarisCli);
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
