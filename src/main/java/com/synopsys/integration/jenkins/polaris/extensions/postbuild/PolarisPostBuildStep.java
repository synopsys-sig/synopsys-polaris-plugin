/**
 * synopsys-polaris
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.jenkins.polaris.extensions.postbuild;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jenkins.JenkinsIntLogger;
import com.synopsys.integration.jenkins.annotations.HelpMarkdown;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

public class PolarisPostBuildStep extends Recorder {
    public static final String DISPLAY_NAME = "Synopsys Polaris";

    @HelpMarkdown("The command line options to pass to Synopsys Detect")
    private final String polarisProperties;

    @DataBoundConstructor
    public PolarisPostBuildStep(final String polarisProperties) {
        this.polarisProperties = polarisProperties;
    }

    public String getPolarisProperties() {
        return polarisProperties;
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

        try {
            final FilePath workspace = build.getWorkspace();
            if (workspace == null) {
                throw new IntegrationException("Polaris cannot be executed when the workspace is null");
            }

            // Run polaris and populate exit code

            final int exitCode = 0;

            if (exitCode > 0) {
                logger.error("Polaris failed with exit code " + exitCode);
                build.setResult(Result.FAILURE);
            }
        } catch (final Exception e) {
            if (e instanceof InterruptedException) {
                logger.error("Polaris thread was interrupted", e);
                build.setResult(Result.ABORTED);
                Thread.currentThread().interrupt();
            } else if (e instanceof IntegrationException) {
                logger.error(e.getMessage());
                logger.debug(e.getMessage(), e);
                build.setResult(Result.UNSTABLE);
            } else {
                logger.error(e.getMessage(), e);
                build.setResult(Result.UNSTABLE);
            }
        }
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> implements Serializable {
        private static final long serialVersionUID = 9059602791947799261L;

        public DescriptorImpl() {
            super(PolarisPostBuildStep.class);
            load();
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
