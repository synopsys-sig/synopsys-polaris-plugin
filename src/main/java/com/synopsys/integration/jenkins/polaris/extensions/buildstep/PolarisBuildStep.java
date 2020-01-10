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
import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jenkins.annotations.HelpMarkdown;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;
import com.synopsys.integration.jenkins.polaris.substeps.CreatePolarisEnvironment;
import com.synopsys.integration.jenkins.polaris.substeps.ExecutePolarisCli;
import com.synopsys.integration.stepworkflow.StepWorkflow;
import com.synopsys.integration.stepworkflow.StepWorkflowResponse;
import com.synopsys.integration.util.IntEnvironmentVariables;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Node;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.ListBoxModel;

public class PolarisBuildStep extends Builder {
    public static final String DISPLAY_NAME = "Synopsys Polaris";

    @HelpMarkdown("The Polaris CLI installation to execute")
    private final String polarisCliName;

    @HelpMarkdown("The command line arguments to pass to the Synopsys Polaris CLI")
    private final String polarisArguments;

    @DataBoundConstructor
    public PolarisBuildStep(final String polarisCliName, final String polarisArguments /*final ChangeBuildStatusTo buildStatusForIssues, final boolean waitForIssues*/) {
        this.polarisCliName = polarisCliName;
        this.polarisArguments = polarisArguments;
        /*this.buildStatusOnProblems = buildStatusForIssues;
        this.waitForIssues = waitForIssues;*/
    }

    public String getPolarisArguments() {
        return polarisArguments;
    }

    public String getPolarisCliName() {
        return polarisCliName;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        final JenkinsIntLogger logger = new JenkinsIntLogger(listener);

        final FilePath workspace = build.getWorkspace();
        if (workspace == null) {
            throw new AbortException("Polaris cannot be executed: The workspace could not be determined.");
        }

        PolarisCli polarisCli = PolarisCli.findInstanceWithName(polarisCliName)
                                    .orElseThrow(() -> new AbortException(
                                        "Polaris cannot be executed: No Polaris CLI installations found. Please configure a Polaris CLI installation in the system tool configuration."));
        final Node node = build.getBuiltOn();
        if (node == null) {
            throw new AbortException("Polaris cannot be executed: The node that it was executed on no longer exists.");
        }

        final IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables(false);
        final EnvVars envVars = build.getEnvironment(listener);
        polarisCli = polarisCli.forEnvironment(envVars);
        polarisCli = polarisCli.forNode(node, listener);
        intEnvironmentVariables.putAll(envVars);
        logger.setLogLevel(intEnvironmentVariables);

        final CreatePolarisEnvironment createPolarisEnvironment = new CreatePolarisEnvironment(logger, intEnvironmentVariables);
        final ExecutePolarisCli executePolarisCli = new ExecutePolarisCli(launcher, intEnvironmentVariables, workspace, listener, polarisCli, polarisArguments);

        return StepWorkflow.first(createPolarisEnvironment)
                   .then(executePolarisCli)
                   .run()
                   .handleResponse(response -> afterPerform(logger, response, build));
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

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> implements Serializable {
        private static final long serialVersionUID = -3800519788262007744L;

        public DescriptorImpl() {
            super(PolarisBuildStep.class);
            load();
        }

        public ListBoxModel doFillPolarisCliNameItems() {
            final PolarisCli.DescriptorImpl polarisCliToolInstallationDescriptor = ToolInstallation.all().get(PolarisCli.DescriptorImpl.class);

            if (polarisCliToolInstallationDescriptor == null) {
                return new ListBoxModel();
            }

            return Stream.of(polarisCliToolInstallationDescriptor.getInstallations())
                       .map(PolarisCli::getName)
                       .map(ListBoxModel.Option::new)
                       .collect(Collectors.toCollection(ListBoxModel::new));
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return DISPLAY_NAME;
        }

    }

}
