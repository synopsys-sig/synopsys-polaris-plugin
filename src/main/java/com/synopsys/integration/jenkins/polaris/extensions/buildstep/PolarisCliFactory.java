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

import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCli;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.Node;

public class PolarisCliFactory {

    public PolarisCli createPolarisCli(final EnvVars envVars, final Node node, final BuildListener listener, final String polarisCliName) throws IOException, InterruptedException {
        PolarisCli polarisCli = PolarisCli.findInstanceWithName(polarisCliName)
                                    .orElseThrow(() -> new AbortException(
                                        "Polaris cannot be executed: No Polaris CLI installations found. Please configure a Polaris CLI installation in the system tool configuration."));
        polarisCli = polarisCli.forEnvironment(envVars);
        polarisCli = polarisCli.forNode(node, listener);
        return polarisCli;
    }
}
