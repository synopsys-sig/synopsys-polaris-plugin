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

import com.synopsys.integration.function.ThrowingConsumer;
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
import com.synopsys.integration.stepworkflow.SubStep;
import com.synopsys.integration.stepworkflow.jenkins.RemoteSubStep;
import com.synopsys.integration.util.IntEnvironmentVariables;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Node;
import jenkins.model.GlobalConfiguration;

public class PolarisWorkflowStepFactory {
    private final String polarisCliName;
    private final String polarisArguments;
    private final Node node;
    private final FilePath workspace;
    private final EnvVars envVars;
    private final Launcher launcher;
    private final BuildListener listener;

    // These fields are lazily initialized; inside this class: use getOrCreate...() to get these values
    private IntEnvironmentVariables intEnvironmentVariables = null;
    private JenkinsIntLogger logger = null;


    public PolarisWorkflowStepFactory(final String polarisCliName, final String polarisArguments, final Node node, final FilePath workspace, final EnvVars envVars, final Launcher launcher, final BuildListener listener) {
        this.polarisCliName = polarisCliName;
        this.polarisArguments = polarisArguments;
        this.node = node;
        this.workspace = workspace;
        this.envVars = envVars;
        this.launcher = launcher;
        this.listener = listener;
    }

    public CreatePolarisEnvironment createCreatePolarisEnvironmentStep() throws IOException, InterruptedException {
        final IntEnvironmentVariables intEnvironmentVariables = getOrCreateIntEnvironmentVariables();
        final JenkinsIntLogger logger = getOrCreateJenkinsIntLogger();
        return new CreatePolarisEnvironment(logger, intEnvironmentVariables);
    }

    public RemoteSubStep<String> createFindPolarisCliStep() throws IOException, InterruptedException {
        PolarisCli polarisCli = PolarisCli.findInstanceWithName(polarisCliName)
                                    .orElseThrow(() -> new AbortException(
                                        "Polaris cannot be executed: No Polaris CLI installations found. Please configure a Polaris CLI installation in the system tool configuration."));
        polarisCli = polarisCli.forEnvironment(envVars);
        polarisCli = polarisCli.forNode(node, listener);

        final GetPathToPolarisCli getPathToPolarisCli = new GetPathToPolarisCli(polarisCli.getHome());
        return RemoteSubStep.of(launcher.getChannel(), getPathToPolarisCli);
    }

    public ExecutePolarisCli createExecutePolarisCliStep() throws IOException, InterruptedException {
        final JenkinsIntLogger logger = getOrCreateJenkinsIntLogger();
        final IntEnvironmentVariables intEnvironmentVariables = getOrCreateIntEnvironmentVariables();
        final ExecutePolarisCli executePolarisCli = new ExecutePolarisCli(logger, launcher, intEnvironmentVariables, workspace, listener, polarisArguments);
        return executePolarisCli;
    }

    public RemoteSubStep<String>  createGetPolarisCliResponseContentStep() {
        final GetPolarisCliResponseContent getPolarisCliResponseContent = new GetPolarisCliResponseContent(logger, workspace.getRemote());
        final RemoteSubStep<String> getPolarisCliResponseContentRemoteStep = RemoteSubStep.of(launcher.getChannel(), getPolarisCliResponseContent);
        return getPolarisCliResponseContentRemoteStep;
    }

    public GetTotalIssueCount createGetTotalIssueCountStep() throws AbortException {
        final PolarisGlobalConfig polarisGlobalConfig = GlobalConfiguration.all().get(PolarisGlobalConfig.class);
        if (polarisGlobalConfig == null) {
            throw new AbortException("Polaris cannot be executed: No Polaris global configuration detected in the Jenkins system configuration.");
        }
        final PolarisServerConfig polarisServerConfig = polarisGlobalConfig.getPolarisServerConfig();
        final PolarisServicesFactory polarisServicesFactory = polarisServerConfig.createPolarisServicesFactory(logger);
        final PolarisService polarisService = polarisServicesFactory.createPolarisService();
        return new GetTotalIssueCount(logger, polarisService);
    }

    // TODO use this:
    public SubStep createStepOfGivenConsumer(final ThrowingConsumer<Integer, ? extends Exception> consumer) {
        return SubStep.ofConsumer(consumer);
    }

    public JenkinsIntLogger getOrCreateJenkinsIntLogger() throws IOException, InterruptedException {
        if (logger == null) {
            logger = new JenkinsIntLogger(listener);
            logger.setLogLevel(getOrCreateIntEnvironmentVariables());
        }
        return logger;
    }

    private IntEnvironmentVariables getOrCreateIntEnvironmentVariables() {
        if (intEnvironmentVariables == null) {
            intEnvironmentVariables = new IntEnvironmentVariables(false);
            intEnvironmentVariables.putAll(envVars);
        }
        return intEnvironmentVariables;
    }
}
