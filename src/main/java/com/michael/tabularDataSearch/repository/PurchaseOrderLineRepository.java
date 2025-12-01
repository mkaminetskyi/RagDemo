package com.michael.tabularDataSearch.repository;

import com.michael.tabularDataSearch.entity.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Integer> {
}
