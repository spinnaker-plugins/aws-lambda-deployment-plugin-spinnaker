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
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.CloudDriverCacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LambdaCacheRefreshTask implements LambdaStageBaseTask {
    private static Logger logger = LoggerFactory.getLogger(LambdaCacheRefreshTask.class);
    static final String REFRESH_TYPE = "Function";

    private final CloudDriverCacheService cacheService;

    private final LambdaCloudDriverUtils utils;

    @Nonnull
    @Override
    public TaskResult execute(@Nonnull StageExecution stage) {
        logger.debug("Executing LambdaCacheRefreshTask...");
        prepareTask(stage);
        Map<String, Object> task = new HashMap<>(stage.getContext());
        task.put("appName", stage.getExecution().getApplication());
        cacheService.forceCacheUpdate("aws", REFRESH_TYPE, task);
        logger.debug("Going to wait for some seconds after requesting cache refresh...");
        utils.await();
        return TaskResult.ofStatus(ExecutionStatus.SUCCEEDED);
    }
}