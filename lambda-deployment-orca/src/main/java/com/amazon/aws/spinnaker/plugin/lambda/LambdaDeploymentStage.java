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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.orca.api.simplestage.SimpleStage;
import com.netflix.spinnaker.orca.api.simplestage.SimpleStageInput;
import com.netflix.spinnaker.orca.api.simplestage.SimpleStageOutput;
import com.netflix.spinnaker.orca.api.simplestage.SimpleStageStatus;
import okhttp3.*;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Extension
public class LambdaDeploymentStage implements SimpleStage<LambdaDeploymentInput> {
    private static Logger logger = LoggerFactory.getLogger(LambdaDeploymentStage.class);
    LambdaDeploymentContext context;

    public LambdaDeploymentStage() {
        logger.debug("Constructing LambdaDeploymentStage");
        context = new LambdaDeploymentContext();
    }

    @Override
    public SimpleStageOutput execute(SimpleStageInput<LambdaDeploymentInput> in) {
        logger.debug("Executing LambdaDeploymentStage");
        LambdaDeploymentInput inp = in.getValue();
        LambdaDeploymentStageOutput x = createLambda(inp);
        x.setStatus(SimpleStageStatus.SUCCEEDED);
        return x;
    }

    private LambdaDeploymentStageOutput createLambda(LambdaDeploymentInput inp) {
        // String rawString = "{\"region\": \"us-west-2\",\"functionName\": \"json_simple_lambda_222\",\"description\": \"json simple lambda as sample\",\"credentials\": \"aws-managed-1\",\"handler\": \"json_simple_lambda.entry_point\",\"s3bucket\": \"222014522391-playground\",\"s3key\": \"lambda/simple-json-function/simple_json_lambda.zip\",\"memory\": 512,\"publish\": \"false\",\"role\": \"arn:aws:iam::222014522391:role/lambda-role\",\"runtime\": \"python3.6\",\"timeout\": \"60\"}";
        String rawString = "";
        inp.setCredentials(inp.getAccount());
        inp.setRegion("us-west-2");
        inp.setTimeout(60);
        inp.setMemory(512);
        inp.setDescription("Hello World. Dummy description");

        /*
        inp = LambdaDeploymentInput.builder().functionName("json_simple_1").region("us-west-2").
                credentials("aws-managed-1").handler("func1_handle").s3bucket("222014522391-playground").
                s3key("lambda/simple-json-function/simple_json_lambda.zip").memory(512).publish("false").
                role("arn:aws:iam::222014522391:role/lambda-role").runtime("python3.6").timeout(60).build();
         */

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            rawString = objectMapper.writeValueAsString(inp);
        } catch (JsonProcessingException e) {
            logger.error("Could not jsonify", e);
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), rawString);
        String endPoint = "http://localhost:7002/aws/ops/createLambdaFunction";
        Request request = new Request.Builder()
                .url(endPoint)
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        LambdaDeploymentStageOutput x = LambdaDeploymentStageOutput.builder().build();
        Map xx = new HashMap<String, String>();
        try {
            Response response = call.execute();
            String respString = response.body().string();
            try {
                LambdaCloudDriverResponse respObj = objectMapper.readValue(respString, LambdaCloudDriverResponse.class);
                xx.put("id", respObj.getId());
                xx.put("resourceUri", respObj.getResourceUri());
            } catch (JsonProcessingException e) {
                logger.error("Could not jsonify", e);
            }
            xx.put("cloudDriverResponse", response.body().string());
        } catch (Exception e) {
            xx.put("errors", e.getMessage());
            logger.error("Could not call to cloud", e);
        }
        x.setOutput(xx);
        return x;
    }


    @Override
    public String getName() {
        return "Aws.LambdaDeploymentStage";
    }
}
