package com.michael.tabularDataSearch.controller;

import com.michael.tabularDataSearch.dto.DocumentRequest;
import com.michael.tabularDataSearch.dto.DocumentSearchResult;
import com.michael.tabularDataSearch.dto.ProductDetails;
import com.michael.tabularDataSearch.entity.Product;
import com.michael.tabularDataSearch.service.ProductService;
import com.michael.tabularDataSearch.service.ProductTools;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
public class RagDemoController {
    private final JdbcTemplate jdbcTemplate;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ProductTools productTools;
    private final ProductService productService;

    @GetMapping("/chatWithRag")
    // Demo: baseline RAG chat that augments the prompt with similar documents from the vector store
    public String chatWithRag(@RequestParam(value = "message") String message) {
        try {
            QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(
                            SearchRequest.builder()
                                   // .similarityThreshold(0.8d)
                                    .topK(10)
                                    .build()
                    )
                    .build();

            return chatClient.prompt()
                    .advisors(qaAdvisor)
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @GetMapping("/chatWithRagAndTool")
    // Demo: RAG + function calling – the model can call ProductTools to fetch relational data
    public String chatWithRagAndToolCalling(@RequestParam(value = "message") String message) {
        try {
            return chatClient.prompt()
                    .tools(productTools)
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @GetMapping("/search-product")
    // Demo: semantic lookup of nearby product names stored as embeddings in the vector store
    public List<ProductDetails> searchProduct(@RequestParam("name") String name) {
        return productTools.findClosestProducts(name, 3);
    }

    @GetMapping("/ask-product-question")
    // Demo: tabular grounding – the LLM receives product rows as context to answer catalog questions
    public String askProductQuestion(@RequestParam("question") String question) {
        List<ProductDetails> products = productTools.findClosestProducts(question, 20);

        StringBuilder promptBuilder = new StringBuilder("Product Info:\n");
        for (ProductDetails product : products) {
            promptBuilder.append("Name: ")
                    .append(product.name())
                    .append(", Price: ")
                    .append(product.price())
                    .append(", Quantity: ")
                    .append(product.quantity())
                    .append("\n");
        }
        promptBuilder.append("\n").append(question);

        return chatClient.prompt()
                .user(promptBuilder.toString())
                .call()
                .content();
    }

    @GetMapping("/text-to-sql")
    // Demo: text-to-SQL pipeline – generate SQL from a natural-language ask and summarize the result
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

    @GetMapping("/search-document")
    // Demo: similarity search over unstructured documents stored in the vector store
    public List<DocumentSearchResult> searchDocument(@RequestBody DocumentRequest request) {
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.builder()
                .query(request.content())
                .topK(3)
                .build());

        return Optional.ofNullable(similarDocuments)
                .orElse(List.of())
                .stream()
                .map(doc -> new
                        DocumentSearchResult(doc.getText(),
                        doc.getScore()))
                .toList();
    }

    @PostMapping("/add-document")
    // Demo: push raw text snippets into the vector store for later retrieval
    public void addDocumentToVectorStore(@RequestBody DocumentRequest request) {
        Document document = new Document(request.content());

        vectorStore.add(List.of(document));

        log.info("Successfully added document to vector store");
    }

    @PostMapping("/upload-csv-file")
    // Demo: ingest structured CSV rows, chunk them, and embed into the vector store
    public ResponseEntity<String> uploadCsvFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a file");
        }

        List<Document> documents = DocumentUtils.readCsvAsDocuments(file);

        vectorStore.add(documents);

        return ResponseEntity.ok("Successfully embedded and stored " +
                documents.size() + " chunks");
    }

    @PostMapping("/upload-text-file")
    // Demo: ingest a plain text file, chunk it, embed it, and store for RAG
    public ResponseEntity<String> uploadTextFile(@RequestParam("file") MultipartFile file)
            throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        List<String> chunks = DocumentUtils.splitIntoChunks(content, 40);

        List<Document> documents = chunks.stream()
                .map(Document::new)
                .toList();

        vectorStore.add(documents);

        return ResponseEntity.ok("Successfully embedded and stored " +
                documents.size() + " chunks");
    }

    @PostMapping("/add-products-to-vector-store")
    // Demo: vectorize relational rows (products) so they can participate in semantic search
    public ResponseEntity<String> addProductsToVectorStore() {
        List<Product> products = productService.findAllProducts();

        List<Document> documents = products.stream()
                .map(p -> new Document(p.getName(), Map.of("productId", p.getId())))
                .toList();

        vectorStore.add(documents);

        return ResponseEntity.ok("Successfully embedded and stored " +
                documents.size() + " products");
    }

    @PostMapping("/add-default-documents")
    // Demo: bootstrap vector store with toy facts for a quick RAG walkthrough
    public void addDefaultDocumentToVectorStore() {
        List<String> facts = List.of(
                "Albert Einstein was a physicist known for the theory of relativity.",
                "Marie Curie won two Nobel Prizes in Physics and Chemistry.",
                "Isaac Newton formulated the laws of motion and gravity.",
                "Nikola Tesla invented the alternating current system.",
                "Ada Lovelace is considered the first computer programmer.",
                "Galileo was an astronomer who supported heliocentrism.",
                "Leonardo da Vinci painted the Mona Lisa.",
                "Vincent van Gogh was a Dutch post-impressionist painter.",
                "Mozart was a prolific and influential composer of the classical era.",
                "Beethoven composed music even after losing his hearing.",
                "Einstein was not known for his musical talents.",
                "Curie did not work on computer science."
        );

        List<Document> documents = facts.stream()
                .map(Document::new)
                .toList();

        vectorStore.add(documents);

        log.info("Successfully added default documents to vector store");
    }

    @DeleteMapping("/delete-all-documents")
    // Demo: clear the vector store between scenarios to keep your talk deterministic
    public ResponseEntity<String> deleteAllDocuments() {
        jdbcTemplate.execute("DELETE FROM vector_store");

        log.info("All documents deleted from vector store");

        return ResponseEntity.ok("All documents deleted");
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
