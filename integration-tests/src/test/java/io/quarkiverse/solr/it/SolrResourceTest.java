package io.quarkiverse.solr.it;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class SolrResourceTest {

    @Test
    void testHelloEndpoint() {
        given()
                .when().get("/solr")
                .then()
                .statusCode(200)
                .body(is("Hello solr"));
    }
}
