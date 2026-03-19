package io.quarkiverse.solr.runtime;

import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;
import org.jboss.logging.Logger;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class SolrSetupRecorder {
    private final RuntimeValue<SolrRunTimeConfig> runTimeConfig;
    private static final Logger log = Logger.getLogger(SolrSetupRecorder.class);

    public SolrSetupRecorder(RuntimeValue<SolrRunTimeConfig> runTimeConfig) {
        this.runTimeConfig = runTimeConfig;
    }

    public RuntimeValue<SolrClient> createClient() {
        List<String> urls = runTimeConfig.getValue().url();
        SolrClient client = new CloudHttp2SolrClient.Builder(urls).build();
        log.info("Created SolrClient with URL: " + String.join(", ", urls));
        return new RuntimeValue<>(client);
    }
}
