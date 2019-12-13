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
package com.synopsys.integration.jenkins.polaris.extensions.tools;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;

import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.polaris.extensions.global.PolarisGlobalConfig;
import com.synopsys.integration.polaris.common.cli.PolarisDownloadUtility;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolInstallerDescriptor;
import jenkins.model.GlobalConfiguration;

public class PolarisCliToolInstaller extends ToolInstaller {
    @DataBoundConstructor
    public PolarisCliToolInstaller(final String label) {
        super(label);
    }

    @Override
    public FilePath performInstallation(final ToolInstallation tool, final Node node, final TaskListener log) throws IOException, InterruptedException {
        final JenkinsIntLogger jenkinsIntLogger = new JenkinsIntLogger(log);
        final PolarisGlobalConfig polarisGlobalConfig = GlobalConfiguration.all().get(PolarisGlobalConfig.class);

        if (polarisGlobalConfig == null) {
            throw new AbortException("Cannot install Polaris CLI Installation" + tool.getName() + " because no Polaris global config was found. Please check your system config.");
        }

        final VirtualChannel virtualChannel = node.getChannel();

        if (virtualChannel == null) {
            throw new AbortException("Cannot install Polaris CLI Installation" + tool.getName() + " because node " + node.getDisplayName() + " is not connected or offline");
        }

        final FilePath polarisCliPath = virtualChannel.call(new CallableInstallerImpl(jenkinsIntLogger, polarisGlobalConfig, preferredLocation(tool, node)))
                                            .map(polarisCliRemotePath -> new FilePath(virtualChannel, polarisCliRemotePath))
                                            .orElseThrow(() -> new AbortException("Polaris CLI failed to install"));

        return polarisCliPath.getParent();
    }

    @Extension
    public static final class DescriptorImpl extends ToolInstallerDescriptor<PolarisCliToolInstaller> {
        @Override
        public String getDisplayName() {
            return "Install from Polaris";
        }

        @Override
        public boolean isApplicable(final Class<? extends ToolInstallation> toolType) {
            return toolType == PolarisCliToolInstallation.class;
        }
    }

    private static final class CallableInstallerImpl implements Callable<Optional<String>, IllegalArgumentException> {
        private static final long serialVersionUID = 6457474109970149144L;
        final JenkinsIntLogger jenkinsIntLogger;
        final PolarisGlobalConfig polarisGlobalConfig;
        final FilePath installationLocation;

        public CallableInstallerImpl(final JenkinsIntLogger jenkinsIntLogger, final PolarisGlobalConfig polarisGlobalConfig, final FilePath installationLocation) {
            this.jenkinsIntLogger = jenkinsIntLogger;
            this.polarisGlobalConfig = polarisGlobalConfig;
            this.installationLocation = installationLocation;
        }

        @Override
        public Optional<String> call() throws IllegalArgumentException {
            final File installLocation = new File(installationLocation.getRemote());
            final AccessTokenPolarisHttpClient accessTokenPolarisHttpClient = polarisGlobalConfig.getPolarisServerConfig().createPolarisHttpClient(jenkinsIntLogger);
            final PolarisDownloadUtility polarisDownloadUtility = PolarisDownloadUtility.fromPolaris(jenkinsIntLogger, accessTokenPolarisHttpClient, installLocation);
            return polarisDownloadUtility.retrievePolarisCliExecutablePath();
        }

        @Override
        public void checkRoles(final RoleChecker checker) throws SecurityException {

        }
    }

}
