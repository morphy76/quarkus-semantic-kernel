package io.quarkiverse.semantickernel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.quarkiverse.semantickernel.SemanticKernelConfiguration.KernelConfig.ClientConfig;
import io.quarkiverse.semantickernel.semanticfunctions.SemanticFunctionConfiguration;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.semantic-kernel")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface SemanticKernelConfiguration {

    /**
     * Configuration properties for the client Semantic Kernel can connect to (openai, azureopenai).
     */
    Optional<ClientConfig> client();

    /**
     * Preload the kernel with semantic functions.
     */
    Optional<SemanticFunctionLibrary> semanticFunctions();

    /**
     * Named kernels.
     */
    Map<String, KernelConfig> kernels();

    /**
     * Semantic function library by configuration.
     */
    Optional<SemanticFunctionConfiguration> semanticFunctionLibrary();

    interface KernelConfig {

        /**
         * Preload the kernel with semantic functions.
         */
        Optional<SemanticFunctionLibrary> semanticFunctions();

        interface ClientConfig {
            /**
             * Configuration properties for the OpenAI client
             */
            Optional<OpenAIConfig> openai();

            /**
             * Configuration properties for the Azure OpenAI client
             */
            Optional<AzureOpenAIConfig> azureopenai();
        }

        interface OpenAIConfig {
            /**
             * OpenAI API key
             */
            String key();

            /**
             * OpenAI organization ID
             */
            String organizationid();
        }

        interface AzureOpenAIConfig {
            /**
             * Azure OpenAI endpoint URL
             */
            String endpoint();

            /**
             * Azure OpenAI endpoint key
             */
            String key();

            /**
             * Azure OpenAI deployment name
             */
            String deploymentname();
        }
    }

    interface SemanticFunctionLibrary {
        /**
         * Lookup for packaged skills
         */
        Optional<String> fromDirectory();

        /**
         * Bind all the semantic functions matching the given tags.
         */
        Optional<List<String>> fromConfiguration();
    }
}
