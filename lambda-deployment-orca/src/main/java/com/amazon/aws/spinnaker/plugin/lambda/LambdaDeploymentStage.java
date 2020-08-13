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

import com.netflix.spinnaker.orca.api.simplestage.SimpleStage;
import com.netflix.spinnaker.orca.api.simplestage.SimpleStageInput;
import com.netflix.spinnaker.orca.api.simplestage.SimpleStageOutput;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class LambdaDeploymentStage implements SimpleStage<LambdaDeploymentInput> {
    Logger logger = LoggerFactory.getLogger(LambdaDeploymentStage.class);
    LambdaDeploymentContext context;

    public LambdaDeploymentStage() {
        logger.debug("Executing LambdaDeploymentStage");
        logger.debug("Executing LambdaDeploymentStage");
    }

    @Override
    public SimpleStageOutput execute(SimpleStageInput<LambdaDeploymentInput> simpleStageInput) {
        logger.debug("Executing LambdaDeploymentStage");
        LambdaDeploymentStageOutput x  =  new LambdaDeploymentStageOutput();
        return x;
    }
    @Override
    public String getName() {
        logger.debug("Calling getName");
        return "Aws.LambdaDeploymentStage";
    }
}