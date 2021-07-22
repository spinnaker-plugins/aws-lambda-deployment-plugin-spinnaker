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

import com.amazon.aws.spinnaker.plugin.lambda.LambdaCloudOperationOutput;
import com.amazon.aws.spinnaker.plugin.lambda.LambdaStageBaseTask;
import com.amazon.aws.spinnaker.plugin.lambda.verify.model.LambdaCacheRefreshInput;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverResponse;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit.client.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class LambdaCacheRefreshTask implements LambdaStageBaseTask {
    private static Logger logger = LoggerFactory.getLogger(LambdaCacheRefreshTask.class);
    private static String CLOUDDRIVER_REFRESH_CACHE_PATH = "/cache/aws/function";

    @Autowired
    CloudDriverConfigurationProperties props;
    private  String cloudDriverUrl;

    @Autowired
    private LambdaCloudDriverUtils utils;

    @Nonnull
    @Override
    public TaskResult execute(@Nonnull StageExecution stage) {
        logger.debug("Executing LambdaCacheRefreshTask...");
        cloudDriverUrl = props.getCloudDriverBaseUrl();
        prepareTask(stage);
        LambdaCloudOperationOutput output = forceCacheRefresh(stage);
        logger.debug("Going to wait for some seconds after requesting cache refresh...");
        //TODO: Change to a live call to verify cache has FINISHED vs. a fixed wait time.  UNFORTUNATELY not implemented in:
        // https://github.com/spinnaker/clouddriver/blob/master/clouddriver-lambda/src/main/java/com/netflix/spinnaker/clouddriver/lambda/provider/agent/LambdaCachingAgent.java#L349
        utils.await(Duration.ofMinutes(5).toMillis());
        return taskComplete(stage);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();


    private LambdaCloudOperationOutput forceCacheRefresh(StageExecution stage) {
        LambdaCacheRefreshInput inp = utils.getInput(stage, LambdaCacheRefreshInput.class);
        inp.setAppName(stage.getExecution().getApplication());
        inp.setCredentials(inp.getAccount());
        String endPoint = cloudDriverUrl + CLOUDDRIVER_REFRESH_CACHE_PATH;
        String rawString = utils.asString(inp);
        LambdaCloudDriverResponse respObj = utils.postToCloudDriver(endPoint, rawString);
        String url = cloudDriverUrl + respObj.getResourceUri();
        logger.debug("Posted to cloudDriver for cache refresh: " + url);
        LambdaCloudOperationOutput operationOutput = LambdaCloudOperationOutput.builder().resourceId(respObj.getId()).url(url).build();


        //WAIT for cache refresh to finish...
        try {
            String response = utils.getFromCloudDriver(cloudDriverUrl + CLOUDDRIVER_REFRESH_CACHE_PATH + "?id=" + respObj.getId());
            Collection<Map<?,?>> onDemands = objectMapper.readValue(, Map.of(String.class, Object.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return operationOutput;
    }
}
