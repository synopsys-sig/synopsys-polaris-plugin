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
import com.synopsys.integration.stepworkflow.StepWorkflow;

public class ExecutePolarisCliStepWorkflow {
    private final String polarisCliName;
    private final String polarisArguments;
    private final PolarisWorkflowStepFactory polarisWorkflowStepFactory;

    public ExecutePolarisCliStepWorkflow(final String polarisCliName, final String polarisArguments, final PolarisWorkflowStepFactory polarisWorkflowStepFactory) {
        this.polarisCliName = polarisCliName;
        this.polarisArguments = polarisArguments;
        this.polarisWorkflowStepFactory = polarisWorkflowStepFactory;
    }

    public Integer perform() throws Exception {
        return StepWorkflow.first(polarisWorkflowStepFactory.createStepCreatePolarisEnvironment())
                   .then(polarisWorkflowStepFactory.createStepFindPolarisCli(polarisCliName))
                   .then(polarisWorkflowStepFactory.createStepExecutePolarisCli(polarisArguments))
                   .run()
                   .getDataOrThrowException();
    }

}
