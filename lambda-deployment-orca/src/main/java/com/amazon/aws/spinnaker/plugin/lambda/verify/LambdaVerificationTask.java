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

package com.amazon.aws.spinnaker.plugin.lambda.verify;

import com.amazon.aws.spinnaker.plugin.lambda.LambdaStageBaseTask;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaStageConstants;
import com.amazon.aws.spinnaker.plugin.lambda.verify.model.LambdaCloudDriverTaskResults;
import com.netflix.spinnaker.orca.api.pipeline.Task;
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

import java.util.*;
import java.util.stream.Collectors;

@Component
public class LambdaVerificationTask implements LambdaStageBaseTask {
    private static Logger logger = LoggerFactory.getLogger(LambdaVerificationTask.class);

    @Autowired
    CloudDriverConfigurationProperties props;
    private  String cloudDriverUrl;

    @Autowired
    private LambdaCloudDriverUtils utils;

    @NotNull
    @Override
    public TaskResult execute(@NotNull StageExecution stage) {
        logger.debug("Executing lambdaVerificationTask...");
        Map<String, Object> stageContext = stage.getContext();

        List<String> urlKeyList = LambdaStageConstants.allUrlKeys;

        List<String> urlList = urlKeyList.stream().filter(x -> {
            return (String)stageContext.get(x) != null ;
        }).map(singleUrlKey ->  {
            return (String)stageContext.get(singleUrlKey);
        }).collect(Collectors.toList());

        if (null != stageContext.get(LambdaStageConstants.eventTaskKey))
            urlList.addAll((List<String>)stageContext.get(LambdaStageConstants.eventTaskKey));

        List<LambdaCloudDriverTaskResults> listOfTaskResults = urlList.stream().map(url -> {
            return utils.verifyStatus(url);
        }).collect(Collectors.toList());

        boolean anyRunning = listOfTaskResults.stream().anyMatch( taskResult -> { return !taskResult.getStatus().isCompleted(); });
        if (anyRunning) {
            return TaskResult.builder(ExecutionStatus.RUNNING).build();
        }

        boolean anyFailures = listOfTaskResults.stream().anyMatch(taskResult -> {
            return taskResult.getStatus().isFailed();
        });

        if (!anyFailures) {
            final Map<String, Object> outputMap = new HashMap<String, Object>();
            listOfTaskResults.stream().forEach(taskResult -> {
                if (taskResult.getResults() != null) {
                    String arn = taskResult.getResults().getFunctionArn();
                    if (arn != null) {
                        outputMap.put("functionARN", arn);
                        outputMap.put("resourceId", arn);
                        outputMap.put("url", taskResult.getResults().getFunctionName());
                        outputMap.put("functionName", taskResult.getResults().getFunctionName());
                    }
                }
            });
            stage.getOutputs().putAll(outputMap);
            return TaskResult.builder(ExecutionStatus.SUCCEEDED).outputs(outputMap).build();
        }

        // Process failures:
        List<String> errorMessages = listOfTaskResults.stream().map(op -> {
            return op.getErrors().getMessage();
        }).collect(Collectors.toList());
        return formErrorListTaskResult(stage, errorMessages);
    }

    @Nullable
    @Override
    public TaskResult onTimeout(@NotNull StageExecution stage) {
        return TaskResult.builder(ExecutionStatus.SKIPPED).build();
    }

    @Override
    public void onCancel(@NotNull StageExecution stage) {
    }

    @Override
    public Collection<String> aliases() {
        List<String> ss = new ArrayList<String>();
        ss.add("lambdaVerificationTask");
        return ss;
    }
}
