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

import com.amazon.aws.spinnaker.plugin.lambda.LambdaStageBaseTask;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaDefinition;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaStageConstants;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Map;

@Component
public class LambdaWaitToStabilizeTask implements LambdaStageBaseTask {
    private static Logger logger = LoggerFactory.getLogger(LambdaWaitToStabilizeTask.class);

    final String PENDING_STATE = "Pending";
    final String ACTIVE_STATE = "Active";
    final String FUNCTION_CREATING = "Creating";

    @Autowired
    CloudDriverConfigurationProperties props;

    @Autowired
    private LambdaCloudDriverUtils utils;

    @Nonnull
    @Override
    public TaskResult execute(@Nonnull StageExecution stage) {
        logger.debug("Executing LambdaWaitForFunctionToStabilizeTask...");
        return waitForStableState(stage);
    }

    private TaskResult waitForStableState(@NotNull StageExecution stage) {
        LambdaDefinition lf = null;
        while(true) {
            lf = utils.findLambda(stage);
            if (lf != null) {
                logger.info(String.format("lambda state %s", lf.getState()));
                if (lf.getState().equals(PENDING_STATE) && lf.getStateReasonCode().equals(FUNCTION_CREATING)) {
                    utils.await(10000);
                    continue;
                }
                if (lf.getState().equals(ACTIVE_STATE)) {
                    return taskComplete(stage);
                }
            }
            break;
        }
        return this.formErrorTaskResult(
                stage,
                String.format(
                        "Failed to stabilize function with state: %s and reason: %s",
                        lf != null ? lf.getState() : "Unknown",
                        lf != null ? lf.getStateReason() : "Unknown reason"
                )
        );
    }
}
