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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.Saveable;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import hudson.tools.ToolPropertyDescriptor;
import hudson.util.DescribableList;

public class PolarisCliToolInstallation extends ToolInstallation implements NodeSpecific<PolarisCliToolInstallation>, EnvironmentSpecific<PolarisCliToolInstallation> {
    private static final long serialVersionUID = -3838254855454518440L;
    private final String executableName;

    @DataBoundConstructor
    public PolarisCliToolInstallation(final String name, final String home, final String executableName) {
        super(name, home, new DescribableList<ToolProperty<?>, ToolPropertyDescriptor>(Saveable.NOOP));
        this.executableName = executableName;
    }

    @Override
    public PolarisCliToolInstallation forNode(@Nonnull final Node node, final TaskListener log) throws IOException, InterruptedException {
        return new PolarisCliToolInstallation(getName(), translateFor(node, log), getExecutableName());
    }

    public String getExecutableName() {
        return executableName;
    }

    @Override
    public PolarisCliToolInstallation forEnvironment(final EnvVars environment) {
        return new PolarisCliToolInstallation(getName(), environment.expand(getHome()), getExecutableName());
    }

    @Override
    public void buildEnvVars(final EnvVars env) {
        env.putIfNotNull("PATH+POLARIS", getHome());
    }

    @Extension
    @Symbol("polaris-cli")
    public static final class DescriptorImpl extends ToolDescriptor<PolarisCliToolInstallation> {
        @Override
        public String getDisplayName() {
            return "Polaris CLI";
        }

        @Override
        public List<? extends ToolInstaller> getDefaultInstallers() {
            return Collections.singletonList(new PolarisCliToolInstaller(null));
        }
    }

}