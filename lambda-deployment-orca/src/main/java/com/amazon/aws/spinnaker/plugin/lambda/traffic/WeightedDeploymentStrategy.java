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
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaDeploymentStrategyOutput;
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaTrafficUpdateInput;
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaWeightedStrategyInput;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WeightedDeploymentStrategy extends BaseDeploymentStrategy<LambdaWeightedStrategyInput> {

    @Autowired
    private LambdaCloudDriverUtils utils;

    @Autowired
    CloudDriverConfigurationProperties props;

    @Override
    public LambdaDeploymentStrategyOutput deploy(LambdaWeightedStrategyInput inp) {
        String cloudDriverUrl = props.getCloudDriverBaseUrl();
        Map<String, Object> outputMap  = new HashMap<>();
        outputMap.put("deployment:majorVersionDeployed", inp.getMajorFunctionVersion());
        outputMap.put("deployment:minorVersionDeployed", inp.getMinorFunctionVersion());
        outputMap.put("deployment:aliasDeployed", inp.getAliasName());
        outputMap.put("deployment:strategyUsed", "WeightedDeploymentStrategy");
        // TODO: Form a new inputObject such as SimpleStrategyInput and just have the
        LambdaCloudOperationOutput out = postToCloudDriver(inp, cloudDriverUrl, utils);
        out.setOutputMap(outputMap);
        LambdaDeploymentStrategyOutput deployOutput =LambdaDeploymentStrategyOutput.builder().build();
        deployOutput.setSucceeded(true);
        deployOutput.setOutput(out);
        return deployOutput;
    }

    @Override
    public LambdaWeightedStrategyInput setupInput(StageExecution stage) {
        LambdaTrafficUpdateInput aliasInp = utils.getInput(stage, LambdaTrafficUpdateInput.class);
        LambdaWeightedStrategyInput weightedInput = utils.getInput(stage, LambdaWeightedStrategyInput.class);
        weightedInput.setAppName(stage.getExecution().getApplication());
        weightedInput.setAccount(aliasInp.getAccount());
        weightedInput.setCredentials(aliasInp.getAccount());

        weightedInput.setWeightToMinorFunctionVersion((double)(100 - aliasInp.getTrafficPercentA()) / 100);
        weightedInput.setMajorFunctionVersion(getVersion(stage, aliasInp.getVersionNameA(), aliasInp.getVersionNumberA()));
        weightedInput.setMinorFunctionVersion(getVersion(stage, aliasInp.getVersionNameB(), aliasInp.getVersionNumberB()));
        return weightedInput;
    }

    @Override
    public LambdaCloudDriverUtils getUtils() {
        return utils;
    }
}
