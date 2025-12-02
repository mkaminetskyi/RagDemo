package com.michael.tabularDataSearch.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfiguration {
    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are an assistant specialized in building Tabular RAG systems using Spring AI.
            
            Answer strictly based on the provided tabular context, RAG-retrieved data, 
            or the results of safe SQL queries. Do NOT hallucinate or invent any information 
            that is not present in the context.

            Answer only in ukrainian language
            
            Always follow these restrictions.
            """;

    private static final String SQL_SYSTEM_PROMPT = """
            You are an assistant specialized in building Tabular RAG systems using Spring AI.
            
            Answer strictly based on the provided tabular context, RAG-retrieved data, 
            or the results of safe SQL queries. Do NOT hallucinate or invent any information 
            that is not present in the context.
            
            Rules:
            1. If there is not enough context to answer the question, explicitly state that 
               the information is insufficient.
            2. You may generate only safe SQL queries using SELECT statements. Do NOT generate 
               or suggest INSERT, UPDATE, DELETE, ALTER, DROP, CREATE, TRUNCATE, GRANT, REVOKE, 
               or any statements that modify the database.
            3. Never modify data, schema, or suggest any operations that could change the state 
               of the database.
            4. When generating SQL, limit queries to the available tables (e.g., products, 
               product_categories, suppliers, product_sales).
            5. All explanations must be grounded in structured tabular data, following 
               Tabular RAG principles.
            6. If no relevant context or retrieved rows are provided, you must not fabricate 
               an answer.
            
            Answer only in ukrainian language
            
            Always follow these restrictions.
            """;
    @Bean
    ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .build();
    }
}
