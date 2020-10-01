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
import com.amazon.aws.spinnaker.plugin.lambda.eventconfig.model.LambdaUpdateEventConfigurationTaskOutput;
import com.amazon.aws.spinnaker.plugin.lambda.upsert.model.LambdaUpdateAliasesInput;
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
import org.pf4j.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LambdaUpdateAliasesTask implements LambdaStageBaseTask {
    private static Logger logger = LoggerFactory.getLogger(LambdaUpdateAliasesTask.class);
    private static final ObjectMapper objMapper = new ObjectMapper();
    private static String CLOUDDRIVER_UPDATE_ALIAS_PATH = "/aws/ops/upsertLambdaFunctionAlias";
    private static final String DEFAULT_ALIAS_DESCRIPTION = "Created via Spinnaker";
    private static final String LATEST_VERSION_STRING = "$LATEST";

    @Autowired
    CloudDriverConfigurationProperties props;
    private  String cloudDriverUrl;

    @Autowired
    private LambdaCloudDriverUtils utils;

    @NotNull
    @Override
    public TaskResult execute(@NotNull StageExecution stage) {
        cloudDriverUrl = props.getCloudDriverBaseUrl();

        if (!shouldAddAliases(stage)) {
            return formSuccessTaskResult(stage, "No aliases to add");
        }

        List<LambdaCloudOperationOutput> output = updateLambdaAliases(stage);
        Map<String, Object> context = buildContextOutput(output);
        context.put(LambdaStageConstants.lambaCodeUpdatedKey, Boolean.TRUE);
        return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(context).build();
    }

    /**
     * Fill up with values required for next task
     */
    private Map<String, Object> buildContextOutput(List<LambdaCloudOperationOutput> ldso) {
        List<String> urlList = new ArrayList<String>();
        ldso.forEach(x -> {
            urlList.add(x.getUrl());
        });
        Map<String, Object> context = new HashMap<>();
        context.put(LambdaStageConstants.eventTaskKey, urlList);
        return context;
    }

    private boolean shouldAddAliases(StageExecution stage) {
        return stage.getContext().containsKey("aliases");
    }

    private LambdaCloudOperationOutput updateSingleAlias(LambdaUpdateAliasesInput inp, String alias) {
        inp.setAliasDescription(DEFAULT_ALIAS_DESCRIPTION);
        inp.setAliasName(alias);
        inp.setMajorFunctionVersion(LATEST_VERSION_STRING);
        String endPoint = cloudDriverUrl + CLOUDDRIVER_UPDATE_ALIAS_PATH;
        String rawString = utils.asString(inp);
        LambdaCloudDriverResponse respObj = utils.postToCloudDriver(endPoint, rawString);
        String url = cloudDriverUrl + respObj.getResourceUri();
        LambdaCloudOperationOutput operationOutput = LambdaCloudOperationOutput.builder().resourceId(respObj.getId()).url(url).build();
        return operationOutput;
    }

    private List<LambdaCloudOperationOutput> updateLambdaAliases(StageExecution stage) {
        List<LambdaCloudOperationOutput> result = new ArrayList<>();
        List<String> aliases = (List<String>)stage.getContext().get("aliases");
        LambdaUpdateAliasesInput inp = utils.getInput(stage, LambdaUpdateAliasesInput.class);
        inp.setAppName(stage.getExecution().getApplication());
        inp.setCredentials(inp.getAccount());
        for (String alias: aliases) {
            if (StringUtils.isNullOrEmpty(alias))
                continue;
            String formattedAlias = alias.trim();
            if (StringUtils.isNullOrEmpty(formattedAlias))
                continue;
            LambdaCloudOperationOutput operationOutput = updateSingleAlias(inp, formattedAlias);
            result.add(operationOutput);
        }
        return result;
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
