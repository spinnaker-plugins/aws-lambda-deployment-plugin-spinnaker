package com.amazon.aws.spinnaker.plugin.lambda;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class Config {

    @Value("${lambdaPluginConfig.cacheRefreshRetryWaitTime:15}")
    private int cacheRefreshRetryWaitTime;
    @Value("${lambdaPluginConfig.cacheOnDemandRetryWaitTime:15}")
    private int cacheOnDemandRetryWaitTime;
    @Value("${lambdaPluginConfig.cloudDriverPostRequestRetries:5}")
    private int cloudDriverPostRequestRetries;
    @Value("${lambdaPluginConfig.cloudDriverPostTimeoutSeconds:120}")
    private int cloudDriverPostTimeoutSeconds;

    //it’s the time the request for LambdaTrafficUpdateTask takes to show on aws
    //and start moving the weights around and the fastest average is 30-35 seconds
    @Value("${lambdaPluginConfig.cloudDriverRetrieveNewPublishedLambdaWaitSeconds:40}")
    private int cloudDriverRetrieveNewPublishedLambdaWaitSeconds;
    /*it’s the max time a lambda takes to finish moving the weights when it has provisioned concurrency
    the longest time average is 3 minutes with 20 seconds so the default value is 240 seconds (4 min)
    */
    @Value("${lambdaPluginConfig.cloudDriverRetrieveMaxValidateWeightsTimeSeconds:240}")
    private int cloudDriverRetrieveMaxValidateWeightsTimeSeconds;
}
