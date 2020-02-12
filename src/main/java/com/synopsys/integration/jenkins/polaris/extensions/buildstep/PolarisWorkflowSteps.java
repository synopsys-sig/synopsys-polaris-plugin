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

import com.synopsys.integration.jenkins.polaris.substeps.CreatePolarisEnvironment;
import com.synopsys.integration.jenkins.polaris.substeps.ExecutePolarisCli;
import com.synopsys.integration.stepworkflow.jenkins.RemoteSubStep;

public class PolarisWorkflowSteps {
    final CreatePolarisEnvironment createPolarisEnvironment;
    final RemoteSubStep<String> findPolarisCli;
    final ExecutePolarisCli executePolarisCli;

    public PolarisWorkflowSteps(final CreatePolarisEnvironment createPolarisEnvironment, final RemoteSubStep<String> findPolarisCli, final ExecutePolarisCli executePolarisCli) {
        this.createPolarisEnvironment = createPolarisEnvironment;
        this.findPolarisCli = findPolarisCli;
        this.executePolarisCli = executePolarisCli;
    }

    public CreatePolarisEnvironment getCreatePolarisEnvironment() {
        return createPolarisEnvironment;
    }

    public RemoteSubStep<String> getFindPolarisCli() {
        return findPolarisCli;
    }

    public ExecutePolarisCli getExecutePolarisCli() {
        return executePolarisCli;
    }
}
