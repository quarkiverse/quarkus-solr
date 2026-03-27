package io.quarkiverse.solr.runtime;

public interface ProxyConfig {
    /**
     * The proxy host
     */
    String host();

    /**
     * The proxy port
     */
    int port();

    /**
     * Is this a socks4 proxy?
     */
    boolean socks4();

    /**
     * Is this proxy using HTTPS
     */
    boolean secure();
}
