package io.quarkiverse.solr.it;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ExtensionsTest {
    @Test
    void shouldStartWithExtensions() throws ClassNotFoundException {
        // Health extension must be on the classpath
        assertNotNull(Class.forName("io.smallrye.health.SmallRyeHealth"));
        // Micrometer/Prometheus not be on the classpath
        assertNotNull(Class.forName("io.micrometer.core.instrument.MeterRegistry"));
        // REST extension not be on the classpath
        assertNotNull(Class.forName("io.quarkus.resteasy.reactive.server.runtime.ResteasyReactiveRecorder"));
    }
}
