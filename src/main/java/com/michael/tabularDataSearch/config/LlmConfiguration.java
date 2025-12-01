package com.michael.tabularDataSearch.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LlmConfiguration {
    private static final String DEFAULT_SYSTEM_PROMPT = "Ти є асистент який допомагає з " +
            "Answer only based on RAG or function calling data. If no context provided - do not provide answer.";

    @Bean(name = "smartChatClient")
    ChatClient smartChat(
            ChatModel chatModel,
            @Value("${ai.models.smart.id}") String modelId,
            @Value("${ai.models.smart.temperature}") double temperature) {

        ChatOptions opts = ChatOptions.builder()
                .model(modelId)
                .temperature(temperature)
                .build();

        return ChatClient.builder(chatModel)
                .defaultOptions(opts)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .build();
    }

    @Primary
    @Bean(name = "cheapChatClient")
    ChatClient cheapChat(
            ChatModel chatModel,
            @Value("${ai.models.cheap.id}") String modelId,
            @Value("${ai.models.cheap.temperature}") double temperature) {

        ChatOptions opts = ChatOptions.builder()
                .model(modelId)
                .temperature(temperature)
                .build();

        return ChatClient.builder(chatModel)
                .defaultOptions(opts)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .build();
    }
}
