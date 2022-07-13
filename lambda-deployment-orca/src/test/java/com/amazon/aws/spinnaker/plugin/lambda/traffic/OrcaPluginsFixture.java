/*
 * Copyright 2022 Armory, LLC
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

package com.amazon.aws.spinnaker.plugin.lambda.traffic;

import com.amazon.aws.spinnaker.plugin.lambda.LambdaSpringLoaderPlugin;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.kork.plugins.internal.PluginJar;
import com.netflix.spinnaker.orca.StageResolver;
import com.netflix.spinnaker.orca.api.test.OrcaFixture;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;

@TestPropertySource(properties = {
        "spinnaker.extensibility.plugins.Aws.LambdaDeploymentPlugin.enabled=true",
        "spinnaker.extensibility.plugins-root-path=build/plugins"
})
@AutoConfigureMockMvc
public class OrcaPluginsFixture extends OrcaFixture {
    @Autowired
    StageResolver stageResolver;

    @Autowired
    MockMvc mockMvc;

    ObjectMapper mapper =  new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    OrcaPluginsFixture() {
        String pluginId = "Aws.LambdaDeploymentPlugin";
        File plugins = new File("build/plugins");
        FileUtils.deleteQuietly(plugins);
        plugins.mkdir();

        new PluginJar.Builder(plugins.toPath()
                .resolve(pluginId+".jar"), pluginId)
                .pluginClass(LambdaSpringLoaderPlugin.class.getName())
                .pluginVersion("1.0.0")
                .build();
    }
}
