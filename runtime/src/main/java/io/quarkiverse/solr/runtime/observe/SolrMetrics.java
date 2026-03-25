package io.quarkiverse.solr.runtime.observe;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.client.solrj.SolrRequest;

import io.quarkus.runtime.metrics.MetricsFactory;

public class SolrMetrics {
    private final AtomicInteger successRequestCounter = new AtomicInteger(0);
    private final AtomicInteger errorRequestCounter = new AtomicInteger(0);
    private final MetricsFactory.TimeRecorder timeRecorder;
    private final Map<SolrRequest.SolrRequestType, RequestTypeMetrics> metricsMap;

    public SolrMetrics(MetricsFactory metricsFactory) {
        metricsMap = Arrays.stream(SolrRequest.SolrRequestType.values())
                .collect(java.util.stream.Collectors.toMap(
                        type -> type,
                        type -> new RequestTypeMetrics(type, metricsFactory)));
        metricsFactory.builder("solr.requests")
                .description("Successful request to solr")
                .unit("requests")
                .buildCounter(successRequestCounter::get);
        metricsFactory.builder("solr.errors")
                .description("Failed requests to solr")
                .unit("requests")
                .buildCounter(errorRequestCounter::get);
        timeRecorder = metricsFactory.builder("solr.time")
                .description("Time taken for requests")
                .unit("milliseconds")
                .buildTimer();

    }

    public void updateSuccess(SolrRequest.SolrRequestType type, long timeTakenMillis) {
        metricsMap.get(type).updateSuccess(timeTakenMillis);
        successRequestCounter.incrementAndGet();
        timeRecorder.update(timeTakenMillis, TimeUnit.MILLISECONDS);
    }

    public void updateError(SolrRequest.SolrRequestType type) {
        metricsMap.get(type).updateError();
        errorRequestCounter.incrementAndGet();
    }

    private static class RequestTypeMetrics {
        private final AtomicInteger successRequestCounter = new AtomicInteger(0);
        private final AtomicInteger errorRequestCounter = new AtomicInteger(0);
        private final MetricsFactory.TimeRecorder timeRecorder;

        private RequestTypeMetrics(SolrRequest.SolrRequestType type, MetricsFactory metricsFactory) {
            metricsFactory.builder("solr." + type + ".requests")
                    .description("Successful " + type + " requests to solr")
                    .unit("requests")
                    .buildCounter(successRequestCounter::get);
            metricsFactory.builder("solr." + type + ".errors")
                    .description("Failed " + type + " requests to solr")
                    .unit("requests")
                    .buildCounter(errorRequestCounter::get);
            timeRecorder = metricsFactory.builder("solr." + type + ".time")
                    .description("Time taken for " + type + " requests")
                    .unit("milliseconds")
                    .buildTimer();
        }

        void updateSuccess(long timeTakenMillis) {
            timeRecorder.update(timeTakenMillis, TimeUnit.MILLISECONDS);
            successRequestCounter.incrementAndGet();
        }

        void updateError() {
            errorRequestCounter.incrementAndGet();
        }

    }
}
