package com.michael.tabularDataSearch.repository;

import com.michael.tabularDataSearch.dto.InventorySummary;
import com.michael.tabularDataSearch.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Integer> {
    Product findByNameIgnoreCase(String name);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :fragment, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("fragment") String fragment);

    List<Product> findByQuantityLessThanEqualOrderByQuantityAsc(int quantity);

    @Query("SELECT p FROM Product p WHERE LOWER(p.supplier.name) = LOWER(:supplierName)")
    List<Product> findBySupplierName(@Param("supplierName") String supplierName);

    @Query("""
            SELECT new com.michael.tabularDataSearch.dto.InventorySummary(
                c.name,
                COUNT(p.id),
                SUM(p.quantity),
                AVG(p.price)
            )
            FROM Product p
            JOIN p.category c
            GROUP BY c.name
            ORDER BY c.name
            """)
    List<InventorySummary> summarizeInventoryByCategory();
}
