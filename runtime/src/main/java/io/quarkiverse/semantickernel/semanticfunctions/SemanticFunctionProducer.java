package io.quarkiverse.semantickernel.semanticfunctions;

import java.util.Optional;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.exceptions.SkillsNotFoundException;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplate;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig.CompletionConfig;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig.CompletionConfigBuilder;
import com.microsoft.semantickernel.semanticfunctions.SemanticFunctionConfig;
import com.microsoft.semantickernel.skilldefinition.FunctionNotFound;

import io.quarkiverse.semantickernel.SemanticKernelConfiguration;
import io.quarkiverse.semantickernel.semanticfunctions.SemanticFunctionConfiguration.Skill;
import io.quarkiverse.semantickernel.semanticfunctions.SemanticFunctionConfiguration.Skill.Function;

public class SemanticFunctionProducer {

    @Inject
    SemanticKernelConfiguration configuration;

    @Produces
    @SemanticFunction
    public SemanticFunctionConfig buildFunction(InjectionPoint ip) {
        String skillName = getSkillName(ip);
        String functionName = getFunctionName(ip);

        return loadFromConfigurations(skillName, functionName);
    }

    private String getSkillName(InjectionPoint ip) {
        String rv = ip.getAnnotated().getAnnotation(SemanticFunction.class).skill();
        return rv != null && !rv.isBlank() ? rv : "default";
    }

    private String getFunctionName(InjectionPoint ip) {
        String rv = ip.getAnnotated().getAnnotation(SemanticFunction.class).function();
        return rv != null && !rv.isBlank() ? rv : "default";
    }

    private SemanticFunctionConfig loadFromConfigurations(String skillName, String functionName) {
        Skill skillConfiguration = lookupForSkill(skillName);
        Function functionConfiguration = lookupForFunction(functionName, skillConfiguration);

        CompletionConfig completionConfig = configureCompletion(functionConfiguration);
        PromptTemplateConfig tplConfig = new PromptTemplateConfig(completionConfig);

        PromptTemplate promptTemplate = SKBuilders.promptTemplate()
                .withPromptTemplate(functionConfiguration.prompt())
                .withPromptTemplateConfig(tplConfig)
                .withPromptTemplateEngine(SKBuilders.promptTemplateEngine().build()).build();

        return new SemanticFunctionConfig(tplConfig, promptTemplate);
    }

    private Function lookupForFunction(String functionName, Skill skillConfiguration) {
        return Optional.ofNullable(skillConfiguration.functions().get(functionName))
                .orElseThrow(() -> new FunctionNotFound(
                        com.microsoft.semantickernel.skilldefinition.FunctionNotFound.ErrorCodes.FUNCTION_NOT_FOUND,
                        "Missing configuration for the semantic function: " + functionName));
    }

    private Skill lookupForSkill(String skillName) {
        return configuration.semanticFunctionLibrary()
                .map(SemanticFunctionConfiguration::skills)
                .flatMap(skill -> Optional.ofNullable(skill.get(skillName)))
                .orElseThrow(() -> new SkillsNotFoundException(
                        com.microsoft.semantickernel.exceptions.SkillsNotFoundException.ErrorCodes.SKILLS_NOT_FOUND,
                        "Missing configuration for the skill: " + skillName));
    }

    private CompletionConfig configureCompletion(Function sfConfiguration) {
        CompletionConfigBuilder builder = new CompletionConfigBuilder();
        if (sfConfiguration.frequencyPenalty().isPresent()) {
            builder = builder.frequencyPenalty(sfConfiguration.frequencyPenalty().get());
        }
        if (sfConfiguration.maxTokens().isPresent()) {
            builder = builder.maxTokens(sfConfiguration.maxTokens().get());
        }
        if (sfConfiguration.presencePenalty().isPresent()) {
            builder = builder.presencePenalty(sfConfiguration.presencePenalty().get());
        }
        if (sfConfiguration.stopSequence().isPresent()) {
            builder = builder.stopSequences(sfConfiguration.stopSequence().get());
        }
        if (sfConfiguration.temperature().isPresent()) {
            builder = builder.temperature(sfConfiguration.temperature().get());
        }
        if (sfConfiguration.topP().isPresent()) {
            builder = builder.topP(sfConfiguration.topP().get());
        }
        return builder.build();
    }
}
