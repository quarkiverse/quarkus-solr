package io.quarkiverse.solr.runtime;

import java.util.List;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigGroup
@ConfigMapping(prefix = "quarkus.solr")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface SolrRunTimeConfig {
    String URL_CONFIG_KEY = "quarkus.solr.url";

    /**
     * Should solr could be used?
     */
    @WithDefault("false")
    boolean cloud();

    /**
     * URL on which Solr is running.
     * <p>
     * If multiple URLs are given, the first one is used as the base URL
     * and the others are used for failover.
     * If solr could is not used, only one URL can be defined.
     */
    List<String> url();

    @Override
    String toString();
}
