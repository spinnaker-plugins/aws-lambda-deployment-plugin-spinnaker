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

package com.amazon.aws.spinnaker.plugin.lambda.eventconfig;

import com.amazon.aws.spinnaker.plugin.lambda.LambdaCloudOperationOutput;
import com.amazon.aws.spinnaker.plugin.lambda.LambdaStageBaseTask;
import com.amazon.aws.spinnaker.plugin.lambda.eventconfig.model.LambdaDeleteEventTaskInput;
import com.amazon.aws.spinnaker.plugin.lambda.eventconfig.model.LambdaEventConfigurationDescription;
import com.amazon.aws.spinnaker.plugin.lambda.eventconfig.model.LambdaUpdateEventConfigurationTaskInput;
import com.amazon.aws.spinnaker.plugin.lambda.eventconfig.model.LambdaUpdateEventConfigurationTaskOutput;
import com.amazon.aws.spinnaker.plugin.lambda.utils.*;
import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
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
import java.util.stream.Collectors;

@Component
public class LambdaUpdateEventConfigurationTask implements LambdaStageBaseTask {
    private static final Logger logger = LoggerFactory.getLogger(LambdaUpdateEventConfigurationTask.class);
    private static final String CLOUDDRIVER_UPDATE_EVENT_CONFIGURATION_LAMBDA_PATH = "/aws/ops/upsertLambdaFunctionEventMapping";
    private static final String CLOUDDRIVER_DELETE_EVENT_CONFIGURATION_LAMBDA_PATH = "/aws/ops/deleteLambdaFunctionEventMapping";

    String cloudDriverUrl;

    @Autowired
    CloudDriverConfigurationProperties props;

    @Autowired
    LambdaCloudDriverUtils utils;

    @NotNull
    @Override
    public TaskResult execute(@NotNull StageExecution stage) {
        logger.debug("Executing LambdaUpdateEventConfigurationTask...");
        cloudDriverUrl = props.getCloudDriverBaseUrl();
        LambdaUpdateEventConfigurationTaskInput ldi = utils.getInput(stage, LambdaUpdateEventConfigurationTaskInput.class);
        ldi.setAppName(stage.getExecution().getApplication());
        Boolean justCreated = (Boolean)stage.getContext().get(LambdaStageConstants.lambaCreatedKey);
        LambdaGetOutput lf = null;
        if (justCreated) {
            utils.await(stage);
        }
        lf = utils.findLambda(stage, true);
        if (lf == null) {
            List<String> errorMessages = new ArrayList<String>();
            errorMessages.add(String.format("Could not find lambda to update event config for"));
            logger.error("Could not find lambda to update event for. ");
            return utils.formErrorTaskResult(stage, errorMessages);
        }

        if (ldi.getTriggerArns() == null || ldi.getTriggerArns().size() == 0) {
            deleteAllExistingEvents(ldi, lf);
            Map<String, Object> context = new HashMap<>();// buildContextOutput(ldso);
            return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(context).build();
        }

        // TODO: Make sure lambda exists - in the case that it was just created.
        deleteExistingEvents(ldi, lf);
        LambdaUpdateEventConfigurationTaskOutput ldso = updateEventConfiguration(ldi);
        Map<String, Object> context = buildContextOutput(ldso);
        return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(context).build();
    }
    private void deleteAllExistingEvents(LambdaUpdateEventConfigurationTaskInput ldi, LambdaGetOutput lf) {
        List<String> eventArnList = getExistingEvents(lf, ldi.getBatchsize());
        eventArnList.stream().forEach( eventArn -> {
                deleteEvent(eventArn, ldi, lf);
        });
    }

    /**
     * For each eventTriggerArn that already exists at backend:
     *      remove from input if it exists in input  (no need to apply again)
     *      delete from backedn if it does not exist in input
     * @param taskInput
     * @param lf
     * @return
     */
    private LambdaUpdateEventConfigurationTaskOutput deleteExistingEvents(LambdaUpdateEventConfigurationTaskInput taskInput, LambdaGetOutput lf) {
        LambdaUpdateEventConfigurationTaskOutput ans = LambdaUpdateEventConfigurationTaskOutput.builder().build();
        if (lf == null) {
            return ans;
        }
        ans.setEventOutputs(new ArrayList<LambdaCloudOperationOutput>());
        String endPoint = cloudDriverUrl + CLOUDDRIVER_UPDATE_EVENT_CONFIGURATION_LAMBDA_PATH;
        int oldBatchsize = taskInput.getBatchsize();
        List<String> eventArnList = getExistingEvents(lf, oldBatchsize);
        eventArnList.stream().forEach( eventArn -> {
            if (taskInput.getTriggerArns().contains(eventArn)) {
                taskInput.getTriggerArns().remove(eventArn);
            }
            else {
                deleteEvent(eventArn, taskInput, lf);
            }
        });
        return ans;
    }

