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
import com.amazon.aws.spinnaker.plugin.lambda.upsert.model.LambdaUpdateConfigInput;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverResponse;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaStageConstants;
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

import java.util.Map;

@Component
public class LambdaUpdateConfigurationTask implements LambdaStageBaseTask {
    private static Logger logger = LoggerFactory.getLogger(LambdaUpdateCodeTask.class);
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
        Boolean justCreated = (Boolean)stage.getContext().getOrDefault(LambdaStageConstants.lambaCreatedKey, Boolean.FALSE);
        if (justCreated) {
            return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(stage.getContext()).build();
        }
        LambdaCloudOperationOutput output = this.updateLambdaConfig(stage);
        Map<String, Object> context = buildContextOutput(output,  LambdaStageConstants.updateConfigUrlKey);
        context.put(LambdaStageConstants.lambaConfigurationUpdatedKey, Boolean.TRUE);
        return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(context).build();
    }

    private LambdaCloudOperationOutput updateLambdaConfig(StageExecution stage) {
        LambdaUpdateConfigInput inp = utils.getInput(stage, LambdaUpdateConfigInput.class);
        inp.setAppName(stage.getExecution().getApplication());
        inp.setCredentials(inp.getAccount());
        String rawString = utils.asString(inp);
        String endPoint = cloudDriverUrl + CLOUDDRIVER_UPDATE_CONFIG_PATH;
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
