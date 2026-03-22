package io.quarkiverse.solr.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.solr.client.solrj.SolrClient;

import io.quarkiverse.solr.runtime.SolrSetupRecorder;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.runtime.RuntimeValue;

class SolrProcessor {
    public static final String FEATURE = "solr";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    //TODO: Native tests
    //TODO: Health indicator
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
}
