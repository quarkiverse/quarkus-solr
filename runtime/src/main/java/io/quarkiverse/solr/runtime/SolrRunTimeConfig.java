package io.quarkiverse.solr.runtime;

import java.util.List;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.solr")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface SolrRunTimeConfig {
    String URL_CONFIG_KEY = "quarkus.solr.url";

    /**
     * URL on which Solr is running.
     * <p>
     * If multiple URLs are given, the first one is used as the base URL
     * and the others are used for failover.
     */
    List<String> url();

    @Override
    String toString();
}
