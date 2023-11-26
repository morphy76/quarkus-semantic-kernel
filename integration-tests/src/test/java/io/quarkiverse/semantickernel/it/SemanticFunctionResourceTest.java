package io.quarkiverse.semantickernel.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SemanticFunctionResourceTest {

    @Test
    public void testSummarization() {
        given()
                .when().get("/semantic-function/summarize")
                .then()
                .statusCode(200)
                .body(is("Summarize completed"));
    }
}
