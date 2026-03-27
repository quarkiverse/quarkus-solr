package io.quarkiverse.solr.runtime.observe;

import java.io.IOException;
import java.io.Serial;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.NamedList;
import org.jboss.logging.Logger;

import io.quarkiverse.solr.runtime.SolrClientProducer;
import io.quarkus.arc.Arc;

public class SolrClientProxy extends SolrClient {
    private static final Logger log = Logger.getLogger(SolrClientProxy.class);
    private final SolrClient delegate;
    private transient SolrMetrics metrics;

    public SolrClientProxy(SolrClient delegate, SolrMetrics metrics) {
        this.delegate = delegate;
        this.metrics = metrics;
    }

    @Serial
    // SolrClient is serializable so we have to reset SolrMetrics after deserialization
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        metrics = Arc.container().instance(SolrClientProducer.class).get().getSolrMetrics();
    }

    @Override
    public NamedList<Object> request(SolrRequest<?> request, String collection) throws SolrServerException, IOException {
        long startTime = startLog();
        try {
            NamedList<Object> response = delegate.request(request, collection);
            successLog(request, response, startTime);
            return response;
        } catch (Exception e) {
            failureLog(request, e, startTime);
            throw e;
        }
    }

    private long startLog() {
        log.trace("Start solr request");
        return System.nanoTime();
    }

    private void successLog(SolrRequest<?> request, NamedList<Object> response, long startTime) {
        long endTime = System.nanoTime();
        long timeInMs = (endTime - startTime) / 1000000;
        log.debug("Successfully performed " + request.getRequestType() + " request taking " + timeInMs + " ms");
        log.trace("Request was " + print(request));
        log.trace("Response was " + response.jsonStr());
        if (metrics != null)
            metrics.updateSuccess(request.getRequestType(), timeInMs);
    }

    private void failureLog(SolrRequest<?> request, Exception e, long startTime) {
        long endTime = System.nanoTime();
        long timeInMs = (endTime - startTime) / 1000000;
        log.warn("Failed to perform " + request.getRequestType() + " request taking " + timeInMs + "ms: " + e.getMessage(), e);
        if (metrics != null)
            metrics.updateError(request.getRequestType());
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
