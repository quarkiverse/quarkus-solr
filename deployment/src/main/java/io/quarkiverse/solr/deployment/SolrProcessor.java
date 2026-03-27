package io.quarkiverse.solr.deployment;

import org.apache.solr.client.solrj.routing.RequestReplicaListTransformerGenerator;

import io.quarkiverse.solr.runtime.SolrBuildTimeConfig;
import io.quarkiverse.solr.runtime.SolrClientProducer;
import io.quarkiverse.solr.runtime.observe.SolrHealthCheck;
import io.quarkiverse.solr.runtime.observe.SolrMetricsRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageConfigBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.metrics.MetricsFactoryConsumerBuildItem;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

class SolrProcessor {
    public static final String FEATURE = "solr";

    //TODO: Codestart template
    //TODO: Mutiny based api?

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem solrClientProducer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(SolrClientProducer.class)
                .build();
    }

    @BuildStep
    HealthBuildItem addHealthCheck(SolrBuildTimeConfig solrBuildTimeConfig) {
        return new HealthBuildItem(SolrHealthCheck.class.getName(), solrBuildTimeConfig.healthEnabled());
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    MetricsFactoryConsumerBuildItem addMetrics(SolrMetricsRecorder recorder, BeanContainerBuildItem beanContainerBuildItem) {
        return new MetricsFactoryConsumerBuildItem(recorder.registerMetrics(beanContainerBuildItem.getValue()));
    }

    @BuildStep
    NativeImageConfigBuildItem configureNative(BuildProducer<RuntimeInitializedClassBuildItem> producer) {
        return NativeImageConfigBuildItem.builder()
                //Static field of Random, has to be initialized at Runtime
                .addRuntimeInitializedClass(RequestReplicaListTransformerGenerator.class.getName())
                .build();
    }
}
