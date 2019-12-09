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

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import com.synopsys.integration.jenkins.annotations.HelpMarkdown;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;

public class ExecutePolarisCliStep extends Step implements Serializable {
    public static final String DISPLAY_NAME = "Execute Synopsys Polaris CLI";
    public static final String PIPELINE_NAME = "executePolaris";
    private static final long serialVersionUID = -2698425344634481146L;

    // Any field set by a DataBoundSetter should be explicitly declared as nullable to avoid NPEs
    @HelpMarkdown("The Polaris CLI installation to execute")
    private final String polarisCliInstallationName;

    @HelpMarkdown("The command line options to pass to the Synopsys Polaris CLI")
    private final String polarisProperties;

    @DataBoundConstructor
    public ExecutePolarisCliStep(final String polarisCliInstallationName, final String polarisProperties) {
        this.polarisCliInstallationName = polarisCliInstallationName;
        this.polarisProperties = polarisProperties;
    }

    public String getPolarisCliInstallationName() {
        return polarisCliInstallationName;
    }

    public String getPolarisProperties() {
        return polarisProperties;
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
        private static final long serialVersionUID = -3799159740768688972L;
        private transient TaskListener listener;
        private transient EnvVars envVars;

        protected Execution(@Nonnull final StepContext context) throws InterruptedException, IOException {
            super(context);
            listener = context.get(TaskListener.class);
            envVars = context.get(EnvVars.class);
        }

        @Override
        protected Integer run() throws Exception {
            // Execute Polaris and populate exit code;
            return 0;
        }
    }
}
