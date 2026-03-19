package io.quarkiverse.solr.deployment;

import java.util.function.BooleanSupplier;

import org.jboss.logging.Logger;

import io.quarkiverse.solr.runtime.SolrDevServicesConfig;
import io.quarkiverse.solr.runtime.SolrRunTimeConfig;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigUtils;

public class SolrDevServicesEnabled implements BooleanSupplier {
    private static final Logger log = Logger.getLogger(SolrDevServicesEnabled.class);
    private final SolrDevServicesConfig solrDevServicesConfig;
    private final DevServicesConfig devServicesConfig;
    private final LaunchMode launchMode;

    public SolrDevServicesEnabled(SolrDevServicesConfig solrDevServicesConfig, DevServicesConfig devServicesConfig,
            LaunchMode launchMode) {
        this.solrDevServicesConfig = solrDevServicesConfig;
        this.devServicesConfig = devServicesConfig;
        this.launchMode = launchMode;
    }

    @Override
    public boolean getAsBoolean() {
        if (!devServicesConfig.enabled()) {
            log.debug("Not starting devservices as globally disabled");
            return false;
        }
        if (!solrDevServicesConfig.enabled()) {
            log.debug("Not starting devservices as solr disabled");
            return false;
        }
        if (!launchMode.isDevServicesSupported()) {
            log.debug("Not starting devservices as launch mode is not supported");
            return false;
        }
        //TODO: is this even working (runtime config in a build time)? If not, remove it!
        if (ConfigUtils.isPropertyPresent(SolrRunTimeConfig.URL_CONFIG_KEY)) {
            log.debug("Not starting devservices as url has been provided");
            return false;
        }
        log.debug("Devservices for solr will be started");
        return true;
    }
}
