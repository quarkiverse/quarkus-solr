package io.quarkiverse.solr.observe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.health.SmallRyeHealthReporter;

class HealthCheckFailedTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.solr.url", "https://invalid.com:8080")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    SmallRyeHealthReporter healthReporter;

    @Test
    void solrNotReachable() {
        JsonObject group = healthReporter.getReadiness().getPayload().getJsonArray("checks").getJsonObject(0);
        assertEquals("Solr Health Check", group.getString("name"));
        assertEquals("DOWN", group.getString("status"));
        assertNotNull(group.getJsonObject("data").getString("error"));
    }
}
