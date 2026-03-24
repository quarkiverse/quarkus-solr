package io.quarkiverse.solr.deployment;

import io.quarkiverse.solr.runtime.SolrBuildTimeConfig;
import io.quarkiverse.solr.runtime.SolrHealthCheck;
import io.quarkiverse.solr.runtime.SolrSetupRecorder;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.solr.client.solrj.SolrClient;

class SolrProcessor {
    public static final String FEATURE = "solr";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    //TODO: Native tests
    //TODO: Metrics
    //TODO: Codestart template
    //TODO: Replace reflections on bean mapping?
    //TODO: Mutiny based api?
    //TODO: Logging
    //TODO: Tracing context

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    SyntheticBeanBuildItem solrClient(SolrSetupRecorder recorder) {
        RuntimeValue<SolrClient> solrClient = recorder.createClient();
        return SyntheticBeanBuildItem
                .configure(SolrClient.class)
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
}
