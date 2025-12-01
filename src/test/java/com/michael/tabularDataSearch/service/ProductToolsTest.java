package com.michael.tabularDataSearch.service;

import com.michael.tabularDataSearch.dto.ProductDetails;
import com.michael.tabularDataSearch.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductToolsTest {

    private ProductService productService = mock(ProductService.class);
    private VectorStore vectorStore = mock(VectorStore.class);
    private ProductTools productTools;

    @BeforeEach
    void setUp() {
        productTools = new ProductTools(productService, vectorStore);
    }

    @Test
    void returnsProductDetailsFromVectorMetadata() {
        Product product = buildProduct(1, "Laptop", 1299, 5);
        when(productService.findProductById(1)).thenReturn(product);

        Document scoredDoc = new Document("Laptop", Map.of("productId", 1));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(scoredDoc));

        List<ProductDetails> results = productTools.findClosestProducts("lap top", 2);

        assertThat(results).containsExactly(new ProductDetails(1, "Laptop", 1299, 5));
    }

    @Test
    void ignoresDocumentsWithoutResolvableIds() {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(new Document("Orphaned", Map.of())));

        List<ProductDetails> results = productTools.findClosestProducts("unknown", 3);

        assertThat(results).isEmpty();
    }

    @Test
    void forwardsSearchRequestToVectorStore() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        productTools.findClosestProducts("battery", 4);

        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(requestCaptor.capture());

        SearchRequest request = requestCaptor.getValue();
        assertThat(request.getQuery()).isEqualTo("battery");
        assertThat(request.getTopK()).isEqualTo(4);
    }

    private Product buildProduct(int id, String name, int price, int quantity) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        return product;
    }
}
