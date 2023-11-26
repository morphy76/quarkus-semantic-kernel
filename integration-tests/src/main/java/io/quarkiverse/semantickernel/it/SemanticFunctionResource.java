/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.quarkiverse.semantickernel.it;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.orchestration.SKContext;
import com.microsoft.semantickernel.semanticfunctions.SemanticFunctionConfig;
import com.microsoft.semantickernel.textcompletion.CompletionSKFunction;
import com.microsoft.semantickernel.textcompletion.TextCompletion;

import io.quarkiverse.semantickernel.semanticfunctions.SemanticFunction;

@Path("/semantic-function")
@ApplicationScoped
public class SemanticFunctionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticFunctionResource.class);

    @Inject
    OpenAIAsyncClient client;

    @Inject
    // This semantic function comes from application properties
    @SemanticFunction(skill = "TestSkill", function = "TestFunction")
    SemanticFunctionConfig summarizeFunction;

    String textToSummarize = null;

    @PostConstruct
    void loadTextToSummarize() throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("textToSummarize.txt")) {
            textToSummarize = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @GET
    @Path("/summarize")
    public String summarize() {

        TextCompletion textCompletion = SKBuilders.chatCompletion().withOpenAIClient(client).withModelId("gpt-3.5-turbo")
                .build();
        Kernel kernel = SKBuilders.kernel().withDefaultAIService(textCompletion).build();

        CompletionSKFunction skFunction = kernel.registerSemanticFunction("TestSkill", "TestFunction", summarizeFunction);
        SKContext ctx = kernel.runAsync(textToSummarize, skFunction).block();

        LOGGER.info("Summary:");
        LOGGER.info(ctx.getResult());

        return "Summarize completed";
    }
}
