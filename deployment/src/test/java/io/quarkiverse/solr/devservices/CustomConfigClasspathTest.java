package io.quarkiverse.solr.devservices;

import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomConfigClasspathTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.solr.devservices.configuration", "/testconfig")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    private static final String ID = UUID.randomUUID().toString();

    @Inject
    SolrClient solrClient;

    @Test
    void ping() throws Exception {
        Object status = solrClient.ping("dummy").getResponse().get("status");
        assertEquals("OK", status.toString());
    }

    @Test
    void indexDocument() throws SolrServerException, IOException {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", ID);
        doc.addField("description", "Stephens Kindle Paperwhite");
        UpdateResponse updateResponse = solrClient.add("dummy", doc);
        UpdateResponse commitResponse = solrClient.commit("dummy");

        assertEquals(0, updateResponse.getStatus());
        assertEquals(0, commitResponse.getStatus());
    }

    @Test
    void queryDocument() throws SolrServerException, IOException {
        indexDocument();

        SolrQuery query = new SolrQuery("description:Stephens");
        query.addField("*");
        QueryResponse response = solrClient.query("dummy", query);
        SolrDocumentList documents = response.getResults();

        assertEquals(1, documents.getNumFound());
        assertEquals("Stephens Kindle Paperwhite", documents.get(0).getFirstValue("description"));
    }

    @Test
    void phoneticMatch() throws SolrServerException, IOException {
        indexDocument();

        SolrQuery query = new SolrQuery("description:Stefan");
        query.addField("*");
        QueryResponse response = solrClient.query("dummy", query);
        SolrDocumentList documents = response.getResults();

        //There is a phonetic matching, so this works
        assertEquals(1, documents.getNumFound());
        assertEquals("Stephens Kindle Paperwhite", documents.get(0).getFirstValue("description"));
    }
}
