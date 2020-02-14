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

import org.kohsuke.stapler.DataBoundConstructor;

import com.synopsys.integration.jenkins.annotations.HelpMarkdown;
import com.synopsys.integration.jenkins.extensions.ChangeBuildStatusTo;
import com.synopsys.integration.jenkins.extensions.JenkinsSelectBoxEnum;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;

public class WaitForIssues extends AbstractDescribableImpl<WaitForIssues> {
    @HelpMarkdown("The build status to set the project to if there are issues")
    private final ChangeBuildStatusTo buildStatusForIssues;

    @DataBoundConstructor
    public WaitForIssues(final ChangeBuildStatusTo buildStatusForIssues) {
        this.buildStatusForIssues = buildStatusForIssues;
    }

    public ChangeBuildStatusTo getBuildStatusForIssues() {
        return buildStatusForIssues;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<WaitForIssues> {
        public ListBoxModel doFillBuildStatusForIssuesItems() {
            return JenkinsSelectBoxEnum.toListBoxModel(ChangeBuildStatusTo.values());
        }
    }
}
