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

import com.amazon.aws.spinnaker.plugin.lambda.agent.LambdaAgentProvider;
import com.netflix.spinnaker.kork.plugins.api.spring.SpringLoaderPlugin;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Arrays;
import java.util.List;

@Log4j2
public class LambdaClouddriverPlugin extends SpringLoaderPlugin {
    public LambdaClouddriverPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void registerBeanDefinitions(BeanDefinitionRegistry registry) {
        List<Pair<String, Class>> beanList =  Arrays.asList(
                Pair.of("lambdaAgentProvider", LambdaAgentProvider.class)
        );
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
        return  Arrays.asList("com.amazon.aws.spinnaker.plugin.lambda");
    }
}
