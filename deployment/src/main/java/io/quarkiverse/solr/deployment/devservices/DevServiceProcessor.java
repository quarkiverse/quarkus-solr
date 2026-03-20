package io.quarkiverse.solr.deployment.devservices;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.testcontainers.shaded.com.google.common.base.Strings;

import io.quarkiverse.solr.runtime.SolrDevServicesConfig;
import io.quarkus.builder.BuildException;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.devservices.common.StartableContainer;

@BuildSteps(onlyIf = SolrDevServicesEnabled.class)
public class DevServiceProcessor {
    public static final String DEV_SERVICE_LABEL = "quarkus-dev-service-solr";

    //TODO: Customize dev-service with setup or own config
    //TODO: Locate existing service

    @BuildStep
    public DevServicesResultBuildItem startDevContainer(SolrDevServicesConfig config) throws BuildException {
        String imageName = config.imageName().orElse("solr:" + getSolrjVersion());
        return DevServicesResultBuildItem.owned()
                .feature("solr")
                .serviceConfig(config.toString())
                .startable(() -> new StartableContainer<>(new SolrDevContainer(imageName, config)))
                .configProvider(Map.of("quarkus.solr.url", s -> s.getContainer().solrUrl()))
                .build();
    }

    private String getSolrjVersion() throws BuildException {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("properties-from-pom.properties")) {
            Properties properties = new Properties();
            properties.load(is);
            String solrjVersion = properties.getProperty("solrj.version");
            if (Strings.isNullOrEmpty(solrjVersion)) {
                throw new BuildException("solrj version is not specified in properties file");
            }
            return solrjVersion;
        } catch (IOException e) {
            throw new BuildException("Unable to read solrj version from properties file", e, Collections.emptyList());
        }
    }
}
