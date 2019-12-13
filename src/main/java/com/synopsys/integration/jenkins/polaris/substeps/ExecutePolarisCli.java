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

import com.synopsys.integration.stepworkflow.SubStep;
import com.synopsys.integration.stepworkflow.SubStepResponse;
import com.synopsys.integration.util.IntEnvironmentVariables;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

public class ExecutePolarisCli implements SubStep<ArgumentListBuilder, Integer> {
    private final Launcher launcher;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final FilePath workspace;
    private final TaskListener listener;

    public ExecutePolarisCli(final Launcher launcher, final IntEnvironmentVariables intEnvironmentVariables, final FilePath workspace, final TaskListener listener) {
        this.launcher = launcher;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.workspace = workspace;
        this.listener = listener;
    }

    @Override
    public SubStepResponse<Integer> run(final SubStepResponse<? extends ArgumentListBuilder> previousResponse) {
        try {
            if (previousResponse.isSuccess() && previousResponse.hasData()) {
                final Integer exitCode = launcher.launch()
                                             .cmds(previousResponse.getData())
                                             .envs(intEnvironmentVariables.getVariables())
                                             .pwd(workspace)
                                             .stdout(listener)
                                             .quiet(true)
                                             .join();

                return SubStepResponse.SUCCESS(exitCode);
            } else {
                return SubStepResponse.FAILURE(previousResponse);
            }
        } catch (final Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return SubStepResponse.FAILURE(e);
        }
    }
}
