/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.amazon.aws.spinnaker.plugin.lambda.traffic;

import com.amazon.aws.spinnaker.plugin.lambda.LambdaCloudOperationOutput;
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaBaseStrategyInput;
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaWeightedStrategyInput;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WeightedDeploymentStrategy extends BaseDeploymentStrategy<LambdaWeightedStrategyInput> {

    @Autowired
    private LambdaCloudDriverUtils utils;

    @Autowired
    CloudDriverConfigurationProperties props;

    @Override
    public LambdaCloudOperationOutput deploy(LambdaWeightedStrategyInput inp) {
        String cloudDriverUrl = props.getCloudDriverBaseUrl();
        return postToCloudDriver(inp, cloudDriverUrl, utils);
    }

    @Override
    public LambdaWeightedStrategyInput setupInput(StageExecution stage) {
        LambdaWeightedStrategyInput aliasInp = utils.getInput(stage, LambdaWeightedStrategyInput.class);
        aliasInp.setCredentials(aliasInp.getAccount());
        aliasInp.setAppName(stage.getExecution().getApplication());
        aliasInp.setWeightToMinorFunctionVersion((double)(100 - aliasInp.getTrafficPercentA()) / 100);
        aliasInp.setMajorFunctionVersion(getVersion(stage, aliasInp.getVersionNameA(), aliasInp.getVersionNumberA()));
        aliasInp.setMinorFunctionVersion(getVersion(stage, aliasInp.getVersionNameB(), aliasInp.getVersionNumberB()));
        return aliasInp;
    }

    @Override
    public LambdaCloudDriverUtils getUtils() {
        return utils;
    };
}
