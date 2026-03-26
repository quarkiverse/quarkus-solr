package io.quarkiverse.solr.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.routing.RequestReplicaListTransformerGenerator;

import io.quarkiverse.solr.runtime.SolrBuildTimeConfig;
import io.quarkiverse.solr.runtime.SolrSetupRecorder;
import io.quarkiverse.solr.runtime.observe.SolrClientProxy;
import io.quarkiverse.solr.runtime.observe.SolrHealthCheck;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageConfigBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.metrics.MetricsFactoryConsumerBuildItem;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

class SolrProcessor {
    public static final String FEATURE = "solr";

    //TODO: Replace reflections on bean mapping?
    //TODO: Codestart template
    //TODO: Mutiny based api?

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    SyntheticBeanBuildItem solrClient(SolrSetupRecorder recorder) {
        RuntimeValue<SolrClientProxy> solrClient = recorder.createClient();
        return SyntheticBeanBuildItem
                .configure(SolrClientProxy.class)
                .addType(SolrClient.class)
                .scope(ApplicationScoped.class)
                .defaultBean()
                .runtimeValue(solrClient)
                .setRuntimeInit()
                .done();
    }

    @BuildStep
    HealthBuildItem addHealthCheck(SolrBuildTimeConfig solrBuildTimeConfig) {
        return new HealthBuildItem(SolrHealthCheck.class.getName(), solrBuildTimeConfig.healthEnabled());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    MetricsFactoryConsumerBuildItem addMetrics(SolrSetupRecorder recorder, BeanContainerBuildItem beanContainerBuildItem) {
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
