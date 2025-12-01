package com.michael.tabularDataSearch.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfiguration {
    private static final String DEFAULT_SYSTEM_PROMPT = "Ти є асистент який допомагає з " +
            "Answer only based on RAG or function calling data. If no context provided - do not provide answer.";

    @Bean
    ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .build();
    }
}
