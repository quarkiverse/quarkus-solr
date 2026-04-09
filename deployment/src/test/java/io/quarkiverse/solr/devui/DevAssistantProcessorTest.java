package io.quarkiverse.solr.devui;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;

class DevAssistantProcessorTest {

    @RegisterExtension
    static final QuarkusDevModeTest test = new QuarkusDevModeTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource("DevAssistantProcessorTest/application.properties", "application.properties"));

    @Test
    void createQuery() throws Exception {
        // The chappie server subprocess takes a few seconds to start after Quarkus boots.
        // Retry with backoff until it is ready or the overall deadline is exceeded.
        RuntimeException lastError = null;
        for (int attempt = 0; attempt < 10; attempt++) {
            try (JsonRpcClient client = new JsonRpcClient()) {
                JsonValue result = client.send("quarkus-solr_createQuery",
                        Map.of("description",
                                "Search for all Persons in the collection having a first- or lastname similar to Peter"),
                        60);
                JsonObject response = result.asJsonObject();
                assertFalse(response.getString("query").isBlank(), "query field should not be blank");
                assertFalse(response.getString("example").isBlank(), "example field should not be blank");
                return;
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("ConnectException")) {
                    lastError = e;
                    Thread.sleep(3000);
                } else {
                    throw e;
                }
            }
        }
        throw lastError;
    }
}
