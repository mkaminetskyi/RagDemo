package com.michael.tabularDataSearch.controller;

import com.michael.tabularDataSearch.entity.Product;
import com.michael.tabularDataSearch.repository.CustomerRepository;
import com.michael.tabularDataSearch.repository.ProductCategoryRepository;
import com.michael.tabularDataSearch.repository.PurchaseOrderLineRepository;
import com.michael.tabularDataSearch.repository.PurchaseOrderRepository;
import com.michael.tabularDataSearch.repository.SupplierRepository;
import com.michael.tabularDataSearch.service.ProductService;
import com.michael.tabularDataSearch.service.ProductTools;
import com.michael.tabularDataSearch.utils.DatabaseSnapshotFormatter;
import com.michael.tabularDataSearch.utils.DocumentUtils;
import com.michael.tabularDataSearch.utils.SchemaDescriptions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@AllArgsConstructor
public class TabularDataSearchDemoController {
    private final JdbcTemplate jdbcTemplate;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ProductTools productTools;
    private final ProductService productService;
    private final ProductCategoryRepository productCategoryRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;

    @GetMapping("/chat/rag")
    public String chatWithRag(@RequestParam(value = "question") String question) {
        try {
            QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(
                            SearchRequest.builder()
                                    .topK(20)
                                    .build()
                    )
                    .build();

            return chatClient.prompt()
                    .advisors(qaAdvisor)
                    .user(question)
                    .call()
                    .content();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @GetMapping("/chat/text-to-sql")
    public ResponseEntity<String> textToSql(@RequestParam("question") String question) {
        try {
            String schema = SchemaDescriptions.TABULAR_RAG_SCHEMA;

            String sql = chatClient.prompt()
                    .user("Generate a safe SQL query for this schema and question. " +
                            "Return ONLY the SQL. Schema: " + schema + " Question: " + question)
                    .call()
                    .content()
                    .trim();

            sql = stripCodeFences(sql);

            if (!isSelectQuery(sql)) {
                log.warn("Rejected unsafe SQL from model: {}", sql);
                return ResponseEntity.badRequest().body("Generated SQL is not a safe SELECT query: " + sql);
            }

            log.info("Executing generated SQL: {}", sql);

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

            String resultSummary = chatClient.prompt()
                    .user("Question: " + question + "\nSQL: " + sql + "\nRows: " + rows)
                    .call()
                    .content();

            return ResponseEntity.ok(resultSummary);
        } catch (Exception e) {
            log.error("Text-to-SQL pipeline failed", e);

            return ResponseEntity.badRequest().body("Unable to answer with text-to-SQL: " + e.getMessage());
        }
    }

    @GetMapping("/chat/rag-and-tool")
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
    public ResponseEntity<String> addProductsToVectorStore() {
        List<Product> products = productService.findAllProducts();

        List<Document> documents = products.stream()
                .map(p -> new Document(p.getName(), Map.of("productId", p.getId())))
                .toList();

        vectorStore.add(documents);

        return ResponseEntity.ok("Successfully embedded and stored " +
                documents.size() + " products");
    }

    @PostMapping("/add-database-info-to-vector-store")
    public ResponseEntity<String> addDatabaseInfoToVectorStore() {
        StringBuilder snapshotBuilder = new StringBuilder();

        DatabaseSnapshotFormatter.appendCategories(snapshotBuilder, productCategoryRepository.findAll());
        DatabaseSnapshotFormatter.appendSuppliers(snapshotBuilder, supplierRepository.findAll());
        DatabaseSnapshotFormatter.appendProducts(snapshotBuilder, productService.findAllProducts());
        DatabaseSnapshotFormatter.appendCustomers(snapshotBuilder, customerRepository.findAll());
        DatabaseSnapshotFormatter.appendPurchaseOrders(snapshotBuilder, purchaseOrderRepository.findAll());
        DatabaseSnapshotFormatter.appendPurchaseOrderLines(snapshotBuilder, purchaseOrderLineRepository.findAll());

        String snapshot = snapshotBuilder.toString().trim();

        if (snapshot.isEmpty()) {
            return ResponseEntity.badRequest().body("No database content found to embed");
        }

        List<String> chunks = DocumentUtils.splitIntoChunks(snapshot, 50);

        AtomicInteger index = new AtomicInteger(1);
        List<Document> documents = chunks.stream()
                .map(chunk -> new Document(chunk, Map.of(
                        "source", "database",
                        "chunkIndex", index.getAndIncrement())))
                .toList();

        vectorStore.add(documents);

        return ResponseEntity.ok("Embedded " + documents.size() + " database chunks into the vector store");
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
