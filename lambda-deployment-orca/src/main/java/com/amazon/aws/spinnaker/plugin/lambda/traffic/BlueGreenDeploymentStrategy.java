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
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.*;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverResponse;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaDefinition;
import com.amazon.aws.spinnaker.plugin.lambda.verify.model.LambdaCloudDriverTaskResults;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BlueGreenDeploymentStrategy extends BaseDeploymentStrategy<LambdaBlueGreenStrategyInput> {
    private static final Logger logger = LoggerFactory.getLogger(BlueGreenDeploymentStrategy.class);
    @Autowired
    private LambdaCloudDriverUtils utils;

    @Autowired
    CloudDriverConfigurationProperties props;

    static String CLOUDDRIVER_INVOKE_LAMBDA_FUNCTION_PATH = "/aws/ops/invokeLambdaFunction";

    @Override
    public LambdaCloudOperationOutput deploy(LambdaBlueGreenStrategyInput inp) {
        LambdaInvokeFunctionOutput out = invokeLambdaFunction(inp);
        boolean results = verifyResults(inp, out);
        if (results) {
            return updateLambdaToLatest(inp);
        }
        return null;
    }

    private boolean verifyResults(LambdaBlueGreenStrategyInput inp, LambdaInvokeFunctionOutput output) {
        int timeout = inp.getTimeout() * 1000;
        String url = output.getUrl();
        int sleepTime = 10000;
        int count = 0;
        LambdaCloudDriverTaskResults taskResult = null;
        boolean done = false;
        while (timeout > 0) {
            taskResult = utils.verifyStatus(url);
            if (taskResult.getStatus().isCompleted()) {
                done = true;
                break;
            }
            try {
                utils.await();
                timeout -= sleepTime;
            }
            catch (Throwable e) {
                logger.error("Error waiting for blue green test to complete");
                continue;
            }
        }
        if (!done)
            return false;
        if (taskResult.getStatus().isFailed()) {
            return  false;
        }
        return true;
    }

    private LambdaCloudOperationOutput updateLambdaToLatest(LambdaBlueGreenStrategyInput inp) {
        inp.setWeightToMinorFunctionVersion(0.0);
        inp.setMajorFunctionVersion(inp.getLatestVersionQualifier());
        inp.setMinorFunctionVersion(null);
        String cloudDriverUrl = props.getCloudDriverBaseUrl();
        Map<String, Object> outputMap  = new HashMap<String, Object>();

        outputMap.put("majorVersionDeployed", inp.getMajorFunctionVersion());
        outputMap.put("aliasDeployed", inp.getAliasName());
        outputMap.put("minorVersionDeployed", "");
        outputMap.put("strategyUsed", "BlueGreenDeploymentStrategy");

        LambdaCloudOperationOutput out = postToCloudDriver(inp, cloudDriverUrl, utils);
        out.setOutputMap(outputMap);
        return out;
    }

    @Override
    public LambdaBlueGreenStrategyInput setupInput(StageExecution stage) {

        LambdaTrafficUpdateInput aliasInp = utils.getInput(stage, LambdaTrafficUpdateInput.class);
        LambdaBlueGreenStrategyInput blueGreenInput = utils.getInput(stage, LambdaBlueGreenStrategyInput.class);
        aliasInp.setAppName(stage.getExecution().getApplication());

        blueGreenInput.setCredentials(aliasInp.getAccount());
        blueGreenInput.setAppName(stage.getExecution().getApplication());

        LambdaDefinition lf = null;
        lf = utils.findLambda(stage, true);

        String qual = utils.getCanonicalVersion(lf, "$LATEST", "", 1);
        blueGreenInput.setQualifier(qual);
        String latestVersion = this.getVersion(stage, "$LATEST", "");
        blueGreenInput.setLatestVersionQualifier(latestVersion);

        return blueGreenInput;
    }

    private LambdaInvokeFunctionOutput invokeLambdaFunction(LambdaBlueGreenStrategyInput ldi) {
        ldi.setPayload(ldi.getLambdaPayload());

        String cloudDriverUrl = props.getCloudDriverBaseUrl();
        String endPoint = cloudDriverUrl + CLOUDDRIVER_INVOKE_LAMBDA_FUNCTION_PATH;
        String rawString = utils.asString(ldi);
        LambdaCloudDriverResponse respObj = utils.postToCloudDriver(endPoint, rawString);
        String url = cloudDriverUrl + respObj.getResourceUri();
        LambdaInvokeFunctionOutput ldso = LambdaInvokeFunctionOutput.builder().url(url).build();
        return ldso;
    }

    @Override
    public LambdaCloudDriverUtils getUtils() {
        return utils;
    };
}
