package io.quarkiverse.solr.devservices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.solr.client.solrj.SolrClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;

class RunningDevServicesTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    SolrClient solrClient;

    @Test
    void ping() throws Exception {
        Object status = solrClient.ping("dummy").getResponse().get("status");
        assertEquals("OK", status.toString());
    }
}
