/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package com.amazon.aws.spinnaker.plugin.lambda.delete;

import com.amazon.aws.spinnaker.plugin.lambda.LambdaStageBaseTask;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.amazon.aws.spinnaker.plugin.lambda.verify.model.LambdaCloudDriverTaskResults;
import com.netflix.spinnaker.orca.api.pipeline.Task;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LambdaDeleteVerificationTask implements LambdaStageBaseTask {

    private static final Logger logger = LoggerFactory.getLogger(LambdaDeleteVerificationTask.class);

    @Autowired
    CloudDriverConfigurationProperties props;

    @Autowired
    private LambdaCloudDriverUtils utils;

    @NotNull
    @Override
    public TaskResult execute(@NotNull StageExecution stage) {
        Map<String, Object> stageContext = stage.getContext();
        String url = (String)stageContext.get("url");
        LambdaCloudDriverTaskResults op = utils.verifyStatus(url);
        if (!op.getStatus().isCompleted()) {
                return TaskResult.builder(ExecutionStatus.RUNNING).build();
        }
        if (op.getStatus().isFailed()) {
            List<String> allMessages = Arrays.asList(op.getErrors().getMessage());
            return formErrorListTaskResult(stage, allMessages);
        }
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("message", "Lambda (Version) Deletion succeeded");
        // TODO: Put exact version numbers that were deleted here...
        stage.getOutputs().putAll(outputMap);
        return TaskResult.builder(ExecutionStatus.SUCCEEDED).outputs(outputMap).build();
    }
}
