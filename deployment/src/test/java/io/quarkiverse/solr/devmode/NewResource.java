package io.quarkiverse.solr.devmode;

import org.apache.solr.client.solrj.SolrClient;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/new")
public class NewResource {
    @Inject
    SolrClient solrClient;

    @GET
    public String test() throws Exception {
        return solrClient.ping("dummy").getResponse().get("status").toString();
    }
}
