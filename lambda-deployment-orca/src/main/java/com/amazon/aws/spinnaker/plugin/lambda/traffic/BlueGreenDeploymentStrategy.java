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
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaBlueGreenStrategyInput;
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaInvokeFunctionOutput;
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaSimpleStrategyInput;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverResponse;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaGetOutput;
import com.amazon.aws.spinnaker.plugin.lambda.verify.model.LambdaCloudDriverTaskResults;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BlueGreenDeploymentStrategy extends BaseDeploymentStrategy<LambdaBlueGreenStrategyInput> {

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
                Thread.sleep(sleepTime);
                timeout -= sleepTime;
            }
            catch (Exception e) {
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
        // TODO: Form a new inputobject such as SimpleStrategyInput and just have the
        return postToCloudDriver(inp, cloudDriverUrl, utils);
    }

    @Override
    public LambdaBlueGreenStrategyInput setupInput(StageExecution stage) {
        LambdaBlueGreenStrategyInput aliasInp = utils.getInput(stage, LambdaBlueGreenStrategyInput.class);
        aliasInp.setCredentials(aliasInp.getAccount());
        aliasInp.setAppName(stage.getExecution().getApplication());
        System.out.println(aliasInp.getLambdaPayload());
        LambdaGetOutput lf = null;
        lf = utils.findLambda(stage, true);
        String qual = utils.getCanonicalVersion(lf, "$LATEST", "", 1);
        aliasInp.setQualifier(qual);
        String latestVersion = this.getVersion(stage, "$LATEST", "");
        aliasInp.setLatestVersionQualifier(latestVersion);
        return aliasInp;
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
