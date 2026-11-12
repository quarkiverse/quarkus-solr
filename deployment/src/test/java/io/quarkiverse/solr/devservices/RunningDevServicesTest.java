package io.quarkiverse.solr.devservices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

import org.apache.solr.client.solrj.SolrClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

class RunningDevServicesTest {
    @RegisterExtension
    static final QuarkusExtensionTest unitTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    SolrClient solrClient;

    @Test
    void ping() throws Exception {
        Object status = solrClient.ping("dummy").getResponse().get("status");
        assertEquals("OK", status.toString());
    }
}
