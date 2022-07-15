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
import com.amazon.aws.spinnaker.plugin.lambda.upsert.model.LambdaConcurrencyInput;
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

import java.util.Optional;

@Component
public class LambdaDeleteConcurrencyTask implements LambdaStageBaseTask {
    private static Logger logger = LoggerFactory.getLogger(LambdaDeleteConcurrencyTask.class);
    private static final ObjectMapper objMapper = new ObjectMapper();

    private static String CLOUDDRIVER_DELETE_PROVISIONED_CONCURRENCY_PATH = "/aws/ops/deleteLambdaProvisionedConcurrency";
    private static String CLOUDDRIVER_DELETE_RESERVED_CONCURRENCY_PATH = "/aws/ops/deleteLambdaReservedConcurrency";

    @Autowired
    CloudDriverConfigurationProperties props;

    @Autowired
    private LambdaCloudDriverUtils utils;
    private  String cloudDriverUrl;

    @NotNull
    @Override
    public TaskResult execute(@NotNull StageExecution stage) {
        logger.debug("Executing LambdaDeleteConcurrencyTask...");
        cloudDriverUrl = props.getCloudDriverBaseUrl();
        prepareTask(stage);
        LambdaConcurrencyInput inp = utils.getInput(stage, LambdaConcurrencyInput.class);
        inp.setAppName(stage.getExecution().getApplication());

        LambdaCloudOperationOutput output;
        if (stage.getType().equals("Aws.LambdaDeploymentStage") && inp.getReservedConcurrentExecutions() == null) {
            output = deleteReservedConcurrency(inp);
        } else if (stage.getType().equals("Aws.LambdaTrafficRoutingStage") && Optional.ofNullable(inp.getProvisionedConcurrentExecutions()).orElse(0) == 0
                && !"$WEIGHTED".equals(stage.getContext().get("deploymentStrategy"))) {
            output = deleteProvisionedConcurrency(inp);
        } else {
            addToOutput(stage, "LambdaDeleteConcurrencyTask" , "Lambda delete concurrency : nothing to delete");
            return taskComplete(stage);
        }
        addCloudOperationToContext(stage, output, LambdaStageConstants.deleteConcurrencyUrlKey);
        return taskComplete(stage);
    }

    private LambdaCloudOperationOutput deleteProvisionedConcurrency(LambdaConcurrencyInput inp) {
        inp.setQualifier(inp.getAliasName());
        String rawString = utils.asString(inp);
        String endPoint = cloudDriverUrl + CLOUDDRIVER_DELETE_PROVISIONED_CONCURRENCY_PATH;
        LambdaCloudDriverResponse respObj = utils.postToCloudDriver(endPoint, rawString);
        String url = cloudDriverUrl + respObj.getResourceUri();
        logger.debug("Posted to cloudDriver for deleteProvisionedConcurrency: " + url);
        LambdaCloudOperationOutput operationOutput = LambdaCloudOperationOutput.builder().resourceId(respObj.getId()).url(url).build();
        return operationOutput;
    }

    private LambdaCloudOperationOutput deleteReservedConcurrency(LambdaConcurrencyInput inp) {
        String rawString = utils.asString(inp);
        String endPoint = cloudDriverUrl + CLOUDDRIVER_DELETE_RESERVED_CONCURRENCY_PATH;
        LambdaCloudDriverResponse respObj = utils.postToCloudDriver(endPoint, rawString);
        String url = cloudDriverUrl + respObj.getResourceUri();
        logger.debug("Posted to cloudDriver for deleteReservedConcurrency: " + url);
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
