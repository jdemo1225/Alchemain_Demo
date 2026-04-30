package com.alchemain.rx.init;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.alchemain.rx.utils.PropertiesUtil;

public class SearchModule extends AbstractModule {

    private final static Logger log = LoggerFactory.getLogger(SearchModule.class);

    private final String CLIENT_HOSTS = "search.transport.hosts";
    private final String CLIENT_METHOD = "search.client.method";
    private final String CLUSTER_NAME = "search.cluster.name";

    @Override
    protected void configure() {
        bind(RestHighLevelClient.class).toInstance(esConnect());
        bind(SearchWrapper.class).in(Scopes.SINGLETON);
    }

    public RestHighLevelClient esConnect() {
        String transportMethod = PropertiesUtil.string(CLIENT_METHOD);
        String clusterName = PropertiesUtil.string(CLUSTER_NAME);
        if (clusterName == null)
            clusterName = "elasticsearch";

        log.trace("Creating ES Client:  cluster = {}, transport = {}", clusterName, transportMethod);

        String[] hosts = PropertiesUtil.string(CLIENT_HOSTS).split(",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            String[] constituents = hosts[i].split(":");
            try {
                httpHosts[i] = new HttpHost(InetAddress.getByName(constituents[0]), Integer.parseInt(constituents[1]), "http");
            } catch (UnknownHostException e) {
                log.error("Failed to add transport address: {}", e.getMessage());
            }
        }
        return new RestHighLevelClient(RestClient.builder(httpHosts));
    }
}

