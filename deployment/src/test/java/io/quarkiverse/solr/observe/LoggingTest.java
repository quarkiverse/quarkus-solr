package io.quarkiverse.solr.observe;

import static org.junit.jupiter.api.Assertions.*;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.solr.runtime.observe.SolrClientProxy;
import io.quarkus.test.InMemoryLogHandler;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;

class LoggingTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));
    private static final Logger rootLogger = LogManager.getLogManager().getLogger("io.quarkiverse.solr");
    InMemoryLogHandler inMemoryLogHandler;
    Level originalLevel = rootLogger.getLevel();

    @Inject
    SolrClient solrClient;

    @BeforeEach
    void setUp() {
        inMemoryLogHandler = new InMemoryLogHandler(r -> true);
        rootLogger.addHandler(inMemoryLogHandler);
        rootLogger.setLevel(Level.FINE);
    }

    @AfterEach
    void tearDown() {
        rootLogger.removeHandler(inMemoryLogHandler);
        rootLogger.setLevel(originalLevel);
    }

    @Test
    void successfulRequest() throws Exception {
        Object status = solrClient.ping("dummy").getResponse().get("status");
        assertEquals("OK", status.toString());
        assertEquals(1, inMemoryLogHandler.getRecords().size());
        LogRecord r = inMemoryLogHandler.getRecords().get(0);
        assertTrue(r.getMessage().startsWith("Successfully performed ADMIN request taking"));
        assertEquals(Level.FINE, r.getLevel());
        assertEquals(SolrClientProxy.class.getName(), r.getLoggerName());
    }

    @Test
    void failedRequest() {
        assertThrows(Exception.class, () -> solrClient.ping("nonExistingCollection"));
        assertEquals(1, inMemoryLogHandler.getRecords().size());
        LogRecord r = inMemoryLogHandler.getRecords().get(0);
        assertTrue(r.getMessage().startsWith("Failed to perform ADMIN request"));
        assertEquals(Level.WARNING, r.getLevel());
        assertEquals(SolrClientProxy.class.getName(), r.getLoggerName());
    }
}
