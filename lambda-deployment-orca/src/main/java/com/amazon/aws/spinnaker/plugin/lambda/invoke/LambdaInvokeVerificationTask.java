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

package com.amazon.aws.spinnaker.plugin.lambda.invoke;

import com.amazon.aws.spinnaker.plugin.lambda.LambdaStageBaseTask;
import com.amazon.aws.spinnaker.plugin.lambda.invoke.model.LambdaInvokeStageInput;
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaCloudDriverInvokeOperationResults;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.amazon.aws.spinnaker.plugin.lambda.verify.model.LambdaCloudDriverTaskResults;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class LambdaInvokeVerificationTask implements LambdaStageBaseTask {

    private static final Logger logger = LoggerFactory.getLogger(LambdaInvokeVerificationTask.class);

    @Autowired
    CloudDriverConfigurationProperties props;

    @Autowired
    private LambdaCloudDriverUtils utils;

    @NotNull
    @Override
    public TaskResult execute(@NotNull StageExecution stage) {
        prepareTask(stage);
        Map<String, Object> stageContext = stage.getContext();
        List<String> urlList = (List<String>)stageContext.get("urlList");
        if (urlList == null) {
            return taskComplete(stage);
        }
        List<Pair<String, LambdaCloudDriverTaskResults>> results = urlList.stream().map(url -> {
            return Pair.of(url, utils.verifyStatus(url));
        }).collect(Collectors.toList());

        boolean anyNotDone = results.stream().anyMatch(x -> {
           return !x.getRight().getStatus().isComplete();
        });

        if (anyNotDone) {
            return TaskResult.builder(ExecutionStatus.RUNNING).build();
        }

        boolean anyFailed = results.stream().anyMatch(x -> {
            return !x.getRight().getStatus().isFailed();
        });

        List<String> allErrors = new ArrayList<String>();
        if (anyFailed) {
            results.stream().forEach(op -> {
                if (op.getRight().getStatus().isFailed()) {
                    List<String> allMessages = Arrays.asList(op.getRight().getErrors().getMessage());
                    if ((allMessages != null) && allMessages.size() > 0) {
                        allErrors.addAll(allMessages);
                    }
                }
            });
        }

        List<Map<String, Object>> invokeResultsList = new ArrayList<Map<String, Object>>();
        results.stream().forEach(x -> {
            LambdaInvokeStageInput ldi = utils.getInput(stage, LambdaInvokeStageInput.class);
            Map<String, Object> invokeResults = this.verifyInvokeResults(x.getLeft(), ldi.getTimeout());
            invokeResultsList.add(invokeResults);
        });

        addToOutput(stage, "invokeResultsList", invokeResultsList);

        if (allErrors.size() > 0) {
            return formErrorListTaskResult(stage, allErrors);
        }
        return taskComplete(stage);
    }

    private Map<String, Object> verifyInvokeResults(String url, int seconds) {
        int timeout = seconds * 1000;
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
            } catch (Throwable e) {
                logger.error("Error waiting for lambda invocation to complete");
                continue;
            }
        }

        Map<String, Object> results = new HashMap<>();

        if (!done) {
            results.put("errors", "Lambda Invocation did not finish on time");
            return results;
        }

        if (taskResult.getStatus().isFailed()) {
            results.put("errors", "Lambda Invocation returned failure");
            return results;
        }

        LambdaCloudDriverInvokeOperationResults invokeResponse = utils.getLambdaInvokeResults(url);
        String actual = invokeResponse.getBody();
        results.put("body", actual);
        results.put("response", invokeResponse.getResponseString());
        results.put("errors", invokeResponse.getErrorMessage());
        results.put("logs", invokeResponse.getInvokeResult().getLogResult());
        return results;
    }

    @Override
    public Collection<String> aliases() {
        List<String> ss = new ArrayList<String>();
        ss.add("lambdaInvokeVerificationTask");
        return ss;
    }
}
