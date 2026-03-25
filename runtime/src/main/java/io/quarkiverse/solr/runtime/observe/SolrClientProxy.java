package io.quarkiverse.solr.runtime.observe;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.NamedList;
import org.jboss.logging.Logger;

public class SolrClientProxy extends SolrClient {
    private static final Logger log = Logger.getLogger(SolrClientProxy.class);
    private final SolrClient delegate;
    private transient SolrMetrics metrics;

    public SolrClientProxy(SolrClient delegate) {
        this.delegate = delegate;
    }

    public void registerMetrics(SolrMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public NamedList<Object> request(SolrRequest<?> request, String collection) throws SolrServerException, IOException {
        log.trace("Start solr request");
        long startNanos = System.nanoTime();
        try {
            NamedList<Object> response = delegate.request(request, collection);
            long endNanos = System.nanoTime();
            long timeInMs = (endNanos - startNanos) / 1000000;
            log.debug("Successfully performed " + request.getRequestType() + " request taking " + timeInMs + " ms");
            log.trace("Request was " + print(request));
            log.trace("Response was " + response.jsonStr());
            if (metrics != null)
                metrics.updateSuccess(request.getRequestType(), endNanos - startNanos);
            return response;
        } catch (Exception e) {
            log.warn("Failed to perform " + request.getRequestType() + " request: " + e.getMessage(), e);
            if (metrics != null)
                metrics.updateError(request.getRequestType());
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    private String print(SolrRequest<?> request) {
        return String.format(
                "apiVersion=%s, requestType=%s, method=%s, path=%s, params=%s, queryParams=%s, headers=%s, collection=%s, basicAuthUser=%s, basicAuthPassword=%s",
                request.getApiVersion(),
                request.getRequestType(),
                request.getMethod(),
                request.getPath(),
                request.getParams(),
                request.getQueryParams(),
                request.getHeaders(),
                request.getCollection(),
                request.getBasicAuthUser(),
                request.getBasicAuthPassword() == null ? null : "****");
    }
}
