package io.quarkiverse.solr.observe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.health.SmallRyeHealthReporter;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

class HealthCheckTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    SmallRyeHealthReporter healthReporter;

    @Test
    void doesIncludeSolr() {
        JsonObject group = healthReporter.getReadiness().getPayload().getJsonArray("checks").getJsonObject(0);
        assertEquals("Solr Health Check", group.getString("name"));
        assertEquals("UP", group.getString("status"));
        assertEquals("dummy", group.getJsonObject("data").getString("collections"));
        assertEquals("false", group.getJsonObject("data").get("cloud").toString());
    }
}
