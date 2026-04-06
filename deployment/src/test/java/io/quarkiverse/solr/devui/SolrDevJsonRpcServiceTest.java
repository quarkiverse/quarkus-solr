package io.quarkiverse.solr.devui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
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
            JsonValue result = client.send("quarkus-solr_getSolrAdminUrl");
            String url = ((JsonString) result).getString();
            assertThat(url, matchesPattern("^http://localhost:\\d+/solr$"));
        }
    }

    @Test
    void listCollections() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonArray result = client.send("quarkus-solr_listCollections").asJsonArray();
            assertEquals(1, result.size());
            assertEquals(COLLECTION, result.getString(0));
        }
    }

    @Test
    void getSchema() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonObject schema = client.send("quarkus-solr_getSchema", Map.of("collection", COLLECTION)).asJsonObject();
            //do not check full schema, but check that something is returned
            Assertions.assertEquals("id", schema.getString("uniqueKey"));
        }
    }

    @Test
    void indexSingleJsonDocument() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            String json = "{\"id\": \"index-single-1\"}";
            JsonObject result = client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json", json)).asJsonObject();
            assertEquals(0, result.getInt("status"));
        }
    }

    @Test
    void indexMultipleJsonDocuments() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            String json = "[{\"id\": \"index-multi-1\"}, {\"id\": \"index-multi-2\"}]";
            JsonObject result = client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json", json)).asJsonObject();
            assertEquals(0, result.getInt("status"));
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
            JsonArray result = client.send("quarkus-solr_search",
                    Map.of("collection", COLLECTION, "query", "id:" + docId)).asJsonArray();
            assertEquals(1, result.size());
            assertEquals(docId, result.getJsonObject(0).getString("id"));
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
            JsonArray result = client.send("quarkus-solr_search",
                    Map.of("collection", COLLECTION, "query", "*:*", "fq", "id:" + docId)).asJsonArray();
            assertEquals(1, result.size());
            assertEquals(docId, result.getJsonObject(0).getString("id"));
        }
    }

    @Test
    void searchWithSortClause() throws Exception {
        String docId = "0000-1";
        try (JsonRpcClient client = new JsonRpcClient()) {
            client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json", "{\"id\": \"" + docId + "\"}"));
        }
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonArray result = client.send("quarkus-solr_search",
                    Map.of("collection", COLLECTION, "query", "*:*", "sortClauses", "{\"id\": \"asc\"}")).asJsonArray();
            assertEquals(docId, result.getJsonObject(0).getString("id"));
        }
    }

    @Test
    void searchWithSortClauseDesc() throws Exception {
        String docId = "ZZZZZZZZZZZZ-1";
        try (JsonRpcClient client = new JsonRpcClient()) {
            client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json", "{\"id\": \"" + docId + "\"}"));
        }
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonArray result = client.send("quarkus-solr_search",
                    Map.of("collection", COLLECTION, "query", "*:*", "sortClauses", "{\"id\": \"desc\"}")).asJsonArray();
            assertEquals(docId, result.getJsonObject(0).getString("id"));
        }
    }

}
