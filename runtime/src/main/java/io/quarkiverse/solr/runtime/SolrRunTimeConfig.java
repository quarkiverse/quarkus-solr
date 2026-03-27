package io.quarkiverse.solr.runtime;

import java.util.List;
import java.util.Optional;

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
     * One or more Solr URLs, which the client then uses to send HTTP requests to Solr.
     * <p>
     * These URLS must point to the root Solr path (i.e. "/solr"). If multiple
     * URLs are configured, it is assumed that
     * <a href="https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html#solrcloud-mode">Solr Cloud
     * Mode</a>
     * is used and these URLs are used to fetch information about the layout
     * and health of the Solr cluster.
     * <p>
     * If only one URL is defined, the URLs is used to send user-provided requests using
     * {@link org.apache.solr.client.solrj.impl.HttpJdkSolrClient HttpJdkSolrClient}.
     */
    List<String> url();

    /**
     * Default collection to be used
     * <p>
     * Most SolrClient methods allow users to specify the collection or
     * core they wish to query, etc. as a String parameter. However,
     * continually specifying this parameter can become tedious, especially
     * for users who always work with the same collection.
     * <p>
     * Users can avoid this pattern by specifying a "default" collection
     * If specified SolrClients will use this default for making requests
     * whenever a collection or core is needed
     * (and no overriding value is specified)
     */
    Optional<String> defaultCollection();

    /**
     * Should HTTP 1.1 be used instead of HTTP/2?
     * <p>
     * If not set, HTTP/2 is used by default
     */
    @WithDefault("false")
    boolean http1();

    /**
     * Request timeout in milliseconds
     * <p>
     * If not set, idle timeout is used
     */
    Optional<Long> requestTimeout();

    /**
     * Idle timeout in milliseconds
     * <p>
     * If not set,
     * {@link org.apache.solr.client.solrj.impl.SolrHttpConstants#DEFAULT_SO_TIMEOUT SolrJ's default value}
     * is used
     */
    Optional<Long> idleTimeout();

    /**
     * Connection timeout in milliseconds
     * <p>
     * If not set,
     * {@link org.apache.solr.client.solrj.impl.SolrHttpConstants#DEFAULT_CONNECT_TIMEOUT SolrJ's default value}
     * is used
     */
    Optional<Long> connectionTimeout();

    /**
     * Should the client follow redirects?
     * <p>
     * If not set, redirect are not followed
     */
    @WithDefault("false")
    boolean followRedirects();

    /**
     * Maximal number of connections per host
     * <p>
     * If not set,
     * {@link org.apache.solr.client.solrj.impl.SolrHttpConstants#DEFAULT_MAXCONNECTIONS SolrJ's default value}
     * is used
     */
    Optional<Integer> maxConnectionsPerHost();

    /**
     * Basic Auth configuration for Solr
     * <p>
     * If not set, no basic auth is used
     */
    Optional<AuthConfig> basicAuth();

    /**
     * Proxy configuration for Solr
     * <p>
     * If not set, no proxy auth is used
     */
    Optional<ProxyConfig> proxy();

    /**
     * Force SolrCloud mode
     * <p>
     * If set, it is assumed that
     * <a href="https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html#solrcloud-mode">Solr Cloud
     * Mode</a>
     * is used even if only one {@link #url()} is defined
     */
    @WithDefault("false")
    boolean cloud();

    /**
     * Zookeeper client timeout in milliseconds
     * <p>
     * Only relevant if
     * <a href="https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html#solrcloud-mode">Solr Cloud
     * Mode</a>
     * is used even. If not set,
     * {@link org.apache.solr.client.solrj.impl.SolrZkClientTimeout#DEFAULT_ZK_CLIENT_TIMEOUT SolrJ's default value}
     * is used
     */
    Optional<Integer> zkClientTimeout();

    /**
     * Zookeeper connection timeout in milliseconds
     * <p>
     * Only relevant if
     * <a href="https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html#solrcloud-mode">Solr Cloud
     * Mode</a> is used. If not set,
     * {@link org.apache.solr.client.solrj.impl.SolrZkClientTimeout#DEFAULT_ZK_CONNECT_TIMEOUT SolrJ's default value}
     * is used
     */
    Optional<Integer> zkConnectTimeout();

    /**
     * Can Zookeepers ACL be used
     * <p>
     * Only relevant if
     * <a href="https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html#solrcloud-mode">Solr Cloud
     * Mode</a>
     * is used. See
     * <a href=
     * "https://solr.apache.org/guide/solr/latest/deployment-guide/zookeeper-access-control.html#about-zookeeper-acls">documentation</a>
     * for details. If not set,SolrJ's default value (true) is used.
     */
    Optional<Boolean> canUseZkACLs();

    /**
     * Cache ttl for cached objects in seconds
     * <p>
     * Only relevant if
     * <a href="https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html#solrcloud-mode">Solr Cloud
     * Mode</a> is used. If not set,SolrJ's default value (60s) is used.
     */
    Optional<Integer> collectionCacheTtl();

    /**
     * Number of parallel collection state refresh operations to run in parallel
     * <p>
     * Only relevant if
     * <a href="https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html#solrcloud-mode">Solr Cloud
     * Mode</a> is used. If not set,SolrJ's default value (5) is used.
     */
    Optional<Integer> parallelCacheRefreshes();

    /**
     * Time to wait to re-fetch the state after getting the same state version from Zookeeper in milliseconds
     * <p>
     * Only relevant if
     * <a href="https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html#solrcloud-mode">Solr Cloud
     * Mode</a> is used. If not set,SolrJ's default value (3000) is used.
     */
    Optional<Long> retryExpiryTime();

    /**
     * Should shard updates be sent in parallel
     * <p>
     * Only relevant if
     * <a href="https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html#solrcloud-mode">Solr Cloud
     * Mode</a> is used. When an UpdateRequest affects multiple shards,
     * CloudSolrClient splits it up and sends a request to each affected shard.
     * This setting chooses whether those sub-requests are sent serially or in parallel.
     * If not set,SolrJ's default value ('true') is used.
     */
    Optional<Boolean> parallelUpdates();

    @Override
    String toString();
}
