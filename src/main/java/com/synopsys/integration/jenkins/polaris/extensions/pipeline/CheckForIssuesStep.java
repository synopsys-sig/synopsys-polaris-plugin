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
package com.synopsys.integration.jenkins.polaris.extensions.pipeline;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.synopsys.integration.jenkins.annotations.HelpMarkdown;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;

public class CheckForIssuesStep extends Step implements Serializable {
    public static final String DISPLAY_NAME = "Check for Issues in Polaris";
    public static final String PIPELINE_NAME = "polarisIssueCheck";
    private static final long serialVersionUID = -6323415418549347785L;

    // Any field set by a DataBoundSetter should be explicitly declared as nullable to avoid NPEs
    @Nullable
    @HelpMarkdown("Specify the project name to check.")
    private String projectName;

    @Nullable
    @HelpMarkdown("If checked, will return the number of issues discovered instead of throwing an exception.")
    private Boolean returnIssueCount;

    @DataBoundConstructor
    public CheckForIssuesStep() {
        // All fields are optional, so this constructor exists only to prevent some versions of the pipeline syntax generator from failing
    }

    public Boolean getReturnIssueCount() {
        if (Boolean.FALSE.equals(returnIssueCount)) {
            return null;
        }
        return returnIssueCount;
    }

    @DataBoundSetter
    public void setReturnIssueCount(final Boolean returnIssueCount) {
        this.returnIssueCount = returnIssueCount;
    }

    public String getProjectName() {
        if (StringUtils.isBlank(projectName)) {
            return null;
        }
        return projectName;
    }

    @DataBoundSetter
    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    @Override
    public StepExecution start(final StepContext context) throws Exception {
        return new Execution(context);
    }

    @Symbol(PIPELINE_NAME)
    @Extension(optional = true)
    public static final class DescriptorImpl extends StepDescriptor {
        public DescriptorImpl() {
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return new HashSet<>(Arrays.asList(TaskListener.class, EnvVars.class));
        }

        @Override
        public String getFunctionName() {
            return PIPELINE_NAME;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return DISPLAY_NAME;
        }

    }

    public class Execution extends SynchronousNonBlockingStepExecution {
        private static final long serialVersionUID = 8354595739615362769L;
        private transient TaskListener listener;
        private transient EnvVars envVars;

        protected Execution(@Nonnull final StepContext context) throws InterruptedException, IOException {
            super(context);
            listener = context.get(TaskListener.class);
            envVars = context.get(EnvVars.class);
        }

        @Override
        protected Integer run() throws Exception {
            // execute issue check and throw exception if there are issues or return the number as an exit code
            return 0;
        }

    }
}
