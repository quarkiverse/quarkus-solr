package io.quarkiverse.solr.it.noext;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolrStartupTest {

    @Test
    void shouldStartWithoutExtraExtensions() {
        // Health extension must not be on the classpath
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("io.smallrye.health.SmallRyeHealth"));
        // Micrometer/Prometheus must not be on the classpath
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("io.micrometer.core.instrument.MeterRegistry"));
        // REST extension must not be on the classpath
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("io.quarkus.resteasy.reactive.server.runtime.ResteasyReactiveRecorder"));
    }
}
