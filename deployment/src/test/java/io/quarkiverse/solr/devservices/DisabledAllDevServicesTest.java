package io.quarkiverse.solr.devservices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.DockerClientFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

import io.quarkiverse.solr.deployment.devservices.SolrDevContainer;
import io.quarkus.test.QuarkusUnitTest;

class DisabledAllDevServicesTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.devservices.enabled", "false")
            .overrideConfigKey("quarkus.solr.url", "http://localhost:8080")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Test
    void containerNotStarted() {
        DockerClient dockerClient = DockerClientFactory.lazyClient();
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .withLabelFilter(Map.of("org.testcontainers", "true", SolrDevContainer.DEV_SERVICE_LABEL, "true"))
                .exec();
        assertEquals(0, containers.size());
    }
}
