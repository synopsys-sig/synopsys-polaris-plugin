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
package com.synopsys.integration.jenkins.polaris.extensions.pipeline;

import com.synopsys.integration.jenkins.polaris.workflow.PolarisWorkflowStepFactory;

import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Node;

public class PolarisPipelineWorkflow {
    private final PolarisWorkflowStepFactory polarisWorkflowStepFactory;
    private final Node node;
    private final FilePath workspace;

    public PolarisPipelineWorkflow(final PolarisWorkflowStepFactory polarisWorkflowStepFactory, final Node node, final FilePath workspace) {
        this.polarisWorkflowStepFactory = polarisWorkflowStepFactory;
        this.node = node;
        this.workspace = workspace;
    }

    public Integer perform() throws Exception {
        validate();
        return polarisWorkflowStepFactory.createStepWorkflowBuilder(polarisWorkflowStepFactory.createStepCreatePolarisEnvironment())
                   .then(polarisWorkflowStepFactory.createStepFindPolarisCli())
                   .then(polarisWorkflowStepFactory.createStepExecutePolarisCli())
                   .run()
                   .getDataOrThrowException();
    }

    private void validate() throws AbortException {
        if (node == null) {
            throw new AbortException("Polaris cannot be executed: The node that it was executed on no longer exists.");
        }
        if (workspace == null) {
            throw new AbortException("Polaris cannot be executed: The workspace could not be determined.");
        }
    }
}
