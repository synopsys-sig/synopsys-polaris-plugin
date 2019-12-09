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
import hudson.tools.ToolProperty;
import hudson.tools.ToolPropertyDescriptor;
import hudson.util.DescribableList;
import hudson.util.FormValidation;

public class PolarisCliToolInstallation extends ToolInstallation implements NodeSpecific<PolarisCliToolInstallation>, EnvironmentSpecific<PolarisCliToolInstallation> {
    private static final long serialVersionUID = -3838254855454518440L;

    @DataBoundConstructor
    public PolarisCliToolInstallation(final String name, final String home) {
        super(name, home, new DescribableList<ToolProperty<?>, ToolPropertyDescriptor>(Saveable.NOOP));
    }

    @Override
    public PolarisCliToolInstallation forNode(@Nonnull final Node node, final TaskListener log) throws IOException, InterruptedException {
        return new PolarisCliToolInstallation(getName(), translateFor(node, log));
    }

    @Override
    public PolarisCliToolInstallation forEnvironment(final EnvVars environment) {
        return new PolarisCliToolInstallation(getName(), environment.expand(getHome()));
    }

    /**
     * {@link ToolDescriptor} for {@link PolarisCliToolInstallation}
     */
    @Extension
    @Symbol("polaris-cli")
    public static final class PolarisCliToolDescriptor extends ToolDescriptor<PolarisCliToolInstallation> {
        @Override
        public String getDisplayName() {
            return "Polaris CLI";
        }

        /**
         * Override in order to remove the default "install automatically" checkbox for tool installation
         */
        @Override
        public List<ToolPropertyDescriptor> getPropertyDescriptors() {
            return Collections.emptyList();
        }

        @Override
        public DescribableList<ToolProperty<?>, ToolPropertyDescriptor> getDefaultProperties() {
            return new DescribableList<>(NOOP);
        }

        @Override
        protected FormValidation checkHomeDirectory(final File home) {
            // This validation is only ever run when on master. Jenkins does not use this to validate node overrides
            /* This is what it looks like in Coverity:
            try {
                File analysisVersionXml = new File(home, "VERSION.xml");
                if (home != null && home.exists()) {
                    if (analysisVersionXml.isFile()) {

                        // check the version file value and validate it is greater than minimum version
                        Optional<CoverityVersion> optionalVersion = getVersion(home);

                        if (!optionalVersion.isPresent()) {
                            return FormValidation.error("Could not determine the version of the Coverity analysis tool.");
                        }
                        CoverityVersion version = optionalVersion.get();
                        if (version.compareTo(CoverityPostBuildStepDescriptor.MINIMUM_SUPPORTED_VERSION) < 0) {
                            return FormValidation.error("Analysis version " + version.toString() + " detected. " +
                                                            "The minimum supported version is " + CoverityPostBuildStepDescriptor.MINIMUM_SUPPORTED_VERSION.toString());
                        }

                        return FormValidation.ok("Analysis installation directory has been verified.");
                    } else {
                        return FormValidation.error("The specified Analysis installation directory doesn't contain a VERSION.xml file.");
                    }
                } else {
                    return FormValidation.error("The specified Analysis installation directory doesn't exists.");
                }
            } catch (IOException e) {
                return FormValidation.error("Unable to verify the Analysis installation directory.");
            }
             */
            return FormValidation.ok();
        }
    }
}