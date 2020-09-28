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

package com.amazon.aws.spinnaker.plugin.lambda.upsert.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LambdaUpdateConfigInput {
    private String credentials,functionName, description, handler, runtime, role, region;
    private String appName, account;
    private int memorySize, timeout;
    private boolean publish;

    private HashMap<String, String> envVariables;
    private HashMap<String, String> tags;
    private HashMap<String, String> deadLetterConfig;
    private HashMap<String, String> tracingConfig;
    private List<String> subnetIds;
    private List<String> securityGroupIds;
    private List<String> layers;

    private String targetGroups;
    private String encryptionKMSKeyArn;
}
