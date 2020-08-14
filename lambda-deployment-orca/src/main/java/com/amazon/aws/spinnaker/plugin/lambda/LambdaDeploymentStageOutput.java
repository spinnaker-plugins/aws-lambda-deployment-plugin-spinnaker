/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.amazon.aws.spinnaker.plugin.lambda;

import com.netflix.spinnaker.orca.api.simplestage.SimpleStageOutput;
import java.util.List;
import java.util.Map;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LambdaDeploymentStageOutput extends SimpleStageOutput<Map<?, ?>, Object> {
    private String resourceId;
    private String url;
}
