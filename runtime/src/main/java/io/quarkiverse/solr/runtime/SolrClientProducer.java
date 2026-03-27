package io.quarkiverse.solr.runtime;

import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import io.quarkiverse.solr.runtime.observe.SolrClientProxy;
import io.quarkiverse.solr.runtime.observe.SolrMetrics;

@ApplicationScoped
public class SolrClientProducer {
    private static final Logger log = Logger.getLogger(SolrClientProducer.class);
    private final SolrRunTimeConfig runTimeConfig;
    private final ManagedExecutor managedExecutor;
    private SolrMetrics solrMetrics;

    public SolrClientProducer(SolrRunTimeConfig runTimeConfig, ManagedExecutor managedExecutor) {
        this.runTimeConfig = runTimeConfig;
        this.managedExecutor = managedExecutor;
    }

    public SolrMetrics getSolrMetrics() {
        return solrMetrics;
    }

    public void setSolrMetrics(SolrMetrics solrMetrics) {
        this.solrMetrics = solrMetrics;
    }

    @Produces
    @Dependent
    public SolrClient createProducer() {
        List<String> urls = runTimeConfig.url();
        boolean useCloud = urls.size() > 1 || runTimeConfig.cloud();
        SolrClient client = useCloud ? createCloudSolrClient(urls) : createJdkSolrClient(urls.get(0));
        return new SolrClientProxy(client, solrMetrics);
    }

    //TODO: Test Solr cloud mode
    private CloudHttp2SolrClient createCloudSolrClient(List<String> urls) {
        log.info("Created Solr cloud client with URLs: " + String.join(", ", urls));
        CloudSolrClient.Builder builder = new CloudHttp2SolrClient.Builder(urls)
                .withHttpClient(createJdkSolrClient(urls.get(0)));
        runTimeConfig.defaultCollection().ifPresent(builder::withDefaultCollection);
        runTimeConfig.canUseZkACLs().ifPresent(builder::canUseZkACLs);
        runTimeConfig.zkClientTimeout().ifPresent(t -> builder.withZkClientTimeout(t, TimeUnit.MILLISECONDS));
        runTimeConfig.collectionCacheTtl().ifPresent(t -> builder.withCollectionCacheTtl(t, TimeUnit.MILLISECONDS));
        runTimeConfig.parallelCacheRefreshes().ifPresent(builder::withParallelCacheRefreshes);
        runTimeConfig.retryExpiryTime().ifPresent(t -> builder.withRetryExpiryTime(t, TimeUnit.MILLISECONDS));
        runTimeConfig.zkConnectTimeout().ifPresent(t -> builder.withZkConnectTimeout(t, TimeUnit.MILLISECONDS));
        runTimeConfig.parallelUpdates().ifPresent(builder::withParallelUpdates);
        return builder.build();
    }

    private HttpJdkSolrClient createJdkSolrClient(String url) {
        HttpJdkSolrClient.Builder builder = new HttpJdkSolrClient.Builder(url)
                .withExecutor(managedExecutor)
                .useHttp1_1(runTimeConfig.http1())
                .withFollowRedirects(runTimeConfig.followRedirects());
        runTimeConfig.requestTimeout().ifPresent(t -> builder.withRequestTimeout(t, TimeUnit.MILLISECONDS));
        runTimeConfig.connectionTimeout().ifPresent(t -> builder.withConnectionTimeout(t, TimeUnit.MILLISECONDS));
        runTimeConfig.idleTimeout().ifPresent(t -> builder.withIdleTimeout(t, TimeUnit.MILLISECONDS));
        runTimeConfig.maxConnectionsPerHost().ifPresent(builder::withMaxConnectionsPerHost);
        runTimeConfig.proxy().ifPresent(p -> builder.withProxyConfiguration(p.host(), p.port(), p.socks4(), p.secure()));
        runTimeConfig.basicAuth().ifPresent(a -> builder.withBasicAuthCredentials(a.username(), a.password()));
        runTimeConfig.defaultCollection().ifPresent(builder::withDefaultCollection);
        log.info("Created Solr client with URL: " + url);
        return builder.build();
    }
}
