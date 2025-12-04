package com.michael.tabularDataSearch.repository;

import com.michael.tabularDataSearch.entity.PurchaseOrder;
import com.michael.tabularDataSearch.entity.Product;
import com.michael.tabularDataSearch.repository.projection.ProductPopularity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {
    @Query("SELECT DISTINCT po.product FROM PurchaseOrder po WHERE po.customer.id = :customerId")
    List<Product> findDistinctProductsByCustomerId(@Param("customerId") int customerId);

    @Query("""
            SELECT po.product AS product, SUM(po.quantity) AS totalQuantity
            FROM PurchaseOrder po
            GROUP BY po.product
            ORDER BY totalQuantity DESC
            """)
    List<ProductPopularity> findProductPopularity();
}
