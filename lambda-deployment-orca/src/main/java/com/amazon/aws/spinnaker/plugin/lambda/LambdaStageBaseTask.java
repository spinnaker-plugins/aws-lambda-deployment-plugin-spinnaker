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

package com.amazon.aws.spinnaker.plugin.lambda;
import com.netflix.spinnaker.orca.api.pipeline.Task;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface LambdaStageBaseTask extends Task {

    default Map<String, Object> buildContextOutput(LambdaCloudOperationOutput ldso, String urlKey) {
        String url = ldso.getUrl() != null ? ldso.getUrl() : "";
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(urlKey, url);
        return context;
    }

    default Map<String, Object> buildContextOutput() {
        Map<String, Object> context = new HashMap<String, Object>();
        return context;
    }

    /**
     * Fill up with values required for next task
     * @param ldso
     * @return
     */
    default Map<String, Object> buildContextOutput(LambdaCloudOperationOutput ldso) {
        String url = ldso.getUrl();
        Map<String, Object> context = new HashMap<>();
        context.put("url", url);
        return context;
    }

    default TaskResult formTaskResult(LambdaCloudOperationOutput ldso) {
        Map<String, Object> context = buildContextOutput(ldso);
        return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(context).build();
    }

    default TaskResult formTaskResultWithOutput(LambdaCloudOperationOutput ldso, Map<String, Object> outputMap) {
        Map<String, Object> context = buildContextOutput(ldso);
        return TaskResult.builder(ExecutionStatus.SUCCEEDED).context(context).outputs(outputMap).build();
    }

    default TaskResult formSuccessTaskResult(StageExecution stage, String successMessage) {
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("status", successMessage);
        stage.setOutputs(outputMap);
        return TaskResult.builder(ExecutionStatus.SUCCEEDED).outputs(outputMap).build();
    }

    default TaskResult formErrorTaskResult(StageExecution stage, String errorMessage) {
        Map<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("failureMessage", errorMessage);
        stage.getOutputs().putAll(outputMap);
        return TaskResult.builder(ExecutionStatus.TERMINAL).outputs(outputMap).build();
    }

    default TaskResult formErrorListTaskResult(StageExecution stage, List<String> errorMessages) {
        errorMessages.removeAll(Collections.singleton(null));
        String errorMessage = StringUtils.join(errorMessages, "\n");
        return formErrorTaskResult(stage, errorMessage);
    }
}
