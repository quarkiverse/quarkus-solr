package io.quarkiverse.solr.devmode;

import org.hamcrest.core.IsEqual;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.RestAssured;

class SolrDevModeTest {

    @RegisterExtension
    static final QuarkusDevModeTest test = new QuarkusDevModeTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(DevResource.class));

    @Test
    void testBeanChange() {
        RestAssured.given()
                .get("/dev")
                .then().assertThat().statusCode(200)
                .body(IsEqual.equalTo("Hello OK"));

        test.modifySourceFile("DevResource.java", s -> s.replace("Hello", "Bye"));

        RestAssured.given()
                .get("/dev")
                .then().assertThat().statusCode(200)
                .body(IsEqual.equalTo("Bye OK"));
    }

    @Test
    void testBeanAdd() {
        RestAssured.given()
                .get("/new")
                .then().assertThat().statusCode(404);

        test.addSourceFile(NewResource.class);

        RestAssured.given()
                .get("/new")
                .then().assertThat().statusCode(200)
                .body(IsEqual.equalTo("OK"));
    }
}
