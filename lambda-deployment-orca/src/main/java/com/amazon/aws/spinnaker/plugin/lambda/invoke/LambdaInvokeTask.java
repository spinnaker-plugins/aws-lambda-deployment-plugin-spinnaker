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
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaTrafficUpdateInput;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverResponse;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaDefinition;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import org.jetbrains.annotations.NotNull;
import org.pf4j.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
public class LambdaInvokeTask  implements LambdaStageBaseTask {
    private static Logger logger = LoggerFactory.getLogger(LambdaInvokeTask.class);

    private String cloudDriverUrl;

    @Autowired
    CloudDriverConfigurationProperties props;

    @Autowired
    private LambdaCloudDriverUtils utils;

    static String CLOUDDRIVER_INVOKE_LAMBDA_FUNCTION_PATH = "/aws/ops/invokeLambdaFunction";

    @NotNull
    @Override
    public TaskResult execute(@NotNull StageExecution stage) {
        logger.debug("Executing LambdaInvokeTask...");
        cloudDriverUrl = props.getCloudDriverBaseUrl();
        prepareTask(stage);
        LambdaDefinition lf = utils.findLambda(stage, true);
        if (lf == null) {
            logger.error("Could not find lambda");
            return this.formErrorTaskResult(stage, "No such lambda found.");
        }
        LambdaInvokeStageInput ldi = utils.getInput(stage, LambdaInvokeStageInput.class);
        LambdaTrafficUpdateInput tui = utils.getInput(stage, LambdaTrafficUpdateInput.class);
        if (!Objects.isNull(ldi.getPayloadArtifact())) {
            ldi.setPayloadArtifact(tui.getPayloadArtifact().getArtifact());
            ldi.setPayload(utils.getPipelinesArtifactContent(ldi.getPayloadArtifact()));
        }
        ldi.setQualifier(StringUtils.isNullOrEmpty(ldi.getAliasName()) ? "$LATEST" : ldi.getAliasName());
        ldi.setAppName(stage.getExecution().getApplication());
        ldi.setCredentials(ldi.getAccount());
        List<String> urlList = new ArrayList<String>();
        for (int i= 0; i < ldi.getExecutionCount(); i++) {
            String url = this.invokeLambdaFunction(ldi);
            urlList.add(url);
        }
        addToTaskContext(stage, "urlList", urlList);
        return taskComplete(stage);
    }

    private String invokeLambdaFunction(LambdaInvokeStageInput ldi) {
        String cloudDriverUrl = props.getCloudDriverBaseUrl();
        String endPoint = cloudDriverUrl + CLOUDDRIVER_INVOKE_LAMBDA_FUNCTION_PATH;
        String rawString = utils.asString(ldi);
        LambdaCloudDriverResponse respObj = utils.postToCloudDriver(endPoint, rawString);
        String url = cloudDriverUrl + respObj.getResourceUri();
        logger.debug("Posted to cloudDriver for lambda invocation: " + url);
        return url;
    }

    @Override
    public Collection<String> aliases() {
        List<String> ss = new ArrayList<String>();
        ss.add("lambdaInvokeTask");
        return ss;
    }
}

