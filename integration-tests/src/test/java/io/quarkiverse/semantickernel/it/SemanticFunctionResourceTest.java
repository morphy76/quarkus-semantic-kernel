package io.quarkiverse.semantickernel.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
public class SemanticFunctionResourceTest {

    @Test
    public void testSummarization() {
        given()
                .when().get("/semantic-function/summarize")
                .then()
                .statusCode(200)
                .body(is("The 2020 World Series was played in Texas at Globe Life Field in Arlington."));
    }

    @Test
    public void testJokeGeneration() {
        given()
                .when().get("/semantic-function/joke")
                .then()
                .statusCode(200)
                .body(is("Joke produced"));
    }
}
