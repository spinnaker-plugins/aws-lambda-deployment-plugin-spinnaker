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

package com.amazon.aws.spinnaker.plugin.lambda.upsert;

import com.amazon.aws.spinnaker.plugin.lambda.LambdaCloudOperationOutput;
import com.amazon.aws.spinnaker.plugin.lambda.LambdaStageBaseTask;
import com.amazon.aws.spinnaker.plugin.lambda.upsert.model.LambdaDeploymentInput;
import com.amazon.aws.spinnaker.plugin.lambda.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LambdaCreateTask implements LambdaStageBaseTask {
    private static Logger logger = LoggerFactory.getLogger(LambdaCreateTask.class);
    private static final ObjectMapper objMapper = new ObjectMapper();
    private static String CLOUDDRIVER_CREATE_PATH = "/aws/ops/createLambdaFunction";
    private static String CLOUDDRIVER_UPDATE_CODE_PATH = "/aws/ops/updateLambdaFunctionCode";
    private static String CLOUDDRIVER_UPDATE_CONFIG_PATH = "/aws/ops/updateLambdaFunctionConfiguration";


    @Autowired
    CloudDriverConfigurationProperties props;
    private  String cloudDriverUrl;

    @Autowired
    private LambdaCloudDriverUtils utils;

    @NotNull
    @Override
    public TaskResult execute(@NotNull StageExecution stage) {
        cloudDriverUrl = props.getCloudDriverBaseUrl();
        logger.debug("Executing LambdaDeploymentTask...");
        LambdaDeploymentInput ldi = utils.getInput(stage, LambdaDeploymentInput.class);
        ldi.setAppName(stage.getExecution().getApplication());
        LambdaGetInput lgi = utils.getInput(stage, LambdaGetInput.class);
        lgi.setAppName(stage.getExecution().getApplication());
        Map<String, Object> context = null;
        LambdaGetOutput lf = utils.retrieveLambda(lgi);
        if (lf != null) {
            logger.debug("noOp. Lambda already exists. only needs updating.");
            context = new HashMap<String, Object>();
            context.put(LambdaStageConstants.lambaCreatedKey, Boolean.FALSE);
            context.put(LambdaStageConstants.lambdaObjectKey, lf);
            context.put(LambdaStageConstants.originalRevisionIdKey, lf.getRevisionId());
            return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(context).build();
        }
        LambdaCloudOperationOutput output = createLambda(stage);
        context = buildContextOutput(output, LambdaStageConstants.createdUrlKey);
        context.put(LambdaStageConstants.lambaCreatedKey, Boolean.TRUE);
        return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(context).build();
    }

    private LambdaCloudOperationOutput createLambda(StageExecution stage) {
        LambdaDeploymentInput ldi = utils.getInput(stage, LambdaDeploymentInput.class);
        ldi.setAppName(stage.getExecution().getApplication());
        ldi.setCredentials(ldi.getAccount());
        String endPoint = cloudDriverUrl + CLOUDDRIVER_CREATE_PATH ;
        String rawString = utils.asString(ldi);
        LambdaCloudDriverResponse respObj = utils.postToCloudDriver(endPoint, rawString);
        String url = cloudDriverUrl + respObj.getResourceUri();
        LambdaCloudOperationOutput xx = LambdaCloudOperationOutput.builder().resourceId(respObj.getId()).url(url).build();
        return xx;
    }

    @Nullable
    @Override
    public TaskResult onTimeout(@NotNull StageExecution stage) {
        return TaskResult.builder(ExecutionStatus.SKIPPED).build();
    }

    @Override
    public void onCancel(@NotNull StageExecution stage) {
    }

}
