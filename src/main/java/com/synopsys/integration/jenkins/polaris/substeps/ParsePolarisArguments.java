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

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.types.Commandline;

import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.polaris.extensions.tools.PolarisCliToolInstallation;
import com.synopsys.integration.stepworkflow.AbstractSupplyingSubStep;
import com.synopsys.integration.stepworkflow.SubStepResponse;
import com.synopsys.integration.util.IntEnvironmentVariables;

import hudson.Util;
import hudson.util.ArgumentListBuilder;

public class ParsePolarisArguments extends AbstractSupplyingSubStep<ArgumentListBuilder> {
    private static final String LOGGING_LEVEL_KEY = "logging.level.com.synopsys.integration";
    private final JenkinsIntLogger logger;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final PolarisCliToolInstallation polarisCliToolInstallation;
    private final String polarisArguments;

    public ParsePolarisArguments(final JenkinsIntLogger logger, final IntEnvironmentVariables intEnvironmentVariables, final PolarisCliToolInstallation polarisCliToolInstallation, final String polarisArguments) {
        this.logger = logger;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.polarisCliToolInstallation = polarisCliToolInstallation;
        this.polarisArguments = polarisArguments;
    }

    @Override
    public SubStepResponse<ArgumentListBuilder> run() {
        final ArgumentListBuilder args = new ArgumentListBuilder();

        if (StringUtils.isNotBlank(polarisArguments)) {
            Arrays.stream(Commandline.translateCommandline(polarisArguments))
                .map(argumentBlobString -> argumentBlobString.split("\\r?\\n"))
                .flatMap(Arrays::stream)
                .filter(StringUtils::isNotBlank)
                .map(this::handleVariableReplacement)
                .forEachOrdered(args::add);
        }

        if (args.toList().stream().noneMatch(argument -> argument.contains(LOGGING_LEVEL_KEY))) {
            args.add(String.format("--%s=%s", LOGGING_LEVEL_KEY, logger.getLogLevel().toString()));
        }

        logger.info("Running Polaris CLI with arguments: " + StringUtils.join(args, " "));

        return SubStepResponse.SUCCESS(args);
    }

    private String handleVariableReplacement(final String value) {
        if (value != null) {
            final String newValue = Util.replaceMacro(value, intEnvironmentVariables.getVariables());
            if (StringUtils.isNotBlank(newValue) && newValue.contains("$")) {
                logger.warn("Variable may not have been properly replaced. Argument: " + value + ", resolved argument: " + newValue + ". Make sure the variable has been properly defined.");
            }
            return newValue;
        }
        return null;
    }

}
