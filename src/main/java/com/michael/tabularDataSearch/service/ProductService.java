package com.michael.tabularDataSearch.service;

import com.michael.tabularDataSearch.dto.InventorySummary;
import com.michael.tabularDataSearch.entity.Product;
import com.michael.tabularDataSearch.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;

    public Product findProductByName(String name) {
        return productRepository.findByNameIgnoreCase(name);
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public Product findProductById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> findProductsByNameFragment(String nameFragment) {
        return productRepository.findByNameContainingIgnoreCase(nameFragment);
    }

    public List<Product> findLowStockProducts(int threshold) {
        return productRepository.findByQuantityLessThanEqualOrderByQuantityAsc(threshold);
    }

    public List<Product> findProductsBySupplier(String supplierName) {
        return productRepository.findBySupplierName(supplierName);
    }

    public List<InventorySummary> summarizeInventoryByCategory() {
        return productRepository.summarizeInventoryByCategory();
    }
}