package io.quarkiverse.solr.observe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import jakarta.inject.Inject;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.quarkus.test.QuarkusExtensionTest;

class MetricsTest {
    @RegisterExtension
    static final QuarkusExtensionTest unitTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    SolrClient solrClient;

    @Inject
    @SuppressWarnings("deprecation") // We need to use the same as quarkus...
    PrometheusMeterRegistry prometheusMeterRegistry;

    @Test
    void doesIncludeSolr() throws SolrServerException, IOException {
        Timer totalTimer = prometheusMeterRegistry.get("solr.time").timer();
        Timer adminTimer = prometheusMeterRegistry.get("solr.ADMIN.time").timer();
        FunctionCounter totalCounter = prometheusMeterRegistry.get("solr.requests").functionCounter();
        FunctionCounter adminCounter = prometheusMeterRegistry.get("solr.ADMIN.requests").functionCounter();
        assertEquals(0, totalCounter.count());
        assertEquals(0, adminCounter.count());
        solrClient.ping("dummy");
        assertEquals(1, totalCounter.count());
        assertEquals(1, adminCounter.count());
        assertEquals(1, totalTimer.count());
        assertEquals(1, adminTimer.count());
    }
}
