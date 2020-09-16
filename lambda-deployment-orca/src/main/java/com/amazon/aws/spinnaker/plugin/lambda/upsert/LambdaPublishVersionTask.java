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
import com.amazon.aws.spinnaker.plugin.lambda.upsert.model.LambdaPublisVersionInput;
import com.amazon.aws.spinnaker.plugin.lambda.upsert.model.LambdaUpdateConfigInput;
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
public class LambdaPublishVersionTask implements LambdaStageBaseTask {
    private static Logger logger = LoggerFactory.getLogger(LambdaPublishVersionTask.class);
    private static final ObjectMapper objMapper = new ObjectMapper();
    private static String CLOUDDRIVER_PUBLISH_VERSION_PATH = "/aws/ops/publishLambdaFunctionVersion";

    @Autowired
    CloudDriverConfigurationProperties props;
    private  String cloudDriverUrl;

    @Autowired
    private LambdaCloudDriverUtils utils;

    @NotNull
    @Override
    public TaskResult execute(@NotNull StageExecution stage) {
        cloudDriverUrl = props.getCloudDriverBaseUrl();
        if (!requiresVersionPublish(stage)) {
            return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(stage.getContext()).build();
        }

        // TODO: Commented out until cloud-driver is merged with the new atomic operation.
        // LambdaCloudOperationOutput output = this.publishVersion(stage);
        // Map<String, Object> context = buildContextOutput(output,  LambdaStageConstants.publishVersionUrlKey);
        // context.put(LambdaStageConstants.lambaVersionPublishedKey, Boolean.TRUE);
        Map<String, Object> context = new HashMap<>();
        return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(context).build();
    }

    private boolean requiresVersionPublish(StageExecution stage) {
        Boolean justCreated = (Boolean)stage.getContext().getOrDefault(LambdaStageConstants.lambaCreatedKey, Boolean.FALSE);
        if (justCreated)
            return false;
        LambdaGetInput lgi = utils.getInput(stage, LambdaGetInput.class);
        lgi.setAppName(stage.getExecution().getApplication());
        LambdaGetOutput lf = utils.retrieveLambda(lgi);
        String newRevisionId = lf.getRevisionId();
        String origRevisionId = (String)stage.getContext().get(LambdaStageConstants.originalRevisionIdKey);
        stage.getContext().put(LambdaStageConstants.newRevisionIdKey, newRevisionId);
        return !newRevisionId.equals(origRevisionId);
        //Boolean configUpdated = (Boolean)stage.getContext().getOrDefault(LambdaStageConstants.lambaConfigurationUpdatedKey, Boolean.FALSE);
        //Boolean codeUpdated = (Boolean)stage.getContext().getOrDefault(LambdaStageConstants.lambaCodeUpdatedKey, Boolean.FALSE);
        //return  (!justCreated); //  && !codeUpdated || configUpdated);
    }

    private LambdaCloudOperationOutput publishVersion(StageExecution stage) {
        LambdaPublisVersionInput inp = utils.getInput(stage, LambdaPublisVersionInput.class);
        inp.setAppName(stage.getExecution().getApplication());
        inp.setCredentials(inp.getAccount());
        String rawString = utils.asString(inp);
        String endPoint = cloudDriverUrl + CLOUDDRIVER_PUBLISH_VERSION_PATH;
        String revisionId = (String)stage.getContext().get(LambdaStageConstants.newRevisionIdKey);
        inp.setRegion(revisionId);
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
