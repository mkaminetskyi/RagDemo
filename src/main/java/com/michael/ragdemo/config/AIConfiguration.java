package com.michael.ragdemo.config;

import com.michael.ragdemo.service.ProductTools;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class AIConfiguration {

    @Bean
    public ChatClient ChatClientBean(ChatClient.Builder builder, VectorStore vectorStore,
                                     ProductTools productTools) {
       return builder.defaultSystem("Ти є асистент який допомагає з " +
                       "Answer only based on RAG or function calling data. If no context provided - do not provide answer.")
               .build();
    }
}
