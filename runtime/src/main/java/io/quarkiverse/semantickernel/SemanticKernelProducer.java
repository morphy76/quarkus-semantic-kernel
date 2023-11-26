package io.quarkiverse.semantickernel;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.chatcompletion.ChatCompletion;
import com.microsoft.semantickernel.chatcompletion.ChatHistory;

public class SemanticKernelProducer {

    @Inject
    SemanticKernelConfiguration configuration;

    @Inject
    OpenAIAsyncClient client;

    @Produces
    public Kernel buildKernel() {
        // TODO: it should be determined by configuration of services and binding kernel
        ChatCompletion<ChatHistory> textCompletion = SKBuilders.chatCompletion()
                // .withStuffFromConfig
                .withOpenAIClient(client)
                .withModelId("gpt-3.5-turbo")
                .build();

        return SKBuilders.kernel()
                // .withStuffFromConfig
                .withDefaultAIService(textCompletion)
                .build();
    }

    // private Optional<CompletionSKFunction> loadFromDirectory(String skillName, String functionName) {
    //     Optional<String> directory = configuration.semanticFunction().flatMap(SemanticFunctionConfiguration::fromDirectory);
    //     if (directory.isPresent() && Files.exists(Path.of(directory.get(), skillName))) {
    //         kernel.importSkillsFromDirectory(directory.get(), skillName);
    //         boolean knownFunction = isKnownSemanticFunction(skillName, functionName);
    //         return knownFunction
    //                 ? Optional.of(kernel.getSkill(skillName).getFunction(functionName, CompletionSKFunction.class))
    //                 : Optional.empty();
    //     }
    //     return Optional.empty();
    // }

    // private boolean isKnownSemanticFunction(String skillName, String functionName) {
    //     Predicate<SKFunction<?>> knownCompletionPredicate = f -> {
    //         return f.getName().equals(functionName)
    //                 && CompletionRequestSettings.class.equals(f.getType());
    //     };
    //     return kernel.getSkills().asMap().containsKey(skillName)
    //             && kernel.getSkill(skillName).getAll().stream().anyMatch(knownCompletionPredicate);
    // }
}
