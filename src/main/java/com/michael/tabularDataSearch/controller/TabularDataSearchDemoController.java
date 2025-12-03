package com.michael.tabularDataSearch.controller;

import com.michael.tabularDataSearch.entity.Product;
import com.michael.tabularDataSearch.service.ProductService;
import com.michael.tabularDataSearch.service.ProductTools;
import com.michael.tabularDataSearch.utils.SchemaDescriptions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
public class TabularDataSearchDemoController {
    private static final String RAG_SYSTEM_MESSAGE = """
            You are an assistant specialized in answering about data in database
            
            Answer strictly based on the provided tabular context, RAG-retrieved data. 
            Do NOT hallucinate or invent any information 
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

    private final JdbcTemplate jdbcTemplate;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ProductTools productTools;
    private final ProductService productService;

    @GetMapping("/chat/rag")
    public String chatWithRag(@RequestParam("question") String question) {
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(10)
                        .build()
        );

        if (documents == null) {
            documents = List.of();
        }

        String context = documents.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        String userMessage = """
                You are answering using the following context.
                
                Context:
                %s
                
                Question:
                %s
                """.formatted(context, question);

        log.info("LLM user message: \n {}", userMessage);

        return chatClient.prompt()
                .system(RAG_SYSTEM_MESSAGE)
                .user(userMessage)
                .call()
                .content();
    }

    @GetMapping("/chat/text-to-sql")
    public ResponseEntity<String> textToSql(@RequestParam("question") String question) {
        String schema = SchemaDescriptions.TABULAR_RAG_SCHEMA;

        String sql = Objects.requireNonNull(chatClient.prompt()
                        .user("Generate a SQL query for this schema and question. " +
                                "Return ONLY the SQL. Answer in Ukrainian. Schema: " + schema + " Question: " + question)
                        .call()
                        .content())
                .trim();

        sql = stripCodeFences(sql);
        String lower = sql.stripLeading().toLowerCase(Locale.ROOT);

        String llmInput;
        if (lower.startsWith("select")) {
            log.info("Executing generated SELECT SQL: {}", sql);

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            llmInput = "Question: " + question + "\nSQL: " + sql + "\nRows: " + rows;
        } else {
            log.warn("Executing NON-SELECT SQL from LLM (unsafe demo): {}", sql);

            jdbcTemplate.execute(sql);
            llmInput = "Question: " + question + "\nSQL: " + sql +
                    "\nNote: Non-SELECT SQL was executed against the database.";
        }

        String resultSummary = chatClient.prompt()
                .user(llmInput)
                .call()
                .content();

        return ResponseEntity.ok(resultSummary);
    }


    @GetMapping("/chat/text-to-sql-safe")
    public ResponseEntity<String> textToSqlSafe(@RequestParam("question") String question) {
        String schema = SchemaDescriptions.TABULAR_RAG_SCHEMA;

        String sql = Objects.requireNonNull(chatClient.prompt()
                        .user("Generate a safe SQL query for this schema and question. " +
                                "Return ONLY the SQL. Schema: " + schema + " Question: " + question)
                        .call()
                        .content())
                .trim();

        sql = stripCodeFences(sql);

        if (!isSelectQuery(sql)) {
            log.warn("Rejected unsafe SQL from model: {}", sql);

            return ResponseEntity.badRequest().body("Generated SQL is not a safe");
        }

        log.info("Executing generated safe SQL: {}", sql);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        String resultSummary = chatClient.prompt()
                .user("Question: " + question + "\nSQL: " + sql + "\nRows: " + rows)
                .system(SQL_SYSTEM_PROMPT)
                .call()
                .content();

        return ResponseEntity.ok(resultSummary);
    }

    @GetMapping("/chat/tool-calling")
    public String chatWithRagAndToolCalling(@RequestParam(value = "question") String question) {
        try {
            return chatClient.prompt()
                    .tools(productTools)
                    .user(question)
                    .call()
                    .content();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/add-products-to-vector-store")
    public ResponseEntity<String> addDatabaseInfoToVectorStore() {
        List<Product> products = productService.findAllProducts();

        if (products.isEmpty()) {
            return ResponseEntity.badRequest().body("No products found to embed");
        }

        List<Document> documents = products.stream()
                .map(this::createProductDocument)
                .toList();

        vectorStore.add(documents);

        return ResponseEntity.ok("Embedded " + documents.size() + " products into the vector store");
    }

    @DeleteMapping("/delete-all-embeddings")
    public ResponseEntity<String> deleteAllEmbeddings() {
        jdbcTemplate.execute("DELETE FROM embeddings");

        log.info("All embeddings are deleted");

        return ResponseEntity.ok("All embeddings are deleted");
    }

    private String stripCodeFences(String sqlResponse) {
        String cleaned = sqlResponse.trim();

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("```sql\\s*", "");
            cleaned = cleaned.replaceFirst("^```", "");
            int closingFenceIndex = cleaned.indexOf("```");
            if (closingFenceIndex >= 0) {
                cleaned = cleaned.substring(0, closingFenceIndex);
            }
        }

        if (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned.trim();
    }

    private boolean isSelectQuery(String sql) {
        String normalized = sql.trim();
        String lowerCaseSql = normalized.toLowerCase(Locale.ROOT);

        if (!lowerCaseSql.startsWith("select")) {
            return false;
        }

        List<String> forbiddenKeywords = List.of(
                "insert ", "update ", "delete ", "drop ", "alter ", "truncate ", "create ", " grant ", " revoke ");

        for (String keyword : forbiddenKeywords) {
            if (lowerCaseSql.contains(keyword)) {
                return false;
            }
        }

        return true;
    }

    private Document createProductDocument(Product product) {
        String content = """
                Назва: %s
                Категорія: %s
                Постачальник: %s
                Ціна: %d грн
                Залишок: %d шт.
                Опис: %s
                """.formatted(
                product.getName(),
                product.getCategory().getName(),
                product.getSupplier().getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getDescription());

        return new Document(content.trim(), Map.of(
                "productId", product.getId(),
                "productName", product.getName(),
                "category", product.getCategory().getName(),
                "supplier", product.getSupplier().getName()));
    }

    private String formatDocumentSource(Document document) {
        String name = (String) document.getMetadata().getOrDefault("productName", "Невідомий товар");
        String category = (String) document.getMetadata().getOrDefault("category", "-");
        String supplier = (String) document.getMetadata().getOrDefault("supplier", "-");

        return "%s (категорія: %s, постачальник: %s)".formatted(name, category, supplier);
    }

}
