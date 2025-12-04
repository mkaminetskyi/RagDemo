package com.michael.tabularDataSearch.repository.projection;

import com.michael.tabularDataSearch.entity.Product;

public interface ProductPopularity {
    Product getProduct();

    Long getTotalQuantity();
}
