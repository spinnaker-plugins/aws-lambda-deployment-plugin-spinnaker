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

package com.amazon.aws.spinnaker.plugin.lambda.utils;

import com.amazonaws.services.lambda.model.AliasConfiguration;
import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
import com.amazonaws.services.lambda.model.FunctionCodeLocation;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LambdaGetOutput  extends FunctionConfiguration {
    private String cloudProvider;
    private String account;
    private String region;

    private Map<String, String> revisions;
    private List<AliasConfiguration> aliasConfigurations;
    private List<EventSourceMappingConfiguration> eventSourceMappings;
    private FunctionCodeLocation code;
    private Map<String, String> tags;
    private List<String> targetGroups;
}