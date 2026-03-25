package io.quarkiverse.solr.runtime.observe;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.HealthCheckRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.jboss.logging.Logger;

import io.quarkiverse.solr.runtime.SolrRunTimeConfig;

@Readiness
public class SolrHealthCheck implements HealthCheck {
    private static final Logger log = Logger.getLogger(SolrHealthCheck.class);
    private static final String HEALTH_CHECK_NAME = "Solr Health Check";
    private final SolrClient solrClient;
    private final SolrRunTimeConfig runTimeConfig;

    public SolrHealthCheck(SolrClient solrClient, SolrRunTimeConfig runTimeConfig) {
        this.solrClient = solrClient;
        this.runTimeConfig = runTimeConfig;
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.builder()
                .name(HEALTH_CHECK_NAME)
                .withData("url", String.join(", ", runTimeConfig.url()))
                .withData("cloud", runTimeConfig.cloud());
        try {
            boolean overallHealth = getOverallHealth();
            if (!overallHealth) {
                return builder
                        .down()
                        .withData("error", "Overall Solr health check failed")
                        .build();
            }
            Set<String> collectionNames = getCollectionNames();
            boolean health = collectionNames.stream().allMatch(this::checkCollection);
            log.debug("Solr status overall is " + health);
            return builder
                    .status(health)
                    .withData("collections", String.join(", ", collectionNames))
                    .build();
        } catch (Exception e) {
            log.warn("Cannot communicate with solr", e);
            return builder
                    .down()
                    .withData("error", e.getClass().getName() + ": " + e.getMessage())
                    .build();
        }
    }

    private boolean getOverallHealth() throws SolrServerException, IOException {
        HealthCheckRequest request = new HealthCheckRequest();
        org.apache.solr.client.solrj.response.HealthCheckResponse response = request.process(solrClient);
        return response.getNodeStatus().equals("OK");
    }

    @SuppressWarnings("unchecked")
    private Set<String> getCollectionNames() throws SolrServerException, IOException {
        CollectionAdminRequest.List list = new CollectionAdminRequest.List();
        CollectionAdminResponse response = list.process(solrClient);
        return new HashSet<>(((java.util.List<String>) response.getResponse().get("collections")));
    }

    private boolean checkCollection(String collectionName) {
        try {
            int status = solrClient.ping(collectionName).getStatus();
            log.debug("Collection '" + collectionName + "' has status " + status);
            return status == 0;
        } catch (Exception e) {
            log.warn("Cannot check collection '" + collectionName + "'", e);
            return false;
        }
    }
}
