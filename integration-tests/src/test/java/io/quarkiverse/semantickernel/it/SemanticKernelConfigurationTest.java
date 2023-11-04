package io.quarkiverse.semantickernel.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkiverse.semantickernel.SemanticKernelConfiguration;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SemanticKernelConfigurationTest {

    @Inject
    SemanticKernelConfiguration configuration;

    @Test
    //     @Disabled("To enable integration tests")
    public void testConfiguration() {
        assertEquals(Optional.of("OPEN_AI_KEY"),
                configuration.client().flatMap(client -> client.openai().map(openai -> openai.key())));
        assertEquals(Optional.of("OPEN_AI_ORGANIZATION_ID"),
                configuration.client().flatMap(client -> client.openai().map(openai -> openai.organizationid())));
        assertEquals(Optional.of("AZURE_OPEN_AI_KEY"),
                configuration.client().flatMap(client -> client.azureopenai().map(azureopenai -> azureopenai.key())));
        assertEquals(Optional.of("AZURE_OPEN_AI_ENDPOINT"),
                configuration.client().flatMap(client -> client.azureopenai().map(azureopenai -> azureopenai.endpoint())));
        assertEquals(Optional.of("AZURE_OPEN_AI_DEPLOYMENT_NAME"), configuration.client()
                .flatMap(client -> client.azureopenai().map(azureopenai -> azureopenai.deploymentname())));
    }
}
