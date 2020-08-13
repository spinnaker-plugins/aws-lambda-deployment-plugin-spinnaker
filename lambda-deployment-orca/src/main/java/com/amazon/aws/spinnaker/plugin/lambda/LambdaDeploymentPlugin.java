/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LambdaDeploymentPlugin extends Plugin {
    Logger logger = LoggerFactory.getLogger(LambdaDeploymentPlugin.class);
    public LambdaDeploymentPlugin(PluginWrapper wrapper) {
        super(wrapper);
        logger.debug("Creating LambdaDeploymentPlugin");
    }

    @Override
    public void start() {
        logger.debug("LambdaDeploymentPlugin: Hello world. start plugin");
    }

    public void stop() {
        logger.debug("LambdaDeploymentPlugin: Hello world. stop plugin");
    }
}