/*
 * Copyright 2021 Armory, LLC
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

import com.amazon.aws.spinnaker.plugin.lambda.upsert.model.LambdaDeploymentInput;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

public class LambdaClouddriverUtilsTest {

    LambdaCloudDriverUtils lambdaCloudDriverUtils = new LambdaCloudDriverUtils();

    @Test
    public void validateUpsertLambdaInput_EnableLambdaAtEdgeIsFalse_True() {
        LambdaDeploymentInput ldi = LambdaDeploymentInput.builder()
                .account("account-test")
                .region("us-west-2")
                .functionName("function-test")
                .runtime("runtime")
                .s3bucket("s3-bucket")
                .s3key("abcdefg123456")
                .handler("handler")
                .role("arn:aws:iam::123456789012:role/LambdaAccess")
                .enableLambdaAtEdge(false)
                .build();
        assertTrue(lambdaCloudDriverUtils.validateUpsertLambdaInput(ldi, new ArrayList<String>()));
    }

    @Test
    public void validateLambdaEdgeInput_EnvVariablesIsNotEmpty_False() {
        HashMap<String,String> envVariables = new HashMap<>();
        envVariables.put("ENV_TEST_PORT","8888");
        LambdaDeploymentInput ldi = LambdaDeploymentInput.builder()
                .envVariables(envVariables)
                .region("us-east-1")
                .memorySize(128)
                .subnetIds(new ArrayList<>())
                .securityGroupIds(new ArrayList<>())
                .build();
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<String>()));
    }

    @Test
    public void validateLambdaEdgeInput_TimeoutGreaterThan5_False() {
        LambdaDeploymentInput ldi = LambdaDeploymentInput.builder()
                .envVariables(new HashMap<>())
                .timeout(10)
                .memorySize(128)
                .subnetIds(new ArrayList<>())
                .securityGroupIds(new ArrayList<>())
                .region("us-east-1")
                .build();
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<String>()));
    }

    @Test
    public void validateLambdaEdgeInput_MemorySizeGreaterThan128_False() {
        LambdaDeploymentInput ldi = LambdaDeploymentInput.builder()
                .envVariables(new HashMap<>())
                .timeout(5)
                .region("us-east-1")
                .memorySize(256)
                .subnetIds(new ArrayList<>())
                .securityGroupIds(new ArrayList<>())
                .build();
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<String>()));
    }

    @Test
    public void validateLambdaEdgeInput_RegionIsNotUsEast1_False() {
        LambdaDeploymentInput ldi = LambdaDeploymentInput.builder()
                .envVariables(new HashMap<>())
                .timeout(5)
                .region("us-west-2")
                .memorySize(128)
                .subnetIds(new ArrayList<>())
                .securityGroupIds(new ArrayList<>())
                .build();
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<String>()));
    }

    @Test
    public void validateLambdaEdgeInput_VpcIdIsNotNull_False() {
        LambdaDeploymentInput ldi = LambdaDeploymentInput.builder()
                .envVariables(new HashMap<>())
                .timeout(5)
                .region("us-east-1")
                .memorySize(128)
                .subnetIds(new ArrayList<>())
                .securityGroupIds(new ArrayList<>())
                .vpcId("vpc-2f09a348")
                .build();
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<String>()));
    }

    @Test
    public void validateLambdaEdgeInput_HaveSubNetIds_False() {
        LambdaDeploymentInput ldi = LambdaDeploymentInput.builder()
                .envVariables(new HashMap<>())
                .timeout(5)
                .region("us-east-1")
                .memorySize(128)
                .subnetIds(Stream.of("subnet-b46032ec").collect(Collectors.toList()))
                .securityGroupIds(new ArrayList<>())
                .build();
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<String>()));
    }

    @Test
    public void validateLambdaEdgeInput_HaveSecurityGroups_False() {
        LambdaDeploymentInput ldi = LambdaDeploymentInput.builder()
                .envVariables(new HashMap<>())
                .timeout(5)
                .region("us-east-1")
                .memorySize(128)
                .subnetIds(new ArrayList<>())
                .securityGroupIds(Stream.of("sg-b46032ec").collect(Collectors.toList()))
                .build();
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<String>()));
    }

    @Test
    public void getSortedRevisions_SortVersion_321(){
        Map<String, String> revisions = new HashMap<>();
            revisions.put("first","1");
            revisions.put("second","2");
            revisions.put("third","3");
        List<String> sortedList = Stream.of("3","2","1").collect(Collectors.toList());
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals(sortedList, lambdaCloudDriverUtils.getSortedRevisions(lambdaDefinition));
    }

    @Test
    public void getCanonicalVersion_NoRevisions_NoPublishedVersionsExist(){
        Map<String, String> revisions = new HashMap<>();
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertNull(lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition,"","",1));
    }

    @Test
    public void getCanonicalVersion_ProvidedRevision_PROVIDED(){
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first","1");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals("3", lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition,"$PROVIDED","3",0));
    }

    @Test
    public void getCanonicalVersion_LatestRevision_GetLatestVersion(){
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first","1");
        revisions.put("third","3");
        revisions.put("second","2");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals("3", lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition,"$LATEST","5",0));
    }

    @Test
    public void getCanonicalVersion_OldestRevision_GetOldestVersion(){
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first","5");
        revisions.put("third","1");
        revisions.put("second","7");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals("1", lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition,"$OLDEST","5",0));
    }

    @Test
    public void getCanonicalVersion_PreviousRevision_GetPreviousVersion(){
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first","5");
        revisions.put("third","1");
        revisions.put("second","7");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals("5", lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition,"$PREVIOUS","5",0));
    }

    @Test
    public void getCanonicalVersion_MovingRevision_GetMovingVersion(){
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first","5");
        revisions.put("third","1");
        revisions.put("second","7");
        revisions.put("other","8");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals("5,1", lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition,"$MOVING","5",2));
    }

    @Test
    public void getCanonicalVersion_InvalidInputVersion_Null(){
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first","5");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertNull(lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition,"$FAKE_INOUT_VERSION","5",2));
    }

    @Test
    public void getCanonicalVersion_NoRevisions_Null(){
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(new HashMap<>())
                .build();
        assertNull(lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition,"$PREVIOUS","5",2));
    }
}
