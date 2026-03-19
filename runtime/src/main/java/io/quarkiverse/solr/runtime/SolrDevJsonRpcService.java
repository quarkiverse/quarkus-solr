package io.quarkiverse.solr.runtime;

import io.smallrye.common.annotation.NonBlocking;

public class SolrDevJsonRpcService {
    private final SolrRunTimeConfig solrRunTimeConfig;

    public SolrDevJsonRpcService(SolrRunTimeConfig solrRunTimeConfig) {
        this.solrRunTimeConfig = solrRunTimeConfig;
    }

    @NonBlocking
    public String getSolrAdminUrl() {
        return solrRunTimeConfig.url().get(0);
    }
}
