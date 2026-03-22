package io.quarkiverse.solr.deployment.devservices;

import java.util.Map;

import io.quarkiverse.solr.runtime.SolrDevServicesConfig;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.devservices.common.StartableContainer;

@BuildSteps(onlyIf = SolrDevServicesEnabled.class)
public class DevServiceProcessor {
    //TODO: Locate existing service

    @BuildStep
    public DevServicesResultBuildItem startDevContainer(SolrDevServicesConfig config) {
        return DevServicesResultBuildItem.owned()
                .feature("solr")
                .serviceConfig(config.toString())
                .startable(() -> new StartableContainer<>(new SolrDevContainer(config)))
                .configProvider(Map.of("quarkus.solr.url", s -> s.getContainer().getSolrUrl()))
                .build();
    }
}
