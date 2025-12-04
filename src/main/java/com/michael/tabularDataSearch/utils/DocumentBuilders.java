package com.michael.tabularDataSearch.utils;

import com.michael.tabularDataSearch.entity.*;
import lombok.experimental.UtilityClass;
import org.springframework.ai.document.Document;

import java.util.Map;

@UtilityClass
public final class DocumentBuilders {

    public static Document createProductDocument(Product product) {
        String content = """
                Назва: %s
                Категорія: %s
                Постачальник: %s
                Ціна: %d грн
                Залишок: %d шт.
                Опис: %s
                """.formatted(
                product.getName(),
                product.getCategory().getName(),
                product.getSupplier().getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getDescription());

        return new Document(content.trim(), Map.of(
                "productId", product.getId(),
                "productName", product.getName(),
                "category", product.getCategory().getName(),
                "supplier", product.getSupplier().getName()));
    }

    public static Document createCategoryDocument(ProductCategory category) {
        String content = """
                Категорія: %s
                Опис: %s
                """.formatted(
                category.getName(),
                category.getDescription());

        return new Document(content.trim(), Map.of(
                "categoryId", category.getId(),
                "type", "category",
                "categoryName", category.getName()));
    }

    public static Document createSupplierDocument(Supplier supplier) {
        String content = """
                Постачальник: %s
                Контактний email: %s
                Надійність: %d
                """.formatted(
                supplier.getName(),
                supplier.getContactEmail(),
                supplier.getReliabilityScore());

        return new Document(content.trim(), Map.of(
                "supplierId", supplier.getId(),
                "type", "supplier",
                "supplierName", supplier.getName()));
    }

    public static Document createCustomerDocument(Customer customer) {
        String content = """
                Покупець: %s
                Email: %s
                Рівень лояльності: %s
                """.formatted(
                customer.getName(),
                customer.getEmail(),
                customer.getLoyaltyTier());

        return new Document(content.trim(), Map.of(
                "customerId", customer.getId(),
                "type", "customer",
                "customerName", customer.getName()));
    }

    public static Document createPurchaseOrderDocument(PurchaseOrder order) {
        String customerName = order.getCustomer() != null ? order.getCustomer().getName() : "Невідомий клієнт";
        String productName = order.getProduct() != null ? order.getProduct().getName() : "Невідомий товар";

        String content = """
                Замовлення #%d
                Товар: %s
                Покупець: %s
                Кількість: %d
                Статус: %s
                """.formatted(
                order.getId(),
                productName,
                customerName,
                order.getQuantity(),
                order.getStatus());

        return new Document(content.trim(), Map.of(
                "purchaseOrderId", order.getId(),
                "type", "purchase_order",
                "productName", productName,
                "customerName", customerName));
    }
}
