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

import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;
import com.synopsys.integration.stepworkflow.StepWorkflow;
import com.synopsys.integration.util.IntEnvironmentVariables;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Node;

public class PolarisBuildStepPerformer {
    private final PolarisCliFactory polarisCliFactory;
    private final PolarisWorkflow polarisWorkflow;
    private final JenkinsIntLoggerFactory jenkinsIntLoggerFactory;
    private final IntEnvironmentVariablesFactory intEnvironmentVariablesFactory;

    public PolarisBuildStepPerformer(final PolarisCliFactory polarisCliFactory, final PolarisWorkflow polarisWorkflow, final JenkinsIntLoggerFactory jenkinsIntLoggerFactory,
        final IntEnvironmentVariablesFactory intEnvironmentVariablesFactory) {
        this.polarisCliFactory = polarisCliFactory;
        this.polarisWorkflow = polarisWorkflow;
        this.jenkinsIntLoggerFactory = jenkinsIntLoggerFactory;
        this.intEnvironmentVariablesFactory = intEnvironmentVariablesFactory;
    }

    // TODO we'll need more of these? or a more flexible one of these? this method will take the factory object , that creates the steps
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final EnvVars envVars, final Node node, final BuildListener listener, final FilePath workspace, final String polarisCliName, final String polarisArguments)
        throws IOException, InterruptedException {
        final PolarisCli polarisCli = polarisCliFactory.createPolarisCli(envVars, node, listener, polarisCliName);
        final IntEnvironmentVariables intEnvironmentVariables = intEnvironmentVariablesFactory.createIntEnvironmentVariables(envVars);
        final JenkinsIntLogger logger = jenkinsIntLoggerFactory.createJenkinsIntLogger(listener, intEnvironmentVariables);

        final PolarisWorkflowSteps polarisWorkflowSteps = polarisWorkflow.createPolarisWorkflowSteps(launcher, listener, logger, workspace, polarisCli, intEnvironmentVariables, polarisArguments);
        final StepWorkflow.Builder<Integer> workflow = polarisWorkflow. createWorkflowFromSteps(polarisWorkflowSteps);

        final boolean result = polarisWorkflow.executeWorkflow(build, logger, workflow);
        return result;
    }
}
