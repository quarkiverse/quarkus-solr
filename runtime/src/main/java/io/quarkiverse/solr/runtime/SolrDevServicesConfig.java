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
     * a solr instance when running in Dev or Test mode and when Docker is running.
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

    /**
     * The value of the label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services looks for a container with the
     * {@code quarkus-dev-service-solr} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise it
     * starts a new container with the {@code quarkus-dev-service-solr} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared Vault instances.
     */
    @WithDefault("solr")
    String serviceName();

    /**
     * Indicates if the instance managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-solr} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @WithDefault("true")
    boolean shared();

    @Override
    String toString();
}
