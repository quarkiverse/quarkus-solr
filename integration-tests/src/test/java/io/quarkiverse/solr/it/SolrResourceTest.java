package io.quarkiverse.solr.it;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolrResourceTest {

    @Test
    void testHelloEndpoint() {
        given()
                .when().get("/solr")
                .then()
                .statusCode(200);
    }
}
