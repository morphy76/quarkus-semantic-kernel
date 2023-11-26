package io.quarkiverse.semantickernel.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.quarkiverse.semantickernel.SemanticKernelConfiguration;
import io.quarkiverse.semantickernel.SemanticKernelConfiguration.KernelConfig;
import io.quarkiverse.semantickernel.SemanticKernelConfiguration.KernelConfig.AzureOpenAIConfig;
import io.quarkiverse.semantickernel.SemanticKernelConfiguration.KernelConfig.ClientConfig;
import io.quarkiverse.semantickernel.SemanticKernelConfiguration.KernelConfig.OpenAIConfig;
import io.quarkiverse.semantickernel.SemanticKernelConfiguration.SemanticFunctionLibrary;
import io.quarkiverse.semantickernel.semanticfunctions.SemanticFunctionConfiguration;
import io.quarkiverse.semantickernel.semanticfunctions.SemanticFunctionConfiguration.Skill;
import io.quarkiverse.semantickernel.semanticfunctions.SemanticFunctionConfiguration.Skill.Function;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SemanticKernelConfigurationTest {

    @Inject
    SemanticKernelConfiguration configuration;

    @Nested
    public class Client {
        @Test
        @Disabled
        public void testConfiguration() {

            assertEquals(Optional.of("OPEN_AI_KEY"),
                    configuration.client().flatMap(ClientConfig::openai).map(OpenAIConfig::key));
            assertEquals(Optional.of("OPEN_AI_ORGANIZATION_ID"),
                    configuration.client().flatMap(ClientConfig::openai).map(OpenAIConfig::organizationid));

            assertEquals(Optional.of("AZURE_OPEN_AI_KEY"),
                    configuration.client().flatMap(ClientConfig::azureopenai).map(AzureOpenAIConfig::key));
            assertEquals(Optional.of("AZURE_OPEN_AI_ENDPOINT"),
                    configuration.client().flatMap(ClientConfig::azureopenai).map(AzureOpenAIConfig::endpoint));
            assertEquals(Optional.of("AZURE_OPEN_AI_DEPLOYMENT_NAME"),
                    configuration.client().flatMap(ClientConfig::azureopenai).map(AzureOpenAIConfig::deploymentname));

            assertEquals(Optional.of("IMPORT_DIRECTORY_DEFAULT"),
                    configuration.semanticFunctions().flatMap(SemanticFunctionLibrary::fromDirectory));
            assertEquals(Optional.of(List.of("IMPORT_TAGS_DEFAULT")),
                    configuration.semanticFunctions().flatMap(SemanticFunctionLibrary::fromConfiguration));

            Optional<KernelConfig> namedKernel = Optional.ofNullable(configuration.kernels().get("namedKernel"));

            assertEquals(Optional.of("IMPORT_DIRECTORY_NAMED"),
                    namedKernel.flatMap(KernelConfig::semanticFunctions).flatMap(SemanticFunctionLibrary::fromDirectory));
            assertEquals(Optional.of(List.of("IMPORT_TAGS_NAMED")),
                    namedKernel.flatMap(KernelConfig::semanticFunctions).flatMap(SemanticFunctionLibrary::fromConfiguration));
        }
    }

    @Nested
    public class SemanticFunction {
        @Test
        public void testConfiguration() {

            String testFunctionExpectedPrompt = "{{$input}}\n\nSummarize the content above in less than 140 characters.";
            Optional<Skill> skill = configuration.semanticFunctionLibrary().map(SemanticFunctionConfiguration::skills)
                    .flatMap(skills -> Optional.ofNullable(skills.get("TestSkill")));
            Optional<Function> function = skill.map(Skill::functions)
                    .flatMap(functions -> Optional.ofNullable(functions.get("TestFunction")));

            assertEquals(Optional.of(testFunctionExpectedPrompt), function.map(Function::prompt));
            assertEquals(Optional.of(List.of("IMPORT_TAGS")), function.map(Function::tags));
        }
    }
}
