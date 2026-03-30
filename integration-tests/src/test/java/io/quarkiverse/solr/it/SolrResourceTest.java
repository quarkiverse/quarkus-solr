package io.quarkiverse.solr.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolrResourceTest {

    @Test
    void add() {
        given()
                .contentType("application/json")
                .body(new SolrDocument("1", "Test Document", "This is a test document"))
                .when().post("/solr")
                .then()
                .statusCode(204);
        given()
                .when().get("/solr")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is("1"))
                .body("[0].name", is("Test Document"))
                .body("[0].description", is("This is a test document"));
    }

    @Test
    void queryWithoutResult() {
        given()
                .when().get("/solr?query=notpresent")
                .then()
                .statusCode(200)
                .body(is("[]"));
    }
}
