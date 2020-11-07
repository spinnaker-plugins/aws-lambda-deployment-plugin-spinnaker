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
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverResponse;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaStageConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LambdaUpdateConfigurationTask implements LambdaStageBaseTask {
    private static Logger logger = LoggerFactory.getLogger(LambdaUpdateConfigurationTask.class);
    private static final ObjectMapper objMapper = new ObjectMapper();
    private static String CLOUDDRIVER_UPDATE_CONFIG_PATH = "/aws/ops/updateLambdaFunctionConfiguration";

    private final CloudDriverConfigurationProperties props;
    private String cloudDriverUrl;

    private final LambdaCloudDriverUtils utils;

    @NotNull
    @Override
    public TaskResult execute(@NotNull StageExecution stage) {
        logger.debug("Executing LambdaUpdateConfigurationTask...");
        cloudDriverUrl = props.getCloudDriverBaseUrl();
        prepareTask(stage);
        Boolean justCreated = (Boolean)stage.getContext().getOrDefault(LambdaStageConstants.lambaCreatedKey, Boolean.FALSE);
        if (justCreated) {
            return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(stage.getContext()).build();
        }
        List<String> errors = new ArrayList<>();
        LambdaDeploymentInput ldi = utils.getInput(stage, LambdaDeploymentInput.class);
        if (!utils.validateUpsertLambdaInput(ldi, errors)) {
            return this.formErrorListTaskResult(stage, errors);
        }
        LambdaCloudOperationOutput output = this.updateLambdaConfig(stage, ldi);
        addCloudOperationToContext(stage, output,  LambdaStageConstants.updateConfigUrlKey);
        addToTaskContext(stage, LambdaStageConstants.lambaConfigurationUpdatedKey, Boolean.TRUE);
        return taskComplete(stage);
    }

    private LambdaCloudOperationOutput updateLambdaConfig(StageExecution stage,  LambdaDeploymentInput ldi ) {
        ldi.setAppName(stage.getExecution().getApplication());
        ldi.setCredentials(ldi.getAccount());
        String rawString = utils.asString(ldi);
        String endPoint = cloudDriverUrl + CLOUDDRIVER_UPDATE_CONFIG_PATH;
        LambdaCloudDriverResponse respObj = utils.postToCloudDriver(endPoint, rawString);
        String url = cloudDriverUrl + respObj.getResourceUri();
        logger.debug("Posted to cloudDriver for updateLambdaConfig: " + url);
        LambdaCloudOperationOutput operationOutput = LambdaCloudOperationOutput.builder().resourceId(respObj.getId()).url(url).build();
        return operationOutput;
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
