package io.quarkiverse.solr.deployment.devservices;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.net.URIBuilder;
import io.quarkiverse.solr.runtime.SolrDevServicesConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.google.common.base.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SolrDevContainer extends GenericContainer<SolrDevContainer> {
    public static final String DEV_SERVICE_LABEL = "quarkus-dev-service-solr";
    private static final String DEFAULT_IMAGE_NAME = "solr";
    private static final String CUSTOM_CONFIGURATION_NAME = "CustomConfig";
    private static final Integer SOLR_PORT = 8983;
    private static final Integer MANAGEMENT_PORT = 9983;
    private static final String WAIT_REGEX = ".*o\\.e\\.j\\.s\\.Server Started.*";
    private final HttpClient httpClient;
    private final SolrDevServicesConfig config;

    public SolrDevContainer(SolrDevServicesConfig config) {
        super(config.imageName().orElse(DEFAULT_IMAGE_NAME + ":" + getSolrjVersion()));
        this.config = config;
        this.httpClient = HttpClient.newBuilder().build();
        waitingFor(Wait.forLogMessage(WAIT_REGEX, 1).withStartupTimeout(Duration.ofMinutes(1)));
        withReuse(true);
        withLabel(DEV_SERVICE_LABEL, "true");
    }

    private static String getSolrjVersion() {
        try (InputStream is = SolrDevContainer.class.getClassLoader().getResourceAsStream("properties-from-pom.properties")) {
            Properties properties = new Properties();
            properties.load(is);
            String solrjVersion = properties.getProperty("solrj.version");
            if (Strings.isNullOrEmpty(solrjVersion)) {
                throw new RuntimeException("solrj version is not specified in properties file");
            }
            return solrjVersion;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read solrj version from properties file", e);
        }
    }

    @Override
    protected void configure() {
        config.port().ifPresentOrElse(
                p -> addFixedExposedPort(SOLR_PORT, p),
                () -> addExposedPort(SOLR_PORT));
        addExposedPort(MANAGEMENT_PORT);
        setCommand("solr start -f");
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
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUrl(List.of("admin", "configs"), parameters))
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofByteArray(zip))
                .build();
        execute(request, "create configuration");
    }

    private void createCollection() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "CREATE");
        parameters.put("name", config.collection().orElse("dummy"));
        parameters.put("numShards", "1");
        parameters.put("replicationFactor", "1");
        parameters.put("wt", "json");
        config.configuration().ifPresent(c -> parameters.put("collection.configName", CUSTOM_CONFIGURATION_NAME));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUrl(List.of("admin", "collections"), parameters))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        execute(request, "create collection");
    }

    private void execute(HttpRequest request, String action) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300)
                throw new RuntimeException("Unable to " + action + " (" + response.statusCode() + ") \n" + response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Unable to " + action, e);
        }
    }

    public String getSolrUrl() {
        return buildUrl(List.of(), Map.of()).toString();
    }

    private URI buildUrl(List<String> pathSegments, Map<String, String> parameters) {
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http");
            builder.setHost("localhost");
            builder.setPort(getMappedPort(SOLR_PORT));
            builder.appendPath("solr");
            pathSegments.forEach(builder::appendPath);
            parameters.forEach(builder::addParameter);
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Unable to build URL", e);
        }
    }
}
