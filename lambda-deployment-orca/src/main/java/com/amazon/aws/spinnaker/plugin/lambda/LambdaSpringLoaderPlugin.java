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

import com.amazon.aws.spinnaker.plugin.lambda.delete.LambdaDeleteStage;
import com.amazon.aws.spinnaker.plugin.lambda.delete.LambdaDeleteTask;
import com.amazon.aws.spinnaker.plugin.lambda.delete.LambdaDeleteVerificationTask;
import com.amazon.aws.spinnaker.plugin.lambda.eventconfig.LambdaUpdateEventConfigurationTask;
import com.amazon.aws.spinnaker.plugin.lambda.invoke.LambdaInvokeStage;
import com.amazon.aws.spinnaker.plugin.lambda.invoke.LambdaInvokeTask;
import com.amazon.aws.spinnaker.plugin.lambda.invoke.LambdaInvokeVerificationTask;
import com.amazon.aws.spinnaker.plugin.lambda.traffic.*;
import com.amazon.aws.spinnaker.plugin.lambda.upsert.*;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.amazon.aws.spinnaker.plugin.lambda.verify.LambdaCacheRefreshTask;
import com.amazon.aws.spinnaker.plugin.lambda.verify.LambdaVerificationTask;
import com.netflix.spinnaker.kork.plugins.api.spring.SpringLoaderPlugin;
import org.apache.commons.lang3.tuple.Pair;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Arrays;
import java.util.List;

public class LambdaSpringLoaderPlugin extends SpringLoaderPlugin {
    private static Logger logger = LoggerFactory.getLogger(LambdaSpringLoaderPlugin.class);
    public LambdaSpringLoaderPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void registerBeanDefinitions(BeanDefinitionRegistry registry) {
        List<Pair<String, Class>> beanList =  Arrays.asList(
                Pair.of("Aws.LambdaDeploymentStage", LambdaDeploymentStage.class),
                Pair.of("lambdaCloudDriverUtils", LambdaCloudDriverUtils.class),
                Pair.of("lambdaCreationTask", LambdaCreateTask.class),
                Pair.of("lambdaUpdateCodeTask", LambdaUpdateCodeTask.class),
                Pair.of("lambdaUpdateConfigurationTask", LambdaUpdateConfigurationTask.class),
                Pair.of("lambdaVerificationTask", LambdaVerificationTask.class),
                Pair.of("lambdaDeleteTask", LambdaDeleteTask.class),
                Pair.of("lambdaPublishVersionTask", LambdaPublishVersionTask.class),
                Pair.of("lambdaDeleteVerificationTask", LambdaDeleteVerificationTask.class),
                Pair.of("Aws.LambdaDeleteStage", LambdaDeleteStage.class),
                Pair.of("lambdaTrafficUpdateTask", LambdaTrafficUpdateTask.class),
                Pair.of("lambdaUpdateAliasesTask",LambdaUpdateAliasesTask.class),
                Pair.of("lambdaCacheRefreshTask", LambdaCacheRefreshTask.class),
                Pair.of("lambdaWaitForCachePublishTask", LambdaWaitForCachePublishTask.class),
                Pair.of("lambdaOutputTask", LambdaOutputTask.class),
                Pair.of("lambdaPutConcurrencyTask", LambdaPutConcurrencyTask.class),
                Pair.of("lambdaTrafficUpdateVerificationTask", LambdaTrafficUpdateVerificationTask.class),
                Pair.of("lambdaUpdateEventConfigurationTask", LambdaUpdateEventConfigurationTask.class),
                Pair.of("trafficUpdateStrategyInjector", TrafficUpdateStrategyInjector.class),
                Pair.of("simpleStrategy", SimpleDeploymentStrategy.class),
                Pair.of("weightedStrategy", WeightedDeploymentStrategy.class),
                Pair.of("blueGreenStrategy", BlueGreenDeploymentStrategy.class),
                Pair.of("Aws.LambdaInvokeStage", LambdaInvokeStage.class),
                Pair.of("lambdaInvokeTask", LambdaInvokeTask.class),
                Pair.of("lambdaInvokeVerifyTask", LambdaInvokeVerificationTask.class),
                Pair.of("Aws.LambdaTrafficShaper", LambdaTrafficRoutingStage.class));
        beanList.forEach( curr -> {
            BeanDefinition lazyLoadCredentialsRepositoryDefinition = primaryBeanDefinitionFor(curr.getRight());
            try {
                registry.registerBeanDefinition(curr.getLeft(), lazyLoadCredentialsRepositoryDefinition);
            } catch (BeanDefinitionStoreException e) {
                log.error("Could not register bean {}", lazyLoadCredentialsRepositoryDefinition.getBeanClassName());
                throw new RuntimeException(e);
            }
        });
        super.registerBeanDefinitions(registry);
    }

    /**
     * Specify plugin packages to scan for beans.
     */
    public List<String> getPackagesToScan() {
        return  Arrays.asList("com.amazon.aws.spinnaker.plugin.lambda",
                              "com.amazon.aws.spinnaker.plugin.lambda.delete",
                              "com.amazon.aws.spinnaker.plugin.lambda.eventconfig",
                              "com.amazon.aws.spinnaker.plugin.lambda.upsert",
                              "com.amazon.aws.spinnaker.plugin.lambda.utils",
                              "com.amazon.aws.spinnaker.plugin.lambda.verify",
                              "com.amazon.aws.spinnaker.plugin.lambda.traffic");
    }
}
