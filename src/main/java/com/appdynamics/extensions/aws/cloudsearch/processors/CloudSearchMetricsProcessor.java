/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.cloudsearch.processors;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.DimensionFilter;
import com.appdynamics.extensions.aws.cloudsearch.configuration.CloudSearchConfiguration;
import com.appdynamics.extensions.aws.config.IncludeMetric;
import com.appdynamics.extensions.aws.dto.AWSMetric;
import com.appdynamics.extensions.aws.metric.*;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessorHelper;
import com.appdynamics.extensions.aws.predicate.MultiDimensionPredicate;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Satish Muddam
 */
public class CloudSearchMetricsProcessor implements MetricsProcessor {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CloudSearchMetricsProcessor.class);
    private static final String NAMESPACE = "AWS/CloudSearch";
    private List<IncludeMetric> includeMetrics;
    private CloudSearchConfiguration cloudSearchConfiguration;

    public CloudSearchMetricsProcessor(CloudSearchConfiguration cloudSearchConfiguration){
        this.cloudSearchConfiguration = cloudSearchConfiguration;
        this.includeMetrics = cloudSearchConfiguration.getMetricsConfig().getIncludeMetrics();
    }

    @Override
    public List<AWSMetric> getMetrics(AmazonCloudWatch awsCloudWatch, String accountName, LongAdder awsRequestsCounter) {
        List<DimensionFilter> dimensionFilters = getDimensionFilters();
        MultiDimensionPredicate multiDimensionPredicate = new MultiDimensionPredicate(cloudSearchConfiguration.getDimensions());
        return MetricsProcessorHelper.getFilteredMetrics(awsCloudWatch, awsRequestsCounter, NAMESPACE, includeMetrics, dimensionFilters, multiDimensionPredicate);
    }

    private List<DimensionFilter> getDimensionFilters(){
        List<DimensionFilter> dimensionFilters = Lists.newArrayList();
        for (com.appdynamics.extensions.aws.config.Dimension dimension : cloudSearchConfiguration.getDimensions()) {
            DimensionFilter dbDimensionFilter = new DimensionFilter();
            dbDimensionFilter.withName(dimension.getName());
            dimensionFilters.add(dbDimensionFilter);
        }
        return dimensionFilters;
    }

    @Override
    public StatisticType getStatisticType(AWSMetric metric) {
        return MetricsProcessorHelper.getStatisticType(metric.getIncludeMetric(), includeMetrics);
    }

    @Override
    public List<com.appdynamics.extensions.metrics.Metric> createMetricStatsMapForUpload(NamespaceMetricStatistics namespaceMetricStats) {
        List<com.appdynamics.extensions.metrics.Metric> stats = Lists.newArrayList();
        if(namespaceMetricStats != null){
            for(AccountMetricStatistics accountMetricStatistics : namespaceMetricStats.getAccountMetricStatisticsList()){
                for(RegionMetricStatistics regionMetricStatistics : accountMetricStatistics.getRegionMetricStatisticsList()){
                    for (MetricStatistic metricStatistic : regionMetricStatistics.getMetricStatisticsList()){
                        String metricPath = createMetricPath(accountMetricStatistics.getAccountName(), regionMetricStatistics.getRegion(), metricStatistic);
                        if(metricStatistic.getValue() != null){
                            Map<String, Object> metricProperties = Maps.newHashMap();
                            AWSMetric awsMetric = metricStatistic.getMetric();
                            IncludeMetric includeMetric = awsMetric.getIncludeMetric();
                            metricProperties.put("alias", includeMetric.getAlias());
                            metricProperties.put("multiplier", includeMetric.getMultiplier());
                            metricProperties.put("aggregationType", includeMetric.getAggregationType());
                            metricProperties.put("timeRollUpType", includeMetric.getTimeRollUpType());
                            metricProperties.put("clusterRollUpType ", includeMetric.getClusterRollUpType());
                            metricProperties.put("delta", includeMetric.isDelta());
                            com.appdynamics.extensions.metrics.Metric metric = new com.appdynamics.extensions.metrics.Metric(includeMetric.getName(), Double.toString(metricStatistic.getValue()), metricStatistic.getMetricPrefix() + metricPath, metricProperties);
                            stats.add(metric);
                        }
                        else{
                            logger.debug(String.format("Ignoring metric [ %s ] which has null value", metricPath));
                        }
                    }
                }
            }
        }
        return stats;
    }

    private String createMetricPath(String accountName, String region, MetricStatistic metricStatistic){
        AWSMetric awsMetric = metricStatistic.getMetric();
        IncludeMetric includeMetric = awsMetric.getIncludeMetric();
        com.amazonaws.services.cloudwatch.model.Metric metric = awsMetric.getMetric();
        String clientId = null;
        String domainName = null;

        for(Dimension dimension : metric.getDimensions()) {
            if(dimension.getName().equalsIgnoreCase("ClientId")) {
                clientId = dimension.getValue();
            }
            if(dimension.getName().equalsIgnoreCase("DomainName")) {
                domainName = dimension.getValue();
            }
        }

        StringBuilder stringBuilder = new StringBuilder(accountName)
                .append("|")
                .append(region)
                .append("|");
        if (clientId != null) {
            stringBuilder.append(clientId)
                    .append("|");
        }
        if (domainName != null) {
            stringBuilder.append(domainName)
                    .append("|");
        }
        stringBuilder.append(includeMetric.getName());
        return stringBuilder.toString();

    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }
}
