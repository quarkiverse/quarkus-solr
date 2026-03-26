package io.quarkiverse.solr.runtime;

import java.util.List;
import java.util.function.Consumer;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.jboss.logging.Logger;

import io.quarkiverse.solr.runtime.observe.SolrClientProxy;
import io.quarkiverse.solr.runtime.observe.SolrMetrics;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.runtime.metrics.MetricsFactory;

@Recorder
public class SolrSetupRecorder {
    private static final Logger log = Logger.getLogger(SolrSetupRecorder.class);
    private final RuntimeValue<SolrRunTimeConfig> runTimeConfig;

    public SolrSetupRecorder(RuntimeValue<SolrRunTimeConfig> runTimeConfig) {
        this.runTimeConfig = runTimeConfig;
    }

    public RuntimeValue<SolrClientProxy> createClient() {
        List<String> urls = runTimeConfig.getValue().url();
        if (runTimeConfig.getValue().cloud()) {
            SolrClient client = new CloudHttp2SolrClient.Builder(urls).build();
            log.info("Created Solr cloud client with URLs: " + String.join(", ", urls));
            return new RuntimeValue<>(new SolrClientProxy(client));
        } else {
            if (urls.size() != 1) {
                String msg = "Multiple URLs provided for non-cloud configuration. Please provide only one URL or set cloud=true.";
                throw new RuntimeException(msg);
            }
            String url = urls.get(0);
            SolrClient client = new HttpJdkSolrClient.Builder(url).build();
            log.info("Created Solr client with URL: " + url);
            return new RuntimeValue<>(new SolrClientProxy(client));
        }
    }

    public Consumer<MetricsFactory> registerMetrics(BeanContainer value) {
        return metricsFactory -> {
            //noinspection resource
            SolrClientProxy solrClientProxy = value.beanInstance(SolrClientProxy.class);
            solrClientProxy.registerMetrics(new SolrMetrics(metricsFactory));
        };
    }
}
