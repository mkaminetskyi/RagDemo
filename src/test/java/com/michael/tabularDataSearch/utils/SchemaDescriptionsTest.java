package com.michael.tabularDataSearch.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaDescriptionsTest {

    @Test
    void schemaListsAllDemoTablesAndRelationships() {
        String schema = SchemaDescriptions.TABULAR_RAG_SCHEMA;

        assertThat(schema)
                .contains("products")
                .contains("product_categories")
                .contains("suppliers")
                .contains("customers")
                .contains("purchase_orders")
                .contains("purchase_order_lines")
                .contains("products.category_id -> product_categories.id")
                .contains("purchase_order_lines.product_id -> products.id");
    }
}
