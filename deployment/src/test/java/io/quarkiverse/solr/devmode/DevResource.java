package io.quarkiverse.solr.devmode;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.apache.solr.client.solrj.SolrClient;

@Path("/dev")
public class DevResource {
    @Inject
    SolrClient solrClient;

    @GET
    public String test() throws Exception {
        Object status = solrClient.ping("dummy").getResponse().get("status");
        return "Hello " + status.toString();
    }
}
