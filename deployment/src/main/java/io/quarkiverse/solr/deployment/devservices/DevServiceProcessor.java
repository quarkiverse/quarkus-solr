package io.quarkiverse.solr.deployment.devservices;

import static io.quarkus.devservices.common.ConfigureUtil.configureSharedServiceLabel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.testcontainers.shaded.com.google.common.base.Strings;

import io.quarkiverse.solr.runtime.SolrDevServicesConfig;
import io.quarkiverse.solr.runtime.SolrRunTimeConfig;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesComposeProjectBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devservices.common.ComposeLocator;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.LaunchMode;

@BuildSteps(onlyIf = SolrDevServicesEnabled.class)
public class DevServiceProcessor {
    public static final String DEV_SERVICE_LABEL = "quarkus-dev-service-solr";

    @BuildStep
    public DevServicesResultBuildItem startDevContainer(SolrDevServicesConfig config,
            DevServicesConfig devServicesConfig,
            DevServicesComposeProjectBuildItem composeProject,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            LaunchModeBuildItem launchMode) {
        boolean useSharedNetwork = DevServicesSharedNetworkBuildItem.isSharedNetworkRequired(devServicesConfig,
                devServicesSharedNetworkBuildItem);
        return locateLocaleContainer(config, launchMode.getLaunchMode())
                .or(() -> locateComposeContainer(config, composeProject, launchMode.getLaunchMode(), useSharedNetwork))
                .map(this::createFromAddress)
                .orElse(createNew(config, launchMode.getLaunchMode(), composeProject.getDefaultNetworkId(), useSharedNetwork));
    }

    private Optional<ContainerAddress> locateLocaleContainer(SolrDevServicesConfig config, LaunchMode launchMode) {
        return ContainerLocator
                .locateContainerWithLabels(SolrDevContainer.SOLR_PORT, DEV_SERVICE_LABEL)
                .locateContainer(config.serviceName(), config.shared(), launchMode);
    }

    private Optional<ContainerAddress> locateComposeContainer(SolrDevServicesConfig config,
            DevServicesComposeProjectBuildItem composeProject,
            LaunchMode launchMode,
            boolean useSharedNetwork) {
        String solrJVersion = getSolrjVersion();
        String imageName = config.imageName().orElse(SolrDevContainer.DEFAULT_IMAGE_NAME + ":" + solrJVersion);
        List<String> images = List.of(imageName);
        return ComposeLocator.locateContainer(composeProject, images, SolrDevContainer.SOLR_PORT, launchMode, useSharedNetwork);
    }

    private DevServicesResultBuildItem createFromAddress(ContainerAddress containerAddress) {
        //noinspection HttpUrlsUsage
        return DevServicesResultBuildItem.discovered()
                .feature("solr")
                .containerId(containerAddress.getId())
                .config(Map.of(
                        SolrRunTimeConfig.URL_CONFIG_KEY,
                        "http://" + containerAddress.getHost() + ":" + containerAddress.getPort() + "/solr"))
                .build();
    }

    private DevServicesResultBuildItem createNew(SolrDevServicesConfig config, LaunchMode launchMode, String defaultNetworkId,
            boolean useSharedNetwork) {
        String solrJVersion = getSolrjVersion();
        SolrDevContainer container = new SolrDevContainer(config, solrJVersion, useSharedNetwork, defaultNetworkId);
        configureSharedServiceLabel(container, launchMode, DEV_SERVICE_LABEL, config.serviceName());
        //noinspection HttpUrlsUsage
        return DevServicesResultBuildItem.owned()
                .feature("solr")
                .serviceConfig(config.toString())
                .serviceName(config.serviceName())
                .serviceConfig(config.toString())
                .startable(() -> container)
                .configProvider(Map.of(
                        SolrRunTimeConfig.URL_CONFIG_KEY,
                        s -> "http://" + container.getHost() + ":" + container.getPort() + "/solr"))
                .build();
    }

    private String getSolrjVersion() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("properties-from-pom.properties")) {
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
}
