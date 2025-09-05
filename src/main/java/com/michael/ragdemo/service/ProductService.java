package com.michael.ragdemo.service;

import com.michael.ragdemo.entity.Product;
import com.michael.ragdemo.repository.ProductRepository;
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
}