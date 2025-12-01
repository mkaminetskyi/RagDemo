package com.michael.tabularDataSearch.repository;

import com.michael.tabularDataSearch.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Integer> {
    Product findByNameIgnoreCase(String name);
}
