package io.quarkiverse.solr.devservices;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.ConnectException;

import org.apache.solr.client.solrj.SolrClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;

class DisabledDevServicesByUrlTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.solr.url", "http://localhost:8080")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    SolrClient solrClient;

    @Test
    void containerNotStarted() {
        assertThrows(ConnectException.class, () -> solrClient.ping("dummy"));
    }
}
