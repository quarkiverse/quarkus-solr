package io.quarkiverse.solr.devui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.UUID;

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

    private static String randomId() {
        return UUID.randomUUID().toString();
    }

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
            String json = "{\"id\": \"" + randomId() + "\", \"description_s\": \"index single document\"}";
            JsonObject result = client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json", json)).asJsonObject();
            assertEquals(0, result.getInt("status"));
        }
    }

    @Test
    void indexMultipleJsonDocuments() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            String json = "[{\"id\": \"" + randomId() + "\", \"description_s\": \"index multiple first\"}, "
                    + "{\"id\": \"" + randomId() + "\", \"description_s\": \"index multiple second\"}]";
            JsonObject result = client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json", json)).asJsonObject();
            assertEquals(0, result.getInt("status"));
        }
    }

    @Test
    void search() throws Exception {
        String description = "search-basic-" + randomId();
        try (JsonRpcClient client = new JsonRpcClient()) {
            client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json",
                            "{\"id\": \"" + randomId() + "\", \"description_s\": \"" + description + "\"}"));
        }
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonArray result = client.send("quarkus-solr_search",
                    Map.of("collection", COLLECTION, "query", "description_s:" + description)).asJsonArray();
            assertEquals(1, result.size());
            assertEquals(description, result.getJsonObject(0).getString("description_s"));
        }
    }

    @Test
    void searchWithFilterQuery() throws Exception {
        String description = "search-filter-" + randomId();
        try (JsonRpcClient client = new JsonRpcClient()) {
            client.send("quarkus-solr_indexJsonDocuments",
                    Map.of("collection", COLLECTION, "json",
                            "{\"id\": \"" + randomId() + "\", \"description_s\": \"" + description + "\"}"));
        }
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonArray result = client.send("quarkus-solr_search",
                    Map.of("collection", COLLECTION, "query", "*:*", "fq", "description_s:" + description)).asJsonArray();
            assertEquals(1, result.size());
            assertEquals(description, result.getJsonObject(0).getString("description_s"));
        }
    }

    @Test
    void searchWithSortClause() throws Exception {
        String prefix = "sortasc-" + randomId();
        try (JsonRpcClient client = new JsonRpcClient()) {
            String json = "[{\"id\": \"" + randomId() + "\", \"description_s\": \"" + prefix + "-aaa\"}, "
                    + "{\"id\": \"" + randomId() + "\", \"description_s\": \"" + prefix + "-zzz\"}]";
            client.send("quarkus-solr_indexJsonDocuments", Map.of("collection", COLLECTION, "json", json));
        }
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonArray result = client.send("quarkus-solr_search",
                    Map.of("collection", COLLECTION, "query", "*:*", "fq", "description_s:" + prefix + "-*",
                            "sortClauses", "{\"description_s\": \"asc\"}"))
                    .asJsonArray();
            assertEquals(prefix + "-aaa", result.getJsonObject(0).getString("description_s"));
        }
    }

    @Test
    void searchWithSortClauseDesc() throws Exception {
        String prefix = "sortdesc-" + randomId();
        try (JsonRpcClient client = new JsonRpcClient()) {
            String json = "[{\"id\": \"" + randomId() + "\", \"description_s\": \"" + prefix + "-aaa\"}, "
                    + "{\"id\": \"" + randomId() + "\", \"description_s\": \"" + prefix + "-zzz\"}]";
            client.send("quarkus-solr_indexJsonDocuments", Map.of("collection", COLLECTION, "json", json));
        }
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonArray result = client.send("quarkus-solr_search",
                    Map.of("collection", COLLECTION, "query", "*:*", "fq", "description_s:" + prefix + "-*",
                            "sortClauses", "{\"description_s\": \"desc\"}"))
                    .asJsonArray();
            assertEquals(prefix + "-zzz", result.getJsonObject(0).getString("description_s"));
        }
    }

}
