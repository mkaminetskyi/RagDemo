package com.michael.tabularDataSearch.controller;

import com.michael.tabularDataSearch.entity.Product;
import com.michael.tabularDataSearch.repository.CustomerRepository;
import com.michael.tabularDataSearch.repository.ProductCategoryRepository;
import com.michael.tabularDataSearch.repository.PurchaseOrderRepository;
import com.michael.tabularDataSearch.repository.SupplierRepository;
import com.michael.tabularDataSearch.service.ProductService;
import com.michael.tabularDataSearch.service.ProductTools;
import com.michael.tabularDataSearch.utils.DocumentBuilders;
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
import java.util.stream.Stream;

@Slf4j
@RestController
@AllArgsConstructor
public class TabularDataSearchDemoController {
    private static final String RAG_SYSTEM_MESSAGE = """
            You are an assistant specialized in answering about data in database
            
            Answer strictly based on the provided tabular context, RAG-retrieved data. 
            Do NOT hallucinate or invent any information 
            that is not present in the context.
            
            Answer only in Ukrainian
            
            Always follow these restrictions.
            """;

    private static final String SQL_SYSTEM_PROMPT = """
            You are an assistant specialized in building Tabular RAG systems using Spring AI.
            
            Answer strictly based on the provided tabular context, RAG-retrieved data, 
            or the results of SQL queries. Do NOT hallucinate or invent any information 
            that is not present in the context.
            
            Rules:
            1. If there is not enough context to answer the question, explicitly state that 
               the information is insufficient.
            5. All explanations must be grounded in structured tabular data, following 
               Tabular RAG principles.
            6. If no relevant context or retrieved rows are provided, you must not fabricate 
               an answer.
            
            Answer only in Ukrainian
            
            Always follow these restrictions.
            """;

    private static final String SAFE_SQL_SYSTEM_PROMPT = """
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
               product_categories, suppliers, purchase_orders).
            5. All explanations must be grounded in structured tabular data, following 
               Tabular RAG principles.
            6. If no relevant context or retrieved rows are provided, you must not fabricate 
               an answer.
            
            Answer only in Ukrainian
            
            Always follow these restrictions.
            """;

    private static final String TOOL_CALLING_SYSTEM_MESSAGE = """
            You are an assistant specialized in answering about data in database
            
            Use defined tools to obtain information
            Do NOT hallucinate or invent any information that is not present in the context.
            
            Answer only in Ukrainian
            
            Always follow these restrictions.
            """;

    private final JdbcTemplate jdbcTemplate;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ProductTools productTools;
    private final ProductService productService;
    private final ProductCategoryRepository productCategoryRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    /**
     * Chat with LLM using RAG over DB data
     */
    @GetMapping("/chat/rag")
    public String chatWithRag(@RequestParam("question") String question) {
        // Obtain TopK most similar documents from DB
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(10)
                        .build()
        );

        if (documents == null) {
            documents = List.of();
        }

        // Create context using documents from DB
        String contextMessage = documents.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        // Create LLM input message
        String llmInput = """
                You are answering using the following context.
                
                Context:
                %s
                
                Question:
                %s
                """.formatted(contextMessage, question);

        log.info("LLM user message: \n {}", llmInput);

