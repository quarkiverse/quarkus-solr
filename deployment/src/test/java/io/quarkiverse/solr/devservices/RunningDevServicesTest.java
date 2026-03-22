package io.quarkiverse.solr.devservices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.apache.solr.client.solrj.SolrClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.DockerClientFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

import io.quarkiverse.solr.deployment.devservices.SolrDevContainer;
import io.quarkus.test.QuarkusUnitTest;

class RunningDevServicesTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    SolrClient solrClient;

    @Test
    void containerStarted() {
        DockerClient dockerClient = DockerClientFactory.lazyClient();
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .withLabelFilter(Map.of("org.testcontainers", "true", SolrDevContainer.DEV_SERVICE_LABEL, "true"))
                .exec();
        assertEquals(1, containers.size());
        Container container = containers.get(0);
        assertEquals("running", container.getState());
        assertEquals("solr:10.0.0", container.getImage());
        assertEquals(2, container.getPorts().length);
        assertEquals(8983, container.getPorts()[0].getPrivatePort());
        assertEquals(9983, container.getPorts()[1].getPrivatePort());
    }

    @Test
    void ping() throws Exception {
        Object status = solrClient.ping("dummy").getResponse().get("status");
        assertEquals("OK", status.toString());
    }
}
