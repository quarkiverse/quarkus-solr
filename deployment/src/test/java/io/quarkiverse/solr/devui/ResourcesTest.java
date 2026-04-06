package io.quarkiverse.solr.devui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import org.apache.solr.client.solrj.beans.Field;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;

class ResourcesTest {
    @RegisterExtension
    static final QuarkusDevModeTest unitTest = new QuarkusDevModeTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(Bean.class));

    @Test
    void getResource() throws Exception {
        try (JsonRpcClient client = new JsonRpcClient()) {
            JsonObject resource = client.send("resources_list").getJsonArray("resources").stream()
                    .map(JsonValue::asJsonObject)
                    .filter(r -> r.getString("name").equals("quarkus-solr_solrBeans"))
                    .findFirst()
                    .orElseThrow();
            String uri = resource.getString("uri");
            JsonObject beans = client.send("resources_read", Map.of("uri", uri));
            String solrBeansStr = beans.getJsonArray("contents").getJsonObject(0).getString("text");
            JsonObject solrBeans = Json.createReader(new StringReader(solrBeansStr)).readObject();
            JsonObject expected = Json.createObjectBuilder()
                    .add(Bean.class.getName(), Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                    .add("javaFieldName", "id")
                                    .add("solrName", "id"))
                            .add(Json.createObjectBuilder()
                                    .add("javaFieldName", "description")
                                    .add("solrName", "description_t")))
                    .build();
            assertEquals(expected, solrBeans);
        }
    }

    @SuppressWarnings("unused") //Used via reflection
    public static class Bean {
        @Field
        public String id;
        @Field("description_t")
        public String description;

        public Bean(String id, String description) {
            this.id = id;
            this.description = description;
        }

        @SuppressWarnings("unused") //Used internally by solr
        public Bean() {
        }
    }

}
