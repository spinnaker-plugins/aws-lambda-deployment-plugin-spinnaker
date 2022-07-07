package com.amazon.aws.spinnaker.plugin.lambda.upsert;

import com.amazon.aws.spinnaker.plugin.lambda.upsert.model.LambdaConcurrencyInput;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverResponse;
import com.amazon.aws.spinnaker.plugin.lambda.utils.LambdaCloudDriverUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({WiremockResolver.class, WiremockUriResolver.class})
public class LambdaDeleteConcurrencyTaskTest {

    WireMockServer wireMockServer;

    @InjectMocks
    private LambdaDeleteConcurrencyTask lambdaDeleteConcurrencyTask;

    @Mock
    private CloudDriverConfigurationProperties propsMock;

    @Mock
    private LambdaCloudDriverUtils lambdaCloudDriverUtilsMock;

    @Mock
    private StageExecution stageExecution;

    @Mock
    private PipelineExecution pipelineExecution;

    @BeforeEach
    void init(@WiremockResolver.Wiremock WireMockServer wireMockServer, @WiremockUriResolver.WiremockUri String uri) {
        this.wireMockServer = wireMockServer;
        MockitoAnnotations.initMocks(this);
        Mockito.when(propsMock.getCloudDriverBaseUrl()).thenReturn(uri);
        pipelineExecution.setApplication("lambdaApp");
        Mockito.when(stageExecution.getExecution()).thenReturn(pipelineExecution);
        Mockito.when(stageExecution.getContext()).thenReturn(new HashMap<String, Object>());
        Mockito.when(stageExecution.getOutputs()).thenReturn(new HashMap<String, Object>());
        LambdaConcurrencyInput ldi = LambdaConcurrencyInput.builder()
                .functionName("functionName")
                .build();
        Mockito.when(lambdaCloudDriverUtilsMock.validateUpsertLambdaInput(Mockito.any(), Mockito.anyList() )).thenReturn(true);
        Mockito.when(lambdaCloudDriverUtilsMock.getInput(stageExecution, LambdaConcurrencyInput.class)).thenReturn(ldi);
    }

    @Test
    public void execute_DeleteReservedConcurrency_SUCCEEDED(){
        Mockito.when(stageExecution.getType()).thenReturn("Aws.LambdaDeploymentStage");
        LambdaCloudDriverResponse lambdaCloudDriverResponse = LambdaCloudDriverResponse.builder()
                .resourceUri("/resourceUri")
                .build();
        Mockito.when(lambdaCloudDriverUtilsMock
                .postToCloudDriver(Mockito.any(), Mockito.any())).thenReturn(lambdaCloudDriverResponse);
        assertEquals(ExecutionStatus.SUCCEEDED, lambdaDeleteConcurrencyTask.execute(stageExecution).getStatus());
    }

    @Test
    public void execute_DeleteReservedConcurrency_NOTHING_TO_DELETE(){
        LambdaConcurrencyInput ldi = LambdaConcurrencyInput.builder()
                .functionName("functionName")
                .reservedConcurrentExecutions(10)
                .build();
        Mockito.when(lambdaCloudDriverUtilsMock.getInput(stageExecution, LambdaConcurrencyInput.class)).thenReturn(ldi);

        Mockito.when(stageExecution.getType()).thenReturn("Aws.LambdaDeploymentStage");
        assertEquals(ExecutionStatus.SUCCEEDED, lambdaDeleteConcurrencyTask.execute(stageExecution).getStatus());
        assertEquals("Lambda delete concurrency : nothing to delete", stageExecution.getOutputs().get("LambdaDeleteConcurrencyTask"));
    }

    @Test
    public void execute_DeleteProvisionedConcurrency_SUCCEEDED(){
        Mockito.when(stageExecution.getType()).thenReturn("Aws.LambdaTrafficRoutingStage");
        LambdaCloudDriverResponse lambdaCloudDriverResponse = LambdaCloudDriverResponse.builder()
                .resourceUri("/resourceUri")
                .build();
        Mockito.when(lambdaCloudDriverUtilsMock
                .postToCloudDriver(Mockito.any(), Mockito.any())).thenReturn(lambdaCloudDriverResponse);
        assertEquals(ExecutionStatus.SUCCEEDED, lambdaDeleteConcurrencyTask.execute(stageExecution).getStatus());
    }

    @Test
    public void execute_DeleteProvisionedConcurrency_NOTHING_TO_DELETE(){
        LambdaConcurrencyInput ldi = LambdaConcurrencyInput.builder()
                .functionName("functionName")
                .provisionedConcurrentExecutions(10)
                .build();
        Mockito.when(lambdaCloudDriverUtilsMock.getInput(stageExecution, LambdaConcurrencyInput.class)).thenReturn(ldi);

        Mockito.when(stageExecution.getType()).thenReturn("Aws.LambdaTrafficRoutingStage");
        assertEquals(ExecutionStatus.SUCCEEDED, lambdaDeleteConcurrencyTask.execute(stageExecution).getStatus());
        assertEquals("Lambda delete concurrency : nothing to delete", stageExecution.getOutputs().get("LambdaDeleteConcurrencyTask"));
    }

}
