package io.quarkiverse.solr.devui;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import jakarta.json.JsonObject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;

class SolrDevJsonRpcServiceTest {

    @RegisterExtension
    static final QuarkusDevModeTest test = new QuarkusDevModeTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    private static final String COLLECTION = "dummy";

    @Test
    void getSolrAdminUrl() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonObject result = client.send("quarkus-solr_getSolrAdminUrl");
            assertNotNull(result);
        }
    }

    @Test
    void listCollections() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonObject result = client.send("quarkus-solr_listCollections");
            assertNotNull(result);
        }
    }

    @Test
    void getSchema() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonObject schema = client.send("quarkus-solr_getSchema", Map.of("collection", COLLECTION));
            assertNotNull(schema);
            assertFalse(schema.isEmpty());
            assertEquals("id", schema.getString("uniqueKey"));
        }
    }

    @Test
    void indexSingleJsonDocument() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            String json = "{\"id\": \"index-single-1\"}";
            JsonObject result = client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json", json));
            assertNotNull(result);
        }
    }

    @Test
    void indexMultipleJsonDocuments() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            String json = "[{\"id\": \"index-multi-1\"}, {\"id\": \"index-multi-2\"}]";
            JsonObject result = client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json", json));
            assertNotNull(result);
        }
    }

    @Test
    void search() throws Exception {
        String docId = "search-basic-1";
        try (JsonRpcClient client = new JsonRpcClient()) {
            client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json", "{\"id\": \"" + docId + "\"}"));
        }
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonObject result = client.send("quarkus-solr_search",
                    Map.of("collection", COLLECTION, "query", "id:" + docId));
            assertNotNull(result);
        }
    }

    @Test
    void searchWithFilterQuery() throws Exception {
        String docId = "search-fq-1";
        try (JsonRpcClient client = new JsonRpcClient()) {
            client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json", "{\"id\": \"" + docId + "\"}"));
        }
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonObject result = client.send("quarkus-solr_search",
                    Map.of("collection", COLLECTION, "query", "*:*", "fq", "id:" + docId));
            assertNotNull(result);
        }
    }

    @Test
    void searchWithSortClause() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonObject result = client.send("quarkus-solr_search",
                    Map.of("collection", COLLECTION, "query", "*:*", "sortClauses", "{\"id\": \"asc\"}"));
            assertNotNull(result);
        }
    }
}
