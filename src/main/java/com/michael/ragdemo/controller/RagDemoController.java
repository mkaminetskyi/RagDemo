package com.michael.ragdemo.controller;

import com.michael.ragdemo.dto.DocumentRequest;
import com.michael.ragdemo.dto.DocumentSearchResult;
import com.michael.ragdemo.dto.ProductDetails;
import com.michael.ragdemo.entity.Product;
import com.michael.ragdemo.service.ProductService;
import com.michael.ragdemo.service.ProductTools;
import com.michael.ragdemo.utils.DocumentUtils;
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
    public List<ProductDetails> searchProduct(@RequestParam("name") String name) {
        return productTools.findClosestProducts(name, 3);
    }

    @GetMapping("/ask-product-question")
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

    @GetMapping("/search-document")
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
    public void addDocumentToVectorStore(@RequestBody DocumentRequest request) {
        Document document = new Document(request.content());

        vectorStore.add(List.of(document));

        log.info("Successfully added document to vector store");
    }

    @PostMapping("/upload-csv-file")
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
    public ResponseEntity<String> deleteAllDocuments() {
        jdbcTemplate.execute("DELETE FROM vector_store");

        log.info("All documents deleted from vector store");

        return ResponseEntity.ok("All documents deleted");
    }
}