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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.synopsys.integration.jenkins.annotations.HelpMarkdown;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;
import com.synopsys.integration.jenkins.polaris.substeps.CreatePolarisEnvironment;
import com.synopsys.integration.jenkins.polaris.substeps.ExecutePolarisCli;
import com.synopsys.integration.stepworkflow.StepWorkflow;
import com.synopsys.integration.util.IntEnvironmentVariables;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tools.ToolInstallation;
import hudson.util.ListBoxModel;

public class ExecutePolarisCliStep extends Step implements Serializable {
    public static final String DISPLAY_NAME = "Execute Synopsys Polaris CLI";
    public static final String PIPELINE_NAME = "polaris";
    private static final long serialVersionUID = -2698425344634481146L;

    @HelpMarkdown("The command line arguments to pass to the Synopsys Polaris CLI")
    private final String arguments;

    @Nullable
    @HelpMarkdown("The Polaris CLI installation to execute")
    private String polarisCli;

    @DataBoundConstructor
    public ExecutePolarisCliStep(final String arguments) {
        this.arguments = arguments;
    }

    public String getPolarisCli() {
        return polarisCli;
    }

    @DataBoundSetter
    public void setPolarisCli(final String polarisCli) {
        this.polarisCli = polarisCli;
    }

    public String getArguments() {
        return arguments;
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

        public ListBoxModel doFillPolarisCliItems() {
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
        public Set<? extends Class<?>> getRequiredContext() {
            return new HashSet<>(Arrays.asList(TaskListener.class, EnvVars.class, FilePath.class, Launcher.class, Node.class));
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
        private transient FilePath workspace;
        private transient Launcher launcher;
        private transient Node node;

        protected Execution(@Nonnull final StepContext context) throws InterruptedException, IOException {
            super(context);
            listener = context.get(TaskListener.class);
            envVars = context.get(EnvVars.class);
            workspace = context.get(FilePath.class);
            launcher = context.get(Launcher.class);
            node = context.get(Node.class);
        }

        @Override
        protected Integer run() throws Exception {
            final JenkinsIntLogger logger = new JenkinsIntLogger(listener);

            if (workspace == null) {
                throw new AbortException("Polaris cannot be executed: The workspace could not be determined.");
            }

            PolarisCli polarisCli = PolarisCli.findInstanceWithName(ExecutePolarisCliStep.this.polarisCli)
                                        .orElseThrow(() -> new AbortException("Polaris cannot be executed: No Polaris CLI installations found. Please configure a Polaris CLI installation in the system tool configuration."));

            if (node == null) {
                throw new AbortException("Polaris cannot be executed: The node that it was executed on no longer exists.");
            }

            final IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables(false);
            polarisCli = polarisCli.forEnvironment(envVars);
            polarisCli = polarisCli.forNode(node, listener);
            intEnvironmentVariables.putAll(envVars);
            logger.setLogLevel(intEnvironmentVariables);

            final CreatePolarisEnvironment createPolarisEnvironment = new CreatePolarisEnvironment(logger, intEnvironmentVariables);
            final ExecutePolarisCli executePolarisCli = new ExecutePolarisCli(launcher, intEnvironmentVariables, workspace, listener, polarisCli, arguments);

            return StepWorkflow.first(createPolarisEnvironment)
                       .then(executePolarisCli)
                       .run()
                       .getDataOrThrowException();
        }
    }
}
