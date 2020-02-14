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

import com.synopsys.integration.jenkins.annotations.HelpMarkdown;
import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
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
        validateBuild(build);

        final PolarisWorkflowStepFactory polarisWorkflowStepFactory = new PolarisWorkflowStepFactory(polarisCliName, polarisArguments, build.getBuiltOn(), build.getWorkspace(), build.getEnvironment(listener), launcher, listener);
        final PolarisBuildStepWorkflow polarisBuildStepWorkflow = new PolarisBuildStepWorkflow(waitForIssues, polarisWorkflowStepFactory, build);
        final boolean result = polarisBuildStepWorkflow.perform();
        return result;
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

    private void validateBuild(final AbstractBuild<?, ?> build) throws AbortException {
        if (build.getWorkspace() == null) {
            throw new AbortException("Polaris cannot be executed: The workspace could not be determined.");
        }
        if (build.getBuiltOn() == null) {
            throw new AbortException("Polaris cannot be executed: The node that it was executed on no longer exists.");
        }
    }
}
