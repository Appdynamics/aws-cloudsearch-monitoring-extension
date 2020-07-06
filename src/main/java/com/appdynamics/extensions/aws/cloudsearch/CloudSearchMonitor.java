/*
 * Copyright (c) 2018 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.aws.cloudsearch;

import com.appdynamics.extensions.aws.SingleNamespaceCloudwatchMonitor;
import com.appdynamics.extensions.aws.cloudsearch.configuration.CloudSearchConfiguration;
import com.appdynamics.extensions.aws.cloudsearch.processors.CloudSearchMetricsProcessor;
import com.appdynamics.extensions.aws.collectors.NamespaceMetricStatisticsCollector;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import java.util.List;
import java.util.Map;


/**
 * Created by venkata.konala on 4/23/18.
 */
public class CloudSearchMonitor extends SingleNamespaceCloudwatchMonitor<CloudSearchConfiguration> {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CloudSearchMonitor.class);


    public CloudSearchMonitor(){
        super(CloudSearchConfiguration.class);
    }

    @Override
    protected NamespaceMetricStatisticsCollector getNamespaceMetricsCollector(CloudSearchConfiguration cloudSearchConfiguration) {

        MetricsProcessor metricsProcessor = createMetricsProcessor(cloudSearchConfiguration);
        return new NamespaceMetricStatisticsCollector.Builder(cloudSearchConfiguration.getAccounts(),
                cloudSearchConfiguration.getConcurrencyConfig(),
                cloudSearchConfiguration.getMetricsConfig(),
                metricsProcessor,
                cloudSearchConfiguration.getMetricPrefix())
                .withCredentialsDecryptionConfig(cloudSearchConfiguration.getCredentialsDecryptionConfig())
                .withProxyConfig(cloudSearchConfiguration.getProxyConfig())
                .build();
    }

    private MetricsProcessor createMetricsProcessor(CloudSearchConfiguration cloudSearchConfiguration){
        return new CloudSearchMetricsProcessor(cloudSearchConfiguration);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected String getDefaultMetricPrefix() {
        return "Custom Metrics|Amazon CloudSearch";
    }

    @Override
    public String getMonitorName() {
        return "AWSCloudSearchMonitor";
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return Lists.newArrayList();
    }
}
