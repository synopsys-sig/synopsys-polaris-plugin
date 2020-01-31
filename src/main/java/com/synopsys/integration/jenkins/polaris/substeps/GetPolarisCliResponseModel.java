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
package com.synopsys.integration.jenkins.polaris.substeps;

import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.polaris.common.cli.PolarisCliResponseUtility;
import com.synopsys.integration.polaris.common.cli.model.PolarisCliResponseModel;
import com.synopsys.integration.polaris.common.exception.PolarisIntegrationException;

import jenkins.security.MasterToSlaveCallable;

public class GetPolarisCliResponseModel extends MasterToSlaveCallable<PolarisCliResponseModel, PolarisIntegrationException> {
    private static final long serialVersionUID = -5698280934593066898L;
    private final JenkinsIntLogger logger;
    private final String workspaceRemotePath;

    public GetPolarisCliResponseModel(final JenkinsIntLogger logger, final String workspaceRemotePath) {
        this.logger = logger;
        this.workspaceRemotePath = workspaceRemotePath;
    }

    @Override
    public PolarisCliResponseModel call() throws PolarisIntegrationException {
        final PolarisCliResponseUtility polarisCliResponseUtility = PolarisCliResponseUtility.defaultUtility(logger);
        return polarisCliResponseUtility.getPolarisCliResponseModelFromDefaultLocation(workspaceRemotePath);
    }
}
