package io.quarkiverse.semantickernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.microsoft.semantickernel.SemanticKernelHttpSettings;
import com.microsoft.semantickernel.connectors.ai.openai.util.AzureOpenAISettings;
import com.microsoft.semantickernel.connectors.ai.openai.util.ClientType;
import com.microsoft.semantickernel.connectors.ai.openai.util.OpenAIClientProvider;
import com.microsoft.semantickernel.connectors.ai.openai.util.OpenAISettings;
import com.microsoft.semantickernel.exceptions.ConfigurationException;

import io.quarkiverse.semantickernel.SemanticKernelConfiguration.ClientConfig;
import io.quarkiverse.semantickernel.SemanticKernelConfiguration.OpenAIConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

public class SemanticKernelClientProducer {

    @Inject
    SemanticKernelConfiguration semanticKernelConfiguration;

    @Produces
    @ApplicationScoped
    public OpenAIAsyncClient produceOpenAIAsyncClient() throws ConfigurationException {

        if (semanticKernelConfiguration.client().isEmpty()) {
            // There is no Semantic Kernel configuration at the Quarkus level (not in the application.properties file, etc.)
            // We should return a default OpenAIAsyncClient and rely on the SK configuration itself
            return OpenAIClientProvider.getClient();
        } else {
            // OPEN AI
            if (semanticKernelConfiguration.client().get().openai().isPresent()) {
                // TBV Just quick and dirty hacks which need to be properly engineered
                Properties properties = new Properties();
                properties.put(OpenAISettings.getDefaultSettingsPrefix() + "." + OpenAISettings.getKeySuffix(),
                        semanticKernelConfiguration.client().flatMap(client -> client.openai().map(openai -> openai.key()))
                                .orElse(""));
                properties.put(OpenAISettings.getDefaultSettingsPrefix() + "." + OpenAISettings.getOpenAiOrganizationSuffix(),
                        semanticKernelConfiguration.client()
                                .flatMap(client -> client.openai().map(openai -> openai.organizationid())).orElse(""));
                semanticKernelConfiguration.client().flatMap(ClientConfig::openai).flatMap(OpenAIConfig::overrideEndpoint)
                        .ifPresent(url -> properties.put(OpenAISettings.getDefaultSettingsPrefix() + ".endpoint", url));
                return buildOpenAIClient((Map) properties);
            } else {
                // AZURE OPEN AI
                if (semanticKernelConfiguration.client().get().azureopenai().isPresent()) {
                    Properties properties = new Properties();
                    properties.put(AzureOpenAISettings.getDefaultSettingsPrefix() + "." + AzureOpenAISettings.getKeySuffix(),
                            semanticKernelConfiguration.client()
                                    .flatMap(client -> client.azureopenai().map(azureopenai -> azureopenai.key())));
                    properties.put(
                            AzureOpenAISettings.getDefaultSettingsPrefix() + "."
                                    + AzureOpenAISettings.getAzureOpenAiEndpointSuffix(),
                            semanticKernelConfiguration.client()
                                    .flatMap(client -> client.azureopenai().map(azureopenai -> azureopenai.endpoint())));
                    properties.put(
                            AzureOpenAISettings.getDefaultSettingsPrefix()
                                    + "." + AzureOpenAISettings.getAzureOpenAiDeploymentNameSuffix(),
                            semanticKernelConfiguration.client()
                                    .flatMap(client -> client.azureopenai().map(azureopenai -> azureopenai.deploymentname())));
                    return new OpenAIClientProvider((Map) properties, ClientType.AZURE_OPEN_AI).getAsyncClient();
                } else {
                    throw new ConfigurationException(
                            ConfigurationException.ErrorCodes.NO_VALID_CONFIGURATIONS_FOUND,
                            "quarkus.semantic-kernel.client property found, but no openai or azureopenai sub-properties found");
                }
            }

        }
    }

    // Do not use semantic kernel OpenAIClientProvider due to limitations which prevent to use mockserver for tests:
    // - to restore the OpenAIClientProvider, mockserver should run the http server using a shared CA
    // - the OpenAIClientProvider does not permit to override the HTTP pipeline to skip the TLS assumption when using KeyCredentials
    // - setting the endpoint actually creates an AzureOpenAIClient, hence the mockserver responses should be set accordingly
    private OpenAIAsyncClient buildOpenAIClient(Map<String, String> configuredSettings) throws ConfigurationException {
        OpenAISettings settings = new OpenAISettings(configuredSettings);
        try {
            settings.assertIsValid();
        } catch (ConfigurationException e) {
            throw e;
        }

        ClientOptions clientOptions = SemanticKernelHttpSettings.getUserAgent(configuredSettings);

        return new OpenAIClientBuilder()
                .credential(new KeyCredential(settings.getKey()))
                .clientOptions(clientOptions)
                .endpoint(configuredSettings.get(OpenAISettings.getDefaultSettingsPrefix() + ".endpoint"))
                .pipeline(createHttpPipeline(clientOptions))
                .buildAsyncClient();
    }

    private HttpPipeline createHttpPipeline(ClientOptions clientOptions) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new HttpLoggingPolicy(new HttpLogOptions()));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersFromContextPolicy());
        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(null, null, new RetryPolicy()));
        policies.add(new AddDatePolicy());
        policies.add(new CookiePolicy());

        return new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .clientOptions(clientOptions)
                .build();
    }
}
