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

import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaCloudDriverInvokeOperationResults;
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaPipelineArtifact;
import com.amazon.aws.spinnaker.plugin.lambda.upsert.model.LambdaDeploymentInput;
import com.amazon.aws.spinnaker.plugin.lambda.verify.model.LambdaCloudDriverTaskResults;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.collect.ImmutableMap;
import com.netflix.spinnaker.kork.artifacts.model.Artifact;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionType;
import com.netflix.spinnaker.orca.api.pipeline.models.PipelineExecution;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.OortService;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import com.netflix.spinnaker.orca.pipeline.model.PipelineExecutionImpl;
import okhttp3.Headers;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class LambdaClouddriverUtilsTest {

    private static WireMockServer server = new WireMockServer(7002);
    @InjectMocks
    private LambdaCloudDriverUtils lambdaCloudDriverUtils;
    @Mock
    private CloudDriverConfigurationProperties propsMock;
    @Mock
    private OortService oortMock;

    @BeforeAll
    public static void setup() {
        server.start();
        WireMock.configureFor("localhost", 7002);
    }

    @AfterAll
    public static void teardown() {
        if (null != server && server.isRunning()) {
            server.shutdownServer();
        }
    }

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
    }

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
        assertTrue(lambdaCloudDriverUtils.validateUpsertLambdaInput(ldi, new ArrayList<>()));
    }

    @Test
    public void validateLambdaEdgeInput_EnvVariablesIsNotEmpty_False() {
        HashMap<String, String> envVariables = new HashMap<>();
        envVariables.put("ENV_TEST_PORT", "8888");
        LambdaDeploymentInput ldi = LambdaDeploymentInput.builder()
                .envVariables(envVariables)
                .region("us-east-1")
                .memorySize(128)
                .subnetIds(new ArrayList<>())
                .securityGroupIds(new ArrayList<>())
                .build();
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<>()));
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
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<>()));
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
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<>()));
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
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<>()));
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
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<>()));
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
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<>()));
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
        assertFalse(lambdaCloudDriverUtils.validateLambdaEdgeInput(ldi, new ArrayList<>()));
    }

    @Test
    public void getSortedRevisions_SortVersion_321() {
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first", "1");
        revisions.put("second", "2");
        revisions.put("third", "3");
        List<String> sortedList = Stream.of("3", "2", "1").collect(Collectors.toList());
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals(sortedList, lambdaCloudDriverUtils.getSortedRevisions(lambdaDefinition));
    }

    @Test
    public void getCanonicalVersion_NoRevisions_NoPublishedVersionsExist() {
        Map<String, String> revisions = new HashMap<>();
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertNull(lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition, "", "", 1));
    }

    @Test
    public void getCanonicalVersion_ProvidedRevision_PROVIDED() {
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first", "1");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals("3", lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition, "$PROVIDED", "3", 0));
    }

    @Test
    public void getCanonicalVersion_LatestRevision_GetLatestVersion() {
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first", "1");
        revisions.put("third", "3");
        revisions.put("second", "2");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals("3", lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition, "$LATEST", "5", 0));
    }

    @Test
    public void getCanonicalVersion_OldestRevision_GetOldestVersion() {
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first", "5");
        revisions.put("third", "1");
        revisions.put("second", "7");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals("1", lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition, "$OLDEST", "5", 0));
    }

    @Test
    public void getCanonicalVersion_PreviousRevision_GetPreviousVersion() {
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first", "5");
        revisions.put("third", "1");
        revisions.put("second", "7");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals("5", lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition, "$PREVIOUS", "5", 0));
    }

    @Test
    public void getCanonicalVersion_MovingRevision_GetMovingVersion() {
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first", "5");
        revisions.put("third", "1");
        revisions.put("second", "7");
        revisions.put("other", "8");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertEquals("5,1", lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition, "$MOVING", "5", 2));
    }

    @Test
    public void getCanonicalVersion_InvalidInputVersion_Null() {
        Map<String, String> revisions = new HashMap<>();
        revisions.put("first", "5");
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(revisions)
                .build();
        assertNull(lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition, "$FAKE_INOUT_VERSION", "5", 2));
    }

    @Test
    public void getCanonicalVersion_NoRevisions_Null() {
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .revisions(new HashMap<>())
                .build();
        assertNull(lambdaCloudDriverUtils.getCanonicalVersion(lambdaDefinition, "$PREVIOUS", "5", 2));
    }

    @Test
    public void getFromCloudDriver_Success() {
        ResponseDefinitionBuilder mockResponse = new ResponseDefinitionBuilder()
                .withStatus(200)
                .withBody("Success");

        WireMock.stubFor(
                WireMock.get("/healthcheck")
                        .willReturn(mockResponse)
        );
        LambdaCloudDriverUtils lambdaCloudDriverUtilsMock = Mockito.mock(LambdaCloudDriverUtils.class);
        Mockito.when(lambdaCloudDriverUtilsMock.buildHeaders())
                .thenReturn(Headers.of("Content-Disposition", "form-data; name=\"fs_exp\""));
        Mockito.when(lambdaCloudDriverUtilsMock.getFromCloudDriver("http://localhost:7002/healthcheck")).thenCallRealMethod();
        assertEquals("Success", lambdaCloudDriverUtilsMock.getFromCloudDriver("http://localhost:7002/healthcheck"));
    }

    @Test
    public void retrieveLambdaFromCache_ShouldGetLambdaGetInput_NotNull() {
        ResponseDefinitionBuilder mockFunctionResponse = new ResponseDefinitionBuilder()
                .withStatus(200)
                .withBody("[{\"account\":\"account1\",\"aliasConfigurations\":[],\"code\":{\"location\":\"https:\\/\\/awslambda-us-west-2-tasks.s3.us-west-2.amazonaws.com\\/snapshots\\/569630529054\\/hello-world-9d719f9e\",\"repositoryType\":\"S3\"},\"codeSha256\":\"gEfN8j47XTW9VAGo6+dTbppFm3HZRnsOFI3\\/C6v05Xs=\",\"codeSize\":343,\"description\":\"A starter AWS Lambda function.\",\"eventSourceMappings\":[],\"fileSystemConfigs\":[],\"functionArn\":\"arn:aws:lambda:us-west-2:569630529054:function:hello-world\",\"functionName\":\"function-test\",\"handler\":\"lambda_function.lambda_handler\",\"lastModified\":\"2021-04-19T22:58:03.358+0000\",\"layers\":[],\"memorySize\":128,\"packageType\":\"Zip\",\"region\":\"us-west-2\",\"revisionId\":\"dc635189-fb73-4bd7-93d5-3b955568101e\",\"revisions\":{\"dc635189-fb73-4bd7-93d5-3b955568101e\":\"$LATEST\"},\"role\":\"arn:aws:iam::569630529054:role\\/service-role\\/hello-world-role-ff9v8sy0\",\"runtime\":\"python3.7\",\"state\":\"Active\",\"tags\":{\"lambda-console:blueprint\":\"hello-world-python\"},\"targetGroups\":[],\"timeout\":3,\"tracingConfig\":{\"mode\":\"PassThrough\"},\"version\":\"$LATEST\"}]");

        WireMock.stubFor(
                WireMock.get("/functions?region=us-west-2&account=account1&functionName=app-test-function-test")
                        .willReturn(mockFunctionResponse)
        );

        LambdaGetInput lambdaGetInput = new LambdaGetInput("us-west-2", "account1", "function-test", "app-test");
        Mockito.when(propsMock.getCloudDriverBaseUrl()).thenReturn("http://localhost:7002");
        assertNotNull(lambdaCloudDriverUtils.retrieveLambdaFromCache(lambdaGetInput));
    }

    @Test
    public void retrieveLambdaFromCache_ShouldNotGetLambdaGetInput_Null() {
        ResponseDefinitionBuilder mockFunctionNotFoundResponse = new ResponseDefinitionBuilder()
                .withStatus(204)
                .withBody("[]");

        WireMock.stubFor(
                WireMock.get("/functions?region=us-west-2&account=account2&functionName=app-test-function-test")
                        .willReturn(mockFunctionNotFoundResponse)
        );

        LambdaGetInput lambdaGetInput = new LambdaGetInput("us-west-2", "account2", "function-test", "app-test");
        Mockito.when(propsMock.getCloudDriverBaseUrl()).thenReturn("http://localhost:7002");
        assertNull(lambdaCloudDriverUtils.retrieveLambdaFromCache(lambdaGetInput));
    }

    @Test
    public void postToCloudDriver_ShouldGetLambdaCloudDriverResponse_NotNull() {
        ResponseDefinitionBuilder mockPostToCloudDriverResponse = new ResponseDefinitionBuilder()
                .withStatus(200)
                .withBody("{\"id\":\"id-123456789\",\"resourceUri\":\"http://resourceUri\"}");

        WireMock.stubFor(
                WireMock.post("/post")
                        .willReturn(mockPostToCloudDriverResponse)
        );
        assertNotNull(lambdaCloudDriverUtils.postToCloudDriver("http://localhost:7002/post", "{\"spinnaker\":\"test\"}"));
    }

    @Test
    public void postToCloudDriver_ShouldNotGetLambdaCloudDriverResponse_Null() {
        ResponseDefinitionBuilder mockPostToCloudDriverResponse = new ResponseDefinitionBuilder()
                .withStatus(204)
                .withBody("Error");

        WireMock.stubFor(
                WireMock.post("/post")
                        .willReturn(mockPostToCloudDriverResponse)
        );
        assertThrows(RuntimeException.class, () -> lambdaCloudDriverUtils.postToCloudDriver("http://localhost:7002/post", "{\"spinnaker\":\"test\"}"));
    }

    @Test
    public void getLambdaInvokeResults_ShouldGetLambdaCloudDriverInvokeOperationResults_NotNull() {
        ResponseDefinitionBuilder mockResponse = new ResponseDefinitionBuilder()
                .withStatus(200)
                .withBody("{\"status\":{\"complete\":true,\"completed\":true,\"retryable\":false,\"failed\":false,\"phase\":\"phase\",\"status\":\"status\"},\"resultObjects\":[{\"responseString\":\"{\\\"statusCode\\\": 200, \\\"body\\\": \\\"something\\\"}\",\"statusCode\":\"200\",\"body\":\"something\",\"errorMessage\":\"errorMessage\",\"hasErrors\":false}]}");

        WireMock.stubFor(
                WireMock.get("/lambdaInvokeResults")
                        .willReturn(mockResponse)
        );
        LambdaCloudDriverInvokeOperationResults results = lambdaCloudDriverUtils.getLambdaInvokeResults("http://localhost:7002/lambdaInvokeResults");
        assertNotNull(results);
        assertEquals(200, results.getStatusCode());
    }

    @Test
    public void getPublishedVersion_getVersion_1() {
        ResponseDefinitionBuilder mockResponse = new ResponseDefinitionBuilder()
                .withStatus(200)
                .withBody("{\"resultObjects\":[{\"version\":\"1\"}]}");

        WireMock.stubFor(
                WireMock.get("/publishedVersion")
                        .willReturn(mockResponse)
        );
        String publishedVersion = lambdaCloudDriverUtils.getPublishedVersion("http://localhost:7002/publishedVersion");
        assertNotNull(publishedVersion);
        assertEquals("1", publishedVersion);
    }

    @Test
    public void getPublishedVersion_getLatest_$LATEST() {
        ResponseDefinitionBuilder mockResponse = new ResponseDefinitionBuilder()
                .withStatus(200)
                .withBody("{\"resultObjects\":[{}]}");

        WireMock.stubFor(
                WireMock.get("/publishedVersion")
                        .willReturn(mockResponse)
        );
        String publishedVersion = lambdaCloudDriverUtils.getPublishedVersion("http://localhost:7002/publishedVersion");
        assertNotNull(publishedVersion);
        assertEquals("$LATEST", publishedVersion);
    }

    @Test
    public void getPublishedVersion_ShouldGetLatestIfNoVersionIsSpecified_$LATEST() {
        ResponseDefinitionBuilder mockResponse = new ResponseDefinitionBuilder()
                .withStatus(200)
                .withBody("{\"error\":[{}]}");

        WireMock.stubFor(
                WireMock.get("/publishedVersion")
                        .willReturn(mockResponse)
        );
        assertEquals("$LATEST", lambdaCloudDriverUtils.getPublishedVersion("http://localhost:7002/publishedVersion"));
    }

    @Test
    public void verifyStatus_getTheStatus_ShouldNotBeNull() {
        ResponseDefinitionBuilder mockResponse = new ResponseDefinitionBuilder()
                .withStatus(200)
                .withBody("{\"status\":{\"complete\":true,\"completed\":true,\"retryable\":false,\"failed\":false,\"phase\":\"phase\",\"status\":\"status\"},\"resultObjects\":[{\"version\":\"1\",\"functionName\":\"functionName\",\"eventSourceArn\":\"arn:aws:dynamodb:us-east-1:123456789012\",\"functionArn\":\"arn:aws:lambda:region:AccountID:function:function_name\",\"uuid\":\"32dc501c-f3d5-11eb-9a03-0242ac130003\",\"state\":\"completed\"}]}");

        WireMock.stubFor(
                WireMock.get("/verifyStatus")
                        .willReturn(mockResponse)
        );
        LambdaCloudDriverTaskResults lambdaCloudDriverTaskResults = lambdaCloudDriverUtils.verifyStatus("http://localhost:7002/verifyStatus");
        assertNotNull(lambdaCloudDriverTaskResults);
    }

    @Test
    public void resolvePipelineArtifact_Test() {
        LambdaPipelineArtifact lambdaPipelineArtifact = LambdaPipelineArtifact.builder()
                .id("32dc501c-f3d5-11eb-9a03-0242ac130003")
                .artifactAccount("artifactAccount")
                .type("type")
                .reference("reference")
                .name("name")
                .version("version")
                .location("location")
                .provenance("provenance")
                .build();

        Artifact artifact = Artifact.builder()
                .uuid("32dc501c-f3d5-11eb-9a03-0242ac130003")
                .artifactAccount("artifactAccount")
                .type("type")
                .reference("reference")
                .version("version")
                .name("name")
                .build();
        try {
            InputStream stubInputStream =
                    IOUtils.toInputStream("InputStream", "UTF-8");
            final byte[] body = IOUtils.toByteArray(stubInputStream);
            TypedInput typedInput = new TypedInput() {

                @Override
                public String mimeType() {
                    return "application/json";
                }

                @Override
                public long length() {
                    return body.length;
                }

                @Override
                public InputStream in() throws IOException {
                    return new ByteArrayInputStream(body);
                }
            };
            retrofit.client.Response response = new Response("url", 200, "reason", Arrays.asList(new Header("foo", "bar")), typedInput);
            Mockito.when(oortMock.fetchArtifact(artifact)).thenReturn(response);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        assertEquals("InputStream", lambdaCloudDriverUtils.getPipelinesArtifactContent(lambdaPipelineArtifact));
    }

    @Test
    public void retrieveLambdaFromCache_ShouldNotBeNull(){
        Mockito.when(propsMock.getCloudDriverBaseUrl()).thenReturn("http://localhost:7002");
        ResponseDefinitionBuilder mockFunctionResponse = new ResponseDefinitionBuilder()
                .withStatus(200)
                .withBody("[{\"account\":\"account1\",\"aliasConfigurations\":[],\"code\":{\"location\":\"https:\\/\\/awslambda-us-west-2-tasks.s3.us-west-2.amazonaws.com\\/snapshots\\/569630529054\\/hello-world-9d719f9e\",\"repositoryType\":\"S3\"},\"codeSha256\":\"gEfN8j47XTW9VAGo6+dTbppFm3HZRnsOFI3\\/C6v05Xs=\",\"codeSize\":343,\"description\":\"A starter AWS Lambda function.\",\"eventSourceMappings\":[],\"fileSystemConfigs\":[],\"functionArn\":\"arn:aws:lambda:us-west-2:569630529054:function:hello-world\",\"functionName\":\"function-test\",\"handler\":\"lambda_function.lambda_handler\",\"lastModified\":\"2021-04-19T22:58:03.358+0000\",\"layers\":[],\"memorySize\":128,\"packageType\":\"Zip\",\"region\":\"us-west-2\",\"revisionId\":\"dc635189-fb73-4bd7-93d5-3b955568101e\",\"revisions\":{\"dc635189-fb73-4bd7-93d5-3b955568101e\":\"$LATEST\"},\"role\":\"arn:aws:iam::569630529054:role\\/service-role\\/hello-world-role-ff9v8sy0\",\"runtime\":\"python3.7\",\"state\":\"Active\",\"tags\":{\"lambda-console:blueprint\":\"hello-world-python\"},\"targetGroups\":[],\"timeout\":3,\"tracingConfig\":{\"mode\":\"PassThrough\"},\"version\":\"$LATEST\"}]");

        WireMock.stubFor(
                WireMock.get("/functions?region=us-west-2&account=account1&functionName=lambdaApp-function-test")
                        .willReturn(mockFunctionResponse)
        );
        StageExecution stageExecution = Mockito.mock(StageExecution.class);
        Map<String, Object> lambdaGetInput = ImmutableMap.of(
                "region", "us-west-2",
                "account", "account1",
                "functionName", "function-test");
        Mockito.when(stageExecution.getContext()).thenReturn(lambdaGetInput);
        PipelineExecution pipelineExecution = new PipelineExecutionImpl(ExecutionType.PIPELINE,"lambdaApp");
        Mockito.when(stageExecution.getExecution()).thenReturn(pipelineExecution);
        LambdaDefinition lambdaDefinition = lambdaCloudDriverUtils.retrieveLambdaFromCache(stageExecution, false);
        assertNotNull(lambdaDefinition);
        assertEquals("account1",lambdaDefinition.getAccount());
    }

}
