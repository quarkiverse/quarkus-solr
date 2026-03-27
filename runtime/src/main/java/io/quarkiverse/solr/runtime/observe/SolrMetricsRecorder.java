package io.quarkiverse.solr.runtime.observe;

import java.util.function.Consumer;

import io.quarkiverse.solr.runtime.SolrClientProducer;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.runtime.metrics.MetricsFactory;

@Recorder
public class SolrMetricsRecorder {
    public Consumer<MetricsFactory> registerMetrics(BeanContainer container) {
        return metricsFactory -> {
            SolrClientProducer solrClientProducer = container.beanInstance(SolrClientProducer.class);
            solrClientProducer.setSolrMetrics(new SolrMetrics(metricsFactory));
        };
    }
}
