package com.michael.tabularDataSearch.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SchemaDescriptions {

    public static final String TABULAR_RAG_SCHEMA = """
Tables:
  product_categories(id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, description TEXT) -- examples: 'Електроніка', 'Дім і кухня', 'Офіс і навчання'
  suppliers(id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, contact_email VARCHAR(255), reliability_score INTEGER NOT NULL) -- examples: 'СвітТех Поставка' (92), 'Балтія Логістик' (88)
  products(id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, description TEXT, price INTEGER NOT NULL, quantity INTEGER NOT NULL, category_id INTEGER REFERENCES product_categories(id), supplier_id INTEGER REFERENCES suppliers(id)) -- examples: 'Бездротова миша' (1299 UAH, qty 140), 'Настільна лампа з Qi зарядкою' (1890 UAH, qty 75)
  customers(id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, email VARCHAR(255), loyalty_tier VARCHAR(50)) -- examples: 'Олена Коваль' (Gold), 'Ігор Мельник' (Silver)
  purchase_orders(id SERIAL PRIMARY KEY, customer_id INTEGER REFERENCES customers(id), product_id INTEGER REFERENCES products(id), quantity INTEGER NOT NULL, status VARCHAR(50)) -- sales records per product with purchased quantities
Relationships:
  products.category_id -> product_categories.id
  products.supplier_id -> suppliers.id
  purchase_orders.customer_id -> customers.id
  purchase_orders.product_id -> products.id
""";
}
