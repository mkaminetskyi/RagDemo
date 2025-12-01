package com.michael.tabularDataSearch.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SchemaDescriptions {

    public static final String TABULAR_RAG_SCHEMA = """
Tables:
  products(id INT, name VARCHAR, description TEXT, price DECIMAL, quantity INT, category_id INT, supplier_id INT)
  product_categories(id INT, name VARCHAR, description TEXT)
  suppliers(id INT, name VARCHAR, contact_email VARCHAR, reliability_score INT)
  customers(id INT, name VARCHAR, email VARCHAR, loyalty_tier VARCHAR)
  purchase_orders(id INT, customer_id INT, status VARCHAR, reference VARCHAR)
  purchase_order_lines(id INT, order_id INT, product_id INT, quantity INT, unit_price INT)
Relationships:
  products.category_id -> product_categories.id
  products.supplier_id -> suppliers.id
  purchase_orders.customer_id -> customers.id
  purchase_order_lines.order_id -> purchase_orders.id
  purchase_order_lines.product_id -> products.id
""";
}