        // Send request to LLM
        return chatClient.prompt()
                .system(RAG_SYSTEM_MESSAGE)
                .user(llmInput)
                .call()
                .content();
    }

    /**
     * Chat with LLM using Text To SQL over DB data
     */
    @GetMapping("/chat/text-to-sql")
    public ResponseEntity<String> textToSql(@RequestParam("question") String question) {
        // Send LLM request to generate a SQL prompt
        String generatedSQL = Objects.requireNonNull(chatClient.prompt()
                        .user(" Generate a SQL query for this schema and question. " +
                                " Return ONLY the SQL. " +
                                " Schema: " + SchemaDescriptions.TABULAR_RAG_SCHEMA +
                                " Question: " + question)
                        .call()
                        .content())
                .trim();

        // Clean SQL from fences
        generatedSQL = stripCodeFences(generatedSQL);

        // Form final user message for LLM
        String llmInput;
        String lowerGeneratedSQL = generatedSQL.stripLeading().toLowerCase(Locale.ROOT);
        if (lowerGeneratedSQL.startsWith("select")) {
            log.info("Executing generated SELECT SQL: \n{}", lowerGeneratedSQL);

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(generatedSQL);

            llmInput = " Question: " + question +
                    " QL: " + generatedSQL +
                    " Rows: " + rows;
        } else {
            log.warn("Executing NON-SELECT SQL from LLM: \n{}", lowerGeneratedSQL);

            jdbcTemplate.execute(generatedSQL);

            llmInput = " Question: " + question +
                    " SQL: " + generatedSQL;
        }

        // Send request to LLM
        String resultSummary = chatClient.prompt()
                .system(SQL_SYSTEM_PROMPT)
                .user(llmInput)
                .call()
                .content();

        return ResponseEntity.ok(resultSummary);
    }

    /**
     * Chat with LLM using Safe Text To SQL over DB data
     */
    @GetMapping("/chat/text-to-sql-safe")
    public ResponseEntity<String> textToSqlSafe(@RequestParam("question") String question) {
        String generatedSQL = Objects.requireNonNull(chatClient.prompt()
                        .user("Generate a safe SQL query for this schema and question. " +
                                " Return ONLY the SQL. Schema: " + SchemaDescriptions.TABULAR_RAG_SCHEMA +
                                " Question: " + question)
                        .call()
                        .content())
                .trim();

        // Clean SQL from fences
        generatedSQL = stripCodeFences(generatedSQL);


        // !!! Check if SQL is safe
        if (!isSelectQuery(generatedSQL)) {
            log.warn("Rejected unsafe SQL from model: \n{}", generatedSQL);

            return ResponseEntity.badRequest().body("Generated SQL is not a safe");
        }

        log.info("Executing generated safe SQL: \n{}", generatedSQL);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(generatedSQL);
        String llmInput = " Question: " + question +
                " QL: " + generatedSQL +
                " Rows: " + rows;

        // Send request to LLM
        String resultSummary = chatClient.prompt()
                .user(llmInput)
                .system(SAFE_SQL_SYSTEM_PROMPT)
                .call()
                .content();

        return ResponseEntity.ok(resultSummary);
    }

    @GetMapping("/chat/tool-calling")
    public String chatWithToolCalling(@RequestParam(value = "question") String question) {
        try {
            return chatClient.prompt()
                    .system(TOOL_CALLING_SYSTEM_MESSAGE)
                    .user(question)
                    .tools(productTools)
                    .call()
                    .content();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/add-all-db-info-to-vector-store")
    public ResponseEntity<String> addDatabaseInfoToVectorStore() {
        List<Document> documents = Stream.of(
                        productCategoryRepository.findAll().stream().map(DocumentBuilders::createCategoryDocument),
                        supplierRepository.findAll().stream().map(DocumentBuilders::createSupplierDocument),
                        customerRepository.findAll().stream().map(DocumentBuilders::createCustomerDocument),
                        productService.findAllProducts().stream().map(DocumentBuilders::createProductDocument),
                        purchaseOrderRepository.findAll().stream().map(DocumentBuilders::createPurchaseOrderDocument)
                )
                .flatMap(s -> s)
                .toList();

        if (documents.isEmpty()) {
            return ResponseEntity.badRequest().body("No database rows found to embed");
        }

        vectorStore.add(documents);

        return ResponseEntity.ok("Embedded " + documents.size() + " records into the vector store");
    }

    @PostMapping("/add-products-to-vector-store")
    public ResponseEntity<String> addProductsInfoToVectorStore() {
        List<Product> products = productService.findAllProducts();

        if (products.isEmpty()) {
            return ResponseEntity.badRequest().body("No products found to embed");
        }

        List<Document> documents = products.stream()
                .map(DocumentBuilders::createProductDocument)
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
}
