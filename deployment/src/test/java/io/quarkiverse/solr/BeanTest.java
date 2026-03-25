package io.quarkiverse.solr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;

class BeanTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(Bean.class));

    @Inject
    SolrClient solrClient;

    @Test
    void indexDocument() throws SolrServerException, IOException {
        Bean bean = new Bean(UUID.randomUUID().toString(), "Stephens Kindle Paperwhite");
        UpdateResponse updateResponse = solrClient.addBean("dummy", bean);
        UpdateResponse commitResponse = solrClient.commit("dummy");

        assertEquals(0, updateResponse.getStatus());
        assertEquals(0, commitResponse.getStatus());
    }

    @Test
    void queryDocument() throws SolrServerException, IOException {
        indexDocument();

        SolrQuery query = new SolrQuery("description_t:Stephens");
        query.addField("*");
        query.setSort("id", SolrQuery.ORDER.asc);
        QueryResponse response = solrClient.query("dummy", query);
        List<Bean> documents = response.getBeans(Bean.class);

        assertEquals(1, documents.size());
        assertEquals("Stephens Kindle Paperwhite", documents.get(0).description);
    }

    public static class Bean {
        @Field
        public String id;
        @Field("description_t")
        public String description;

        public Bean(String id, String description) {
            this.id = id;
            this.description = description;
        }

        @SuppressWarnings("unused") //Used internally by solr
        public Bean() {
        }
    }
}
