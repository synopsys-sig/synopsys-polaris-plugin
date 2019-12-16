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
package com.synopsys.integration.jenkins.polaris.substeps;

import java.io.File;

import org.jenkinsci.remoting.RoleChecker;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.polaris.extensions.global.PolarisGlobalConfig;
import com.synopsys.integration.polaris.common.cli.PolarisDownloadUtility;
import com.synopsys.integration.polaris.common.exception.PolarisIntegrationException;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

import hudson.remoting.Callable;

public class FindOrInstallPolarisCli implements Callable<String, IntegrationException> {
    private static final long serialVersionUID = 6457474109970149144L;
    private final JenkinsIntLogger jenkinsIntLogger;
    private final PolarisGlobalConfig polarisGlobalConfig;
    private final String installationLocation;

    public FindOrInstallPolarisCli(final JenkinsIntLogger jenkinsIntLogger, final PolarisGlobalConfig polarisGlobalConfig, final String installationLocation) {
        this.jenkinsIntLogger = jenkinsIntLogger;
        this.polarisGlobalConfig = polarisGlobalConfig;
        this.installationLocation = installationLocation;
    }

    @Override
    public String call() throws IntegrationException {
        try {
            final File installLocation = new File(installationLocation);
            final AccessTokenPolarisHttpClient accessTokenPolarisHttpClient = polarisGlobalConfig.getPolarisServerConfig().createPolarisHttpClient(jenkinsIntLogger);
            final PolarisDownloadUtility polarisDownloadUtility = PolarisDownloadUtility.fromPolaris(jenkinsIntLogger, accessTokenPolarisHttpClient, installLocation);

            return polarisDownloadUtility.retrievePolarisCliExecutablePath().orElseThrow(() -> new PolarisIntegrationException("The Polaris CLI could not be found or installed correctly."));
        } catch (final IllegalArgumentException ex) {
            throw new PolarisIntegrationException(ex);
        }
    }

    @Override
    public void checkRoles(final RoleChecker checker) throws SecurityException {

    }
}
