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
import org.pf4j.util.StringUtils;
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
        LambdaUpdateEventConfigurationTaskInput taskInput = utils.getInput(stage, LambdaUpdateEventConfigurationTaskInput.class);
        taskInput.setAppName(stage.getExecution().getApplication());
        Boolean justCreated = (Boolean)stage.getContext().getOrDefault(LambdaStageConstants.lambaCreatedKey, false);
        LambdaDefinition lf = utils.findLambda(stage, justCreated);
        if (lf == null) {
            return formErrorTaskResult(stage, String.format("Could not find lambda to update event config for"));
        }
        if (taskInput.getTriggerArns() == null || taskInput.getTriggerArns().size() == 0) {
            deleteAllExistingEvents(taskInput, lf);
            Map<String, Object> context = new HashMap<>();
            return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(context).build();
        }
        deleteRemovedAndChangedEvents(taskInput, lf);
        LambdaUpdateEventConfigurationTaskOutput ldso = updateEventConfiguration(taskInput, lf);
        Map<String, Object> context = buildContextOutput(ldso);
        return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(context).build();
    }

    /**
     * New configuration has zero events. So find all existing events in the lambda cache and delete them all.
     * @param taskInput
     * @param lf
     */
    private void deleteAllExistingEvents(LambdaUpdateEventConfigurationTaskInput taskInput, LambdaDefinition lf) {
        if (StringUtils.isNotNullOrEmpty(taskInput.getAliasName())) {
            // TODO: cant handle delete of alias ARNS yet
            return;
        }
        List<String> eventArnList = getExistingEvents(lf);
        eventArnList.stream().forEach( eventArn -> { deleteEvent(eventArn, taskInput, lf); });
    }

    /**
     * For each eventTriggerArn that already exists at backend:
     *      remove from input if it exists in input  (no need to apply again)
     *      delete from backedn if it does not exist in input
     * @param taskInput
     * @param lf
     * @return
     */
    private LambdaUpdateEventConfigurationTaskOutput deleteRemovedAndChangedEvents(LambdaUpdateEventConfigurationTaskInput taskInput, LambdaDefinition lf) {
        LambdaUpdateEventConfigurationTaskOutput ans = LambdaUpdateEventConfigurationTaskOutput.builder().build();
        ans.setEventOutputs(new ArrayList<LambdaCloudOperationOutput>());
        if (lf == null) {
            return ans;
        }
        if (StringUtils.isNotNullOrEmpty(taskInput.getAliasName())) {
            // TODO: cant handle delete of alias ARNS yet
            return ans;
        }
        String endPoint = cloudDriverUrl + CLOUDDRIVER_UPDATE_EVENT_CONFIGURATION_LAMBDA_PATH;
        int newBatchsize = taskInput.getBatchsize();
        List<String> eventArnList = getExistingEvents(lf);
        eventArnList.stream()
                    .filter( x-> { return StringUtils.isNotNullOrEmpty(x); } )
                    .filter( x-> { return taskInput.getTriggerArns().contains(x); } )
                    .forEach( eventArn -> {
                        deleteEvent(eventArn, taskInput, lf);
                    });
        return ans;
    }

    List<String> getExistingEvents(LambdaDefinition lf) {
        List<EventSourceMappingConfiguration> esmList = lf.getEventSourceMappings();
        if (esmList == null) {
            return new ArrayList<String>();
        }
        List<String> allEventArns = esmList.stream().map(x -> { return x.getEventSourceArn(); })
                                                    .collect(Collectors.toList());
        return allEventArns;
    }

    private void deleteEvent(String eventArn, LambdaUpdateEventConfigurationTaskInput ti,  LambdaDefinition lgo) {
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
        if (StringUtils.isNotNullOrEmpty(ti.getAliasName())) {
            ti.setQualifier(ti.getAliasName());
        }
        inp.setUuid(toDelete.getUUID());

        deleteLambdaEventConfig(inp);
    }

    private LambdaUpdateEventConfigurationTaskOutput updateEventConfiguration(LambdaUpdateEventConfigurationTaskInput taskInput, LambdaDefinition lf) {
        LambdaUpdateEventConfigurationTaskOutput ans = LambdaUpdateEventConfigurationTaskOutput.builder().build();
        ans.setEventOutputs(new ArrayList<LambdaCloudOperationOutput>());
        taskInput.setCredentials(taskInput.getAccount());
        if (StringUtils.isNotNullOrEmpty(taskInput.getAliasName())) {
            taskInput.setQualifier(taskInput.getAliasName());
        }
        String endPoint = cloudDriverUrl + CLOUDDRIVER_UPDATE_EVENT_CONFIGURATION_LAMBDA_PATH;
        List<String> existingEvents = new ArrayList<String>();
        if (StringUtils.isNullOrEmpty(taskInput.getAliasName())) {
            // still cant update and delete events on aliases.
            existingEvents = getExistingEvents(lf);
        }
        final List<String> tmpEvents = existingEvents;
        taskInput.getTriggerArns().stream()
               .filter(curr -> { return !tmpEvents.contains(curr); })
               .forEach( curr -> {
                   LambdaEventConfigurationDescription singleEvent = formEventObject(curr, taskInput);
                   String rawString = utils.asString(singleEvent);
                   LambdaCloudDriverResponse respObj = utils.postToCloudDriver(endPoint, rawString);
                   String url = cloudDriverUrl + respObj.getResourceUri();
                   LambdaCloudOperationOutput z = LambdaCloudOperationOutput.builder().url(url).resourceId(respObj.getResourceUri()).build();
                   ans.getEventOutputs().add(z);
               });

        return ans;
    }

    private LambdaEventConfigurationDescription formEventObject(String curr, LambdaUpdateEventConfigurationTaskInput taskInput) {
        LambdaEventConfigurationDescription singleEvent = LambdaEventConfigurationDescription.builder().eventSourceArn(curr).batchsize(taskInput.getBatchsize()).enabled(true)
                .account(taskInput.getAccount()).credentials(taskInput.getCredentials()).appName(taskInput.getAppName())
                .region(taskInput.getRegion()).functionName(taskInput.getFunctionName()).qualifier(taskInput.getQualifier()).
                        build();
        return singleEvent;

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