    List<String> getExistingEvents(LambdaGetOutput lf, int oldBatchSize) {
        List<EventSourceMappingConfiguration> esmList = lf.getEventSourceMappings();
        if (esmList == null) {
            return null;
        }
        List<String> allEventArns = esmList.stream().map(x -> {
            if (oldBatchSize != x.getBatchSize())
                return x.getEventSourceArn();
            else
                return "";
        }).collect(Collectors.toList());
        return allEventArns;
    }

    private void deleteEvent(String eventArn, LambdaUpdateEventConfigurationTaskInput ti,  LambdaGetOutput lgo) {
        logger.debug("To be deleted: " + eventArn);
        List<EventSourceMappingConfiguration> esmList = lgo.getEventSourceMappings();
        Optional<EventSourceMappingConfiguration> oo =
        esmList.stream().filter(x -> {
            return x.getEventSourceArn().equals(eventArn);
        }).findFirst();
        if (oo.isEmpty()) {
            return;
        }
        EventSourceMappingConfiguration toDelete = oo.get();
        LambdaDeleteEventTaskInput inp = LambdaDeleteEventTaskInput.builder()
                .account(ti.getAccount())
                .credentials(ti.getCredentials())
                .functionName(ti.getFunctionName())
                .eventSourceArn(toDelete.getEventSourceArn())
                .region(ti.getRegion()).build();
        inp.setUuid(toDelete.getUUID());
        deleteLambdaEventConfig(inp);
    }

    private LambdaUpdateEventConfigurationTaskOutput updateEventConfiguration(LambdaUpdateEventConfigurationTaskInput taskInput) {
        LambdaUpdateEventConfigurationTaskOutput ans = LambdaUpdateEventConfigurationTaskOutput.builder().build();
        ans.setEventOutputs(new ArrayList<LambdaCloudOperationOutput>());
        taskInput.setCredentials(taskInput.getAccount());
        String endPoint = cloudDriverUrl + CLOUDDRIVER_UPDATE_EVENT_CONFIGURATION_LAMBDA_PATH;
        final List<String> urlList = new ArrayList<String>();
        taskInput.getTriggerArns().forEach( curr -> {
            logger.debug("Now processing: " + curr);
            boolean enabled = true;
            LambdaEventConfigurationDescription singleEvent = LambdaEventConfigurationDescription.builder().eventSourceArn(curr).batchsize(taskInput.getBatchsize()).enabled(true)
                    .account(taskInput.getAccount()).credentials(taskInput.getCredentials()).appName(taskInput.getAppName())
                    .region(taskInput.getRegion()).functionName(taskInput.getFunctionName()).build();
            String rawString = utils.asString(singleEvent);
            LambdaCloudDriverResponse respObj = utils.postToCloudDriver(endPoint, rawString);
            String url = cloudDriverUrl + respObj.getResourceUri();
            LambdaCloudOperationOutput z = LambdaCloudOperationOutput.builder().url(url).resourceId(respObj.getResourceUri()).build();
            ans.getEventOutputs().add(z);
            urlList.add(url);
        });

        return ans;
    }

    private LambdaCloudOperationOutput deleteLambdaEventConfig(LambdaDeleteEventTaskInput inp) {
        LambdaCloudOperationOutput ans = LambdaCloudOperationOutput.builder().build();
        inp.setCredentials(inp.getAccount());
        String endPoint = cloudDriverUrl + CLOUDDRIVER_DELETE_EVENT_CONFIGURATION_LAMBDA_PATH;
        String rawString = utils.asString(inp);
        LambdaCloudDriverResponse respObj = utils.postToCloudDriver(endPoint, rawString);
        String url = cloudDriverUrl + respObj.getResourceUri();
        LambdaCloudOperationOutput resp = LambdaCloudOperationOutput.builder().url(url).build();
        return resp;
    }


    /**
     * Fill up with values required for next task
     */
    private Map<String, Object> buildContextOutput(LambdaUpdateEventConfigurationTaskOutput ldso) {
        List<String> urlList = new ArrayList<String>();
        ldso.getEventOutputs().forEach(x -> {
            urlList.add(x.getUrl());
        });
        Map<String, Object> context = new HashMap<>();
        context.put(LambdaStageConstants.eventTaskKey, urlList);
        return context;
    }
}
