package io.quarkiverse.solr.runtime;

import java.util.Optional;
import java.util.OptionalInt;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigGroup
@ConfigMapping(prefix = "quarkus.solr.devservices")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface SolrDevServicesConfig {
    /**
     * If DevServices has been explicitly enabled or disabled. DevServices is generally enabled
     * by default, unless there is an existing configuration present.
     * <p>
     * When DevServices is enabled Quarkus will attempt to automatically configure and start
     * a vault instance when running in Dev or Test mode and when Docker is running.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * The container image name to use, for container based DevServices providers.
     * <p>
     * If not set, the default solr image in the version of solrj will be used
     */
    Optional<String> imageName();

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    OptionalInt port();

    /**
     * The name of the collection to be created in Solr
     * <p>
     * If not defined, a collection names "dummy" will be created
     */
    Optional<String> collection();

    /**
     * Custom configuration to be used. Must be a directory on the classpath containing at least a solrconfig.xml
     * <p>
     * If not set, default configuration of SOLR docker container is used
     */
    Optional<String> configuration();

    @Override
    String toString();
}
