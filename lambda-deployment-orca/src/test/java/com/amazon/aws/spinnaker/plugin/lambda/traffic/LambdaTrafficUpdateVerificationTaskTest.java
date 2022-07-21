package com.amazon.aws.spinnaker.plugin.lambda.traffic;

import com.amazon.aws.spinnaker.plugin.lambda.Config;
import com.amazon.aws.spinnaker.plugin.lambda.traffic.model.LambdaTrafficUpdateInput;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverResponse;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaDefinition;
import com.amazon.aws.spinnaker.plugin.lambda.verify.model.LambdaCloudDriverErrorObject;
import com.amazon.aws.spinnaker.plugin.lambda.verify.model.LambdaCloudDriverTaskResults;
import com.amazon.aws.spinnaker.plugin.lambda.verify.model.LambdaVerificationStatusOutput;
import com.amazonaws.services.lambda.model.AliasConfiguration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.ImmutableList;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.PipelineExecution;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.clouddriver.config.CloudDriverConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({WiremockResolver.class, WiremockUriResolver.class})
public class LambdaTrafficUpdateVerificationTaskTest {

    WireMockServer wireMockServer;

    @InjectMocks
    private LambdaTrafficUpdateVerificationTask lambdaTrafficUpdateVerificationTask;

    @Mock
    private CloudDriverConfigurationProperties propsMock;

    @Mock
    private LambdaCloudDriverUtils lambdaCloudDriverUtilsMock;

    @Mock
    private StageExecution stageExecution;

    @Mock
    private PipelineExecution pipelineExecution;

    @Mock
    private Config config;

    @BeforeEach
    void init(@WiremockResolver.Wiremock WireMockServer wireMockServer, @WiremockUriResolver.WiremockUri String uri) {
        this.wireMockServer = wireMockServer;
        MockitoAnnotations.initMocks(this);
        Mockito.when(propsMock.getCloudDriverBaseUrl()).thenReturn(uri);
        pipelineExecution.setApplication("lambdaApp");
        Mockito.when(stageExecution.getExecution()).thenReturn(pipelineExecution);
        Mockito.when(stageExecution.getContext()).thenReturn(new HashMap<String, Object>());
        Mockito.when(stageExecution.getOutputs()).thenReturn(new HashMap<String, Object>());
        stageExecution.getContext().put("url", "test");
        LambdaTrafficUpdateInput ldi = LambdaTrafficUpdateInput.builder()
                .functionName("functionName")
                .aliasName("develop")
                .deploymentStrategy("$BLUEGREEN")
                .build();
        Mockito.when(lambdaCloudDriverUtilsMock.postToCloudDriver(Mockito.any(),Mockito.any() )).thenReturn(LambdaCloudDriverResponse.builder().build());
        Mockito.when(lambdaCloudDriverUtilsMock.getInput(stageExecution, LambdaTrafficUpdateInput.class)).thenReturn(ldi);
    }

    @Test
    public void execute_UpdateVerification_verifyStatus_RUNNING(){
        LambdaCloudDriverTaskResults lambdaCloudDriverTaskResults = LambdaCloudDriverTaskResults.builder()
                .status(LambdaVerificationStatusOutput.builder().status("RUNNING").build())
                .build();
        Mockito.when(lambdaCloudDriverUtilsMock
                .verifyStatus(Mockito.any())).thenReturn(lambdaCloudDriverTaskResults);

        assertEquals(ExecutionStatus.RUNNING, lambdaTrafficUpdateVerificationTask.execute(stageExecution).getStatus());
    }

    @Test
    public void execute_UpdateVerification_verifyStatus_ERROR(){
        String taskVerifyError = "clouddriver task not found error";
        LambdaCloudDriverTaskResults lambdaCloudDriverTaskResults = LambdaCloudDriverTaskResults.builder()
                .status(LambdaVerificationStatusOutput.builder()
                        .completed(true)
                        .failed(true)
                        .status("TERMINAL")
                        .build())
                .errors(LambdaCloudDriverErrorObject.builder()
                        .message(taskVerifyError)
                        .build())
                .build();
        Mockito.when(lambdaCloudDriverUtilsMock
                .verifyStatus(Mockito.any())).thenReturn(lambdaCloudDriverTaskResults);

        assertEquals(ExecutionStatus.TERMINAL, lambdaTrafficUpdateVerificationTask.execute(stageExecution).getStatus());
        assertEquals(taskVerifyError, stageExecution.getOutputs().get("failureMessage"));
    }

    @Test
    public void execute_UpdateVerification_validateWeights(){
        //RoutingConfig() will be null ending while loop
        List<AliasConfiguration> aliasConfigurationList = ImmutableList.of(new AliasConfiguration().withName("develop"));

        LambdaCloudDriverTaskResults lambdaCloudDriverTaskResults = LambdaCloudDriverTaskResults.builder()
                .status(LambdaVerificationStatusOutput.builder()
                        .status("RUNNING")
                        .completed(true)
                        .build())
                .build();
        Mockito.when(lambdaCloudDriverUtilsMock
                .verifyStatus(Mockito.any())).thenReturn(lambdaCloudDriverTaskResults);
        LambdaDefinition lambdaDefinition = LambdaDefinition.builder()
                .aliasConfigurations(aliasConfigurationList)
                .build();
        Mockito.when(config
                .getCloudDriverRetrieveNewPublishedLambdaWait()).thenReturn(40);
        Mockito.when(config
                .getCacheRefreshRetryWaitTime()).thenReturn(15);
        Mockito.when(config
                .getCloudDriverRetrieveMaxValidateWeightsTime()).thenReturn(240);
        Mockito.when(lambdaCloudDriverUtilsMock
                .retrieveLambdaFromCache(stageExecution, false)).thenReturn(lambdaDefinition);

        assertEquals(ExecutionStatus.SUCCEEDED, lambdaTrafficUpdateVerificationTask.execute(stageExecution).getStatus());
    }

}
