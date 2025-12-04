package com.michael.tabularDataSearch.service;

import com.michael.tabularDataSearch.dto.PopularProduct;
import com.michael.tabularDataSearch.dto.ProductDetails;
import com.michael.tabularDataSearch.entity.Customer;
import com.michael.tabularDataSearch.entity.Product;
import com.michael.tabularDataSearch.entity.PurchaseOrder;
import com.michael.tabularDataSearch.repository.CustomerRepository;
import com.michael.tabularDataSearch.repository.PurchaseOrderRepository;
import com.michael.tabularDataSearch.repository.projection.ProductPopularity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTools {
    private final ProductService productService;
    private final CustomerRepository customerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Tool(description = "Update the price of a product by its ID")
    public ProductDetails updateProductPrice(int productId, int newPrice) {
        log.info("Updating price for product {} to {}", productId, newPrice);

        Product updatedProduct = productService.updateProductPrice(productId, newPrice);

        return toProductDetails(updatedProduct);
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

    @Tool(description = "List all products that exist in the catalog")
    public List<ProductDetails> listProducts() {
        log.info("Fetching all products");

        return productService.findAllProducts().stream()
                .filter(Objects::nonNull)
                .map(this::toProductDetails)
                .toList();
    }

    @Tool(description = "Obtain the products previously purchased by a customer")
    public List<ProductDetails> getCustomerProducts(int customerId) {
        log.info("Fetching products for customer {}", customerId);

        customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        return purchaseOrderRepository.findDistinctProductsByCustomerId(customerId).stream()
                .map(this::toProductDetails)
                .toList();
    }

    @Tool(description = "Calculate the most popular products for recommendation")
    public List<PopularProduct> recommendPopularProducts(int limit) {
        log.info("Calculating most popular products with limit {}", limit);

        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }

        List<PopularProduct> popularProducts = purchaseOrderRepository.findProductPopularity().stream()
                .map(this::toPopularProduct)
                .toList();

        return popularProducts.stream()
                .limit(limit)
                .toList();
    }

    private ProductDetails toProductDetails(Product product) {
        return new ProductDetails(product.getId(),
                product.getName(),
                product.getPrice(),
                product.getQuantity());
    }

    private PopularProduct toPopularProduct(ProductPopularity productPopularity) {
        Product product = productPopularity.getProduct();
        return new PopularProduct(product.getId(),
                product.getName(),
                productPopularity.getTotalQuantity());
    }
}
