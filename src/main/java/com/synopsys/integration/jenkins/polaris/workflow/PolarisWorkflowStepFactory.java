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
package com.synopsys.integration.jenkins.polaris.workflow;

import java.io.IOException;
import java.util.Optional;

import com.synopsys.integration.function.ThrowingConsumer;
import com.synopsys.integration.function.ThrowingSupplier;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.polaris.extensions.global.PolarisGlobalConfig;
import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.service.JobService;
import com.synopsys.integration.polaris.common.service.PolarisService;
import com.synopsys.integration.polaris.common.service.PolarisServicesFactory;
import com.synopsys.integration.stepworkflow.SubStep;
import com.synopsys.integration.stepworkflow.jenkins.RemoteSubStep;
import com.synopsys.integration.util.IntEnvironmentVariables;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.TaskListener;
import jenkins.model.GlobalConfiguration;

public class PolarisWorkflowStepFactory {
    private final EnvVars envVars;
    private final Launcher launcher;
    private final TaskListener listener;
    private final ThrowingSupplier<Node, AbortException> validatedNode;
    private final ThrowingSupplier<FilePath, AbortException> validatedWorkspace;

    // These fields are lazily initialized; inside this class: use getOrCreate...() to get these values
    private IntEnvironmentVariables intEnvironmentVariables = null;
    private JenkinsIntLogger logger = null;

    public PolarisWorkflowStepFactory(final Node node, final FilePath workspace, final EnvVars envVars, final Launcher launcher, final TaskListener listener) {
        this.validatedNode = () -> validateNode(node);
        this.validatedWorkspace = () -> validateWorkspace(workspace);
        this.envVars = envVars;
        this.launcher = launcher;
        this.listener = listener;
    }

    public CreatePolarisEnvironment createStepCreatePolarisEnvironment() {
        final IntEnvironmentVariables intEnvironmentVariables = getOrCreateEnvironmentVariables();
        final JenkinsIntLogger logger = getOrCreateLogger();
        return new CreatePolarisEnvironment(logger, intEnvironmentVariables);
    }

    public RemoteSubStep<String> createStepFindPolarisCli(final String polarisCliName) throws IOException, InterruptedException {
        PolarisCli polarisCli = PolarisCli.findInstanceWithName(polarisCliName)
                                    .orElseThrow(() -> new AbortException("Polaris cannot be executed: No Polaris CLI installations found. Please configure a Polaris CLI installation in the system tool configuration."));
        polarisCli = polarisCli.forEnvironment(envVars);
        polarisCli = polarisCli.forNode(validatedNode.get(), listener);

        final GetPathToPolarisCli getPathToPolarisCli = new GetPathToPolarisCli(polarisCli.getHome());
        return RemoteSubStep.of(launcher.getChannel(), getPathToPolarisCli);
    }

    public ExecutePolarisCli createStepExecutePolarisCli(final String polarisArguments) throws AbortException {
        final JenkinsIntLogger logger = getOrCreateLogger();
        final IntEnvironmentVariables intEnvironmentVariables = getOrCreateEnvironmentVariables();
        return new ExecutePolarisCli(logger, launcher, intEnvironmentVariables, validatedWorkspace.get(), listener, polarisArguments);
    }

    public RemoteSubStep<String> createStepGetPolarisCliResponseContent() throws AbortException {
        final GetPolarisCliResponseContent getPolarisCliResponseContent = new GetPolarisCliResponseContent(validatedWorkspace.get().getRemote());
        return RemoteSubStep.of(launcher.getChannel(), getPolarisCliResponseContent);
    }

    public GetTotalIssueCount createStepGetTotalIssueCount(final Integer jobTimeoutInMinutes) throws AbortException {
        final JenkinsIntLogger logger = getOrCreateLogger();
        final PolarisGlobalConfig polarisGlobalConfig = GlobalConfiguration.all().get(PolarisGlobalConfig.class);
        if (polarisGlobalConfig == null) {
            throw new AbortException("Polaris cannot be executed: No Polaris global configuration detected in the Jenkins system configuration.");
        }
        final PolarisServerConfig polarisServerConfig = polarisGlobalConfig.getPolarisServerConfig();
        final PolarisServicesFactory polarisServicesFactory = polarisServerConfig.createPolarisServicesFactory(logger);
        final JobService jobService = polarisServicesFactory.createJobService();
        final PolarisService polarisService = polarisServicesFactory.createPolarisService();
        return new GetTotalIssueCount(logger, polarisService, jobService, Optional.ofNullable(jobTimeoutInMinutes).orElse(JobService.DEFAULT_JOB_TIMEOUT_IN_MINUTES));
    }

    public SubStep<Integer, Object> createStepWithConsumer(final ThrowingConsumer<Integer, RuntimeException> consumer) {
        return SubStep.ofConsumer(consumer);
    }

    public JenkinsIntLogger getOrCreateLogger() {
        if (logger == null) {
            logger = new JenkinsIntLogger(listener);
            logger.setLogLevel(getOrCreateEnvironmentVariables());
        }
        return logger;
    }

    public IntEnvironmentVariables getOrCreateEnvironmentVariables() {
        if (intEnvironmentVariables == null) {
            intEnvironmentVariables = new IntEnvironmentVariables(false);
            intEnvironmentVariables.putAll(envVars);
        }
        return intEnvironmentVariables;
    }

    private Node validateNode(final Node node) throws AbortException {
        if (node == null) {
            throw new AbortException("Polaris cannot be executed: The node that it was executed on no longer exists.");
        }

        return node;
    }

    private FilePath validateWorkspace(final FilePath workspace) throws AbortException {
        if (workspace == null) {
            throw new AbortException("Polaris cannot be executed: The workspace could not be determined.");
        }

        return workspace;
    }

}
