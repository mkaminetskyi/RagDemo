package com.michael.tabularDataSearch.service;

import com.michael.tabularDataSearch.dto.InventorySummary;
import com.michael.tabularDataSearch.dto.ProductDetails;
import com.michael.tabularDataSearch.entity.Customer;
import com.michael.tabularDataSearch.entity.Product;
import com.michael.tabularDataSearch.entity.PurchaseOrder;
import com.michael.tabularDataSearch.repository.CustomerRepository;
import com.michael.tabularDataSearch.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTools {
    private final ProductService productService;
    private final VectorStore vectorStore;
    private final CustomerRepository customerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Tool(description = "Get Product details by name")
    public ProductDetails getProductDetails(String productName) {
        log.info("!Get Product details by name: {}", productName);

        return Optional.ofNullable(productService.findProductByName(productName))
                .map(this::toProductDetails)
                .orElseGet(() -> new ProductDetails(0, "Not Found", 0, 0));
    }

    @Tool(description = "Find top K products by closest name")
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
                            return toProductDetails(product);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Tool(description = "Search products whose names contain the provided phrase")
    public List<ProductDetails> searchProductsByName(String phrase) {
        log.info("Searching products by name fragment: {}", phrase);

        return productService.findProductsByNameFragment(phrase)
                .stream()
                .map(this::toProductDetails)
                .toList();
    }

    @Tool(description = "List low-stock products at or below the threshold")
    public List<ProductDetails> listLowStock(int threshold) {
        log.info("Listing products at or below stock threshold {}", threshold);

        return productService.findLowStockProducts(threshold)
                .stream()
                .map(this::toProductDetails)
                .toList();
    }

    @Tool(description = "Find all products supplied by the given supplier name")
    public List<ProductDetails> productsBySupplier(String supplierName) {
        log.info("Finding products for supplier {}", supplierName);

        return productService.findProductsBySupplier(supplierName)
                .stream()
                .map(this::toProductDetails)
                .toList();
    }

    @Tool(description = "Update the price of a product by its ID")
    public ProductDetails updateProductPrice(int productId, int newPrice) {
        log.info("Updating price for product {} to {}", productId, newPrice);

        Product updatedProduct = productService.updateProductPrice(productId, newPrice);
        return toProductDetails(updatedProduct);
    }

    @Tool(description = "Summarize inventory by category with totals and averages")
    public List<InventorySummary> summarizeInventory() {
        log.info("Summarizing inventory by category");

        return productService.summarizeInventoryByCategory();
    }

    @Tool(description = "Create a purchase order for a customer and product with quantity")
    public PurchaseOrder createOrder(int customerId, int productId, int quantity) {
        log.info("Creating order for customer {} with product {} x{}", customerId, productId, quantity);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        Product product = Optional.ofNullable(productService.findProductById(productId))
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        PurchaseOrder order = new PurchaseOrder();
        order.setCustomer(customer);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setStatus("NEW");

        return purchaseOrderRepository.save(order);
    }

    private ProductDetails toProductDetails(Product product) {
        return new ProductDetails(product.getId(),
                product.getName(),
                product.getPrice(),
                product.getQuantity());
    }
}
