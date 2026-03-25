package io.quarkiverse.solr.deployment.devservices;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.net.URIBuilder;

import io.quarkiverse.solr.runtime.SolrDevServicesConfig;
import io.quarkus.deployment.builditem.Startable;
import io.quarkus.devservices.common.ConfigureUtil;

public class SolrDevContainer extends GenericContainer<SolrDevContainer> implements Startable {
    public static final Integer SOLR_PORT = 8983;
    public static final String DEFAULT_IMAGE_NAME = "solr";
    private static final String CUSTOM_CONFIGURATION_NAME = "CustomConfig";
    private static final Integer MANAGEMENT_PORT = 9983;
    private static final String WAIT_REGEX = ".*o\\.e\\.j\\.s\\.Server Started.*";
    private final HttpClient httpClient;
    private final SolrDevServicesConfig config;
    private final boolean useSharedNetwork;
    private final String hostName;

    public SolrDevContainer(SolrDevServicesConfig config, String defaultSolrVersion, boolean useSharedNetwork,
            String defaultNetworkId) {
        super(config.imageName().orElse(DEFAULT_IMAGE_NAME + ":" + defaultSolrVersion));
        this.config = config;
        this.httpClient = HttpClient.newBuilder().build();
        waitingFor(Wait.forLogMessage(WAIT_REGEX, 1).withStartupTimeout(Duration.ofMinutes(1)));
        withReuse(true);
        this.useSharedNetwork = useSharedNetwork;
        this.hostName = ConfigureUtil.configureNetwork(this, defaultNetworkId, useSharedNetwork, "solr");
    }

    public int getPort() {
        if (useSharedNetwork) {
            return SOLR_PORT;
        }
        return getMappedPort(SOLR_PORT);
    }

    @Override
    public String getHost() {
        return useSharedNetwork ? hostName : super.getHost();
    }

    @Override
    protected void configure() {
        super.configure();
        setCommand("solr start -f");
        if (useSharedNetwork) {
            return;
        }
        config.port().ifPresentOrElse(
                p -> addFixedExposedPort(SOLR_PORT, p),
                () -> addExposedPort(SOLR_PORT));
        addExposedPort(MANAGEMENT_PORT);
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        config.configuration().ifPresent(this::createConfiguration);
        createCollection();
    }

    private void createConfiguration(String configDir) {
        byte[] zip = ConfigurationFolderZipper.zipFolder(configDir);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "UPLOAD");
        parameters.put("name", CUSTOM_CONFIGURATION_NAME);
        create("configs", parameters, HttpRequest.BodyPublishers.ofByteArray(zip), "application/octet-stream");
    }

    private void createCollection() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "CREATE");
        parameters.put("name", config.collection().orElse("dummy"));
        parameters.put("numShards", "1");
        parameters.put("replicationFactor", "1");
        parameters.put("wt", "json");
        config.configuration().ifPresent(c -> parameters.put("collection.configName", CUSTOM_CONFIGURATION_NAME));
        create("collections", parameters, HttpRequest.BodyPublishers.noBody(), "text/plain");
    }

    private void create(String name, Map<String, String> parameters, HttpRequest.BodyPublisher bodyPublisher,
            String contentType) {
        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme("http")
                    .setHost(getHost())
                    .setPort(getPort())
                    .setPath("/solr/admin")
                    .appendPath(name);
            parameters.forEach(builder::addParameter);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(builder.build())
                    .header("Content-Type", contentType)
                    .POST(bodyPublisher);
            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300)
                throw new RuntimeException(
                        "Unable to create " + name + " (" + response.statusCode() + ") \n" + response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Unable to create " + name + ": " + e.getMessage(), e);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Unable to create " + name + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String getConnectionInfo() {
        return getHost() + ":" + getPort();
    }

    @Override
    public void close() {
        super.close();
    }
}
