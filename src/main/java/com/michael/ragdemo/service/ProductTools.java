package com.michael.ragdemo.service;

import com.michael.ragdemo.dto.ProductDetails;
import com.michael.ragdemo.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTools {
    private final ProductService productService;
    private final VectorStore vectorStore;

    @Tool(description = "Get Product details by name")
    public ProductDetails getProductDetails(String productName) {
        log.info("!Get Product details by name: {}", productName);

        Product product = productService.findProductByName(productName);

        log.info("Product {}", product.getName());

        if (product != null) {
            return new ProductDetails(product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getQuantity());
        } else {
            return new ProductDetails(0, "Not Found", 0, 0);
        }
    }

   // @Tool(description = "Find top K products by closest name")
    public List<ProductDetails> findClosestProducts(String productName, int topK) {
        log.info("Search products by similar name: {}, topK: {}", productName, topK);

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder().query(productName).topK(topK).build());

        return Objects.requireNonNull(documents).stream()
                .map(doc -> {
                    Number idNum = (Number) doc.getMetadata().get("productId");
                    if (idNum != null) {
                        Product product = productService.findProductById(idNum.intValue());
                        if (product != null) {
                            return new ProductDetails(product.getId(),
                                    product.getName(),
                                    product.getPrice(),
                                    product.getQuantity());
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

//    // @Tool(description = "Find product by closest name")
//    public ProductDetails findClosestProduct(String productName) {
//        log.info("Search product by similar name: {}", productName);
//
//        List<Document> documents = vectorStore.similaritySearch(
//                SearchRequest.builder().query(productName).topK(1).build());
//
//        if (!Objects.requireNonNull(documents).isEmpty()) {
//            Document doc = documents.getFirst();
//            Number idNum = (Number) doc.getMetadata().get("productId");
//            if (idNum != null) {
//                Product product = productService.findProductById(idNum.intValue());
//
//                if (product != null) {
//                    return new ProductDetails(product.getId(),
//                            product.getName(),
//                            product.getPrice(),
//                            product.getQuantity());
//                }
//            }
//        }
//
//        return new ProductDetails(0, "Not Found", 0, 0);
//    }
}
