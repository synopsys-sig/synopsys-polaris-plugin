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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jenkins.annotations.HelpMarkdown;
import com.synopsys.integration.jenkins.extensions.ChangeBuildStatusTo;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.polaris.extensions.global.PolarisGlobalConfig;
import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;
import com.synopsys.integration.jenkins.polaris.substeps.CreatePolarisEnvironment;
import com.synopsys.integration.jenkins.polaris.substeps.ExecutePolarisCli;
import com.synopsys.integration.jenkins.polaris.substeps.GetPathToPolarisCli;
import com.synopsys.integration.jenkins.polaris.substeps.GetPolarisCliResponseContent;
import com.synopsys.integration.jenkins.polaris.substeps.GetTotalIssueCount;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.service.PolarisService;
import com.synopsys.integration.polaris.common.service.PolarisServicesFactory;
import com.synopsys.integration.stepworkflow.StepWorkflow;
import com.synopsys.integration.stepworkflow.StepWorkflowResponse;
import com.synopsys.integration.stepworkflow.SubStep;
import com.synopsys.integration.stepworkflow.jenkins.RemoteSubStep;
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
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;

public class PolarisBuildStep extends Builder {
    public static final String DISPLAY_NAME = "Synopsys Polaris";

    @HelpMarkdown("The Polaris CLI installation to execute")
    private final String polarisCliName;

    @HelpMarkdown("The command line arguments to pass to the Synopsys Polaris CLI")
    private final String polarisArguments;

    @HelpMarkdown("Check this box to wait for issues ")
    private final WaitForIssues waitForIssues;

    @DataBoundConstructor
    public PolarisBuildStep(final String polarisCliName, final String polarisArguments, final WaitForIssues waitForIssues) {
        this.polarisCliName = polarisCliName;
        this.polarisArguments = polarisArguments;
        this.waitForIssues = waitForIssues;
    }

    public String getPolarisArguments() {
        return polarisArguments;
    }

    public String getPolarisCliName() {
        return polarisCliName;
    }

    public WaitForIssues getWaitForIssues() {
        return waitForIssues;
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

        final PolarisGlobalConfig polarisGlobalConfig = GlobalConfiguration.all().get(PolarisGlobalConfig.class);
        if (polarisGlobalConfig == null) {
            throw new AbortException("Polaris cannot be executed: No Polaris global configuration detected in the Jenkins system configuration.");
        }

        final PolarisServerConfig polarisServerConfig = polarisGlobalConfig.getPolarisServerConfig();
        final PolarisServicesFactory polarisServicesFactory = polarisServerConfig.createPolarisServicesFactory(logger);
        final PolarisService polarisService = polarisServicesFactory.createPolarisService();

        final IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables(false);
        final EnvVars envVars = build.getEnvironment(listener);
        polarisCli = polarisCli.forEnvironment(envVars);
        polarisCli = polarisCli.forNode(node, listener);
        intEnvironmentVariables.putAll(envVars);
        logger.setLogLevel(intEnvironmentVariables);

        final CreatePolarisEnvironment createPolarisEnvironment = new CreatePolarisEnvironment(logger, intEnvironmentVariables);
        final GetPathToPolarisCli getPathToPolarisCli = new GetPathToPolarisCli(polarisCli.getHome());
        final ExecutePolarisCli executePolarisCli = new ExecutePolarisCli(logger, launcher, intEnvironmentVariables, workspace, listener, polarisArguments);
        final GetPolarisCliResponseContent getPolarisCliResponseContent = new GetPolarisCliResponseContent(logger, workspace.getRemote());
        final GetTotalIssueCount getTotalIssueCount = new GetTotalIssueCount(logger, polarisService);
        final VirtualChannel channel = launcher.getChannel();

        return StepWorkflow.first(createPolarisEnvironment)
                   .then(RemoteSubStep.of(channel, getPathToPolarisCli))
                   .then(executePolarisCli)
                   .andSometimes(RemoteSubStep.of(channel, getPolarisCliResponseContent)).then(getTotalIssueCount).then(SubStep.ofConsumer(issueCount -> failOnIssuesPresent(logger, issueCount, build)))
                   .butOnlyIf(waitForIssues, Objects::nonNull)
                   .run()
                   .handleResponse(response -> afterPerform(logger, response, build));
    }

    private boolean afterPerform(final JenkinsIntLogger logger, final StepWorkflowResponse<Object> stepWorkflowResponse, final AbstractBuild<?, ?> build) {
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

    private void failOnIssuesPresent(final JenkinsIntLogger logger, final Integer issueCount, final AbstractBuild<?, ?> build) {
        final ChangeBuildStatusTo buildStatusToSet;
        if (waitForIssues == null) {
            buildStatusToSet = ChangeBuildStatusTo.SUCCESS;
        } else {
            buildStatusToSet = waitForIssues.getBuildStatusForIssues();
        }

        logger.alwaysLog("Polaris Issue Check");
        logger.alwaysLog("-- Build state for issues: " + buildStatusToSet.getDisplayName());
        logger.alwaysLog(String.format("Found %s issues in view.", issueCount));

        if (issueCount > 0) {
            final Result result = buildStatusToSet.getResult();
            logger.alwaysLog("Setting build status to " + result.toString());
            build.setResult(result);
        }
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
