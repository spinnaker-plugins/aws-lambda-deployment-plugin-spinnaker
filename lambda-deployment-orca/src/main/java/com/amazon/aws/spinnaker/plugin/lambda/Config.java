package com.amazon.aws.spinnaker.plugin.lambda;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class Config {
    @Value("${lambdaPluginConfig.cloudDriverReadTimeout:60}")
    private int cloudDriverReadTimeout;
    @Value("${lambdaPluginConfig.cloudDriverConnectTimeout:15}")
    private int cloudDriverConnectTimeout;
    @Value("${lambdaPluginConfig.cacheRefreshRetryWaitTime:15}")
    private int cacheRefreshRetryWaitTime;
    @Value("${lambdaPluginConfig.cacheOnDemandRetryWaitTime:15}")
    private int cacheOnDemandRetryWaitTime;
    @Value("${lambdaPluginConfig.cloudDriverPostRequestRetries:5}")
    private int cloudDriverPostRequestRetries;
}
