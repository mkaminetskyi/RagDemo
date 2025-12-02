package com.michael.tabularDataSearch.utils;

import com.michael.tabularDataSearch.entity.Customer;
import com.michael.tabularDataSearch.entity.Product;
import com.michael.tabularDataSearch.entity.ProductCategory;
import com.michael.tabularDataSearch.entity.PurchaseOrder;
import com.michael.tabularDataSearch.entity.PurchaseOrderLine;
import com.michael.tabularDataSearch.entity.Supplier;

import java.util.List;

public final class DatabaseSnapshotFormatter {

    private DatabaseSnapshotFormatter() {
    }

    public static void appendCategories(StringBuilder builder, List<ProductCategory> categories) {
        if (categories.isEmpty()) {
            return;
        }

        builder.append("Product Categories:\n");
        categories.forEach(category -> builder
                .append("Category #")
                .append(category.getId())
                .append(": ")
                .append(category.getName())
                .append(" - ")
                .append(category.getDescription())
                .append("\n"));
        builder.append("\n");
    }

    public static void appendSuppliers(StringBuilder builder, List<Supplier> suppliers) {
        if (suppliers.isEmpty()) {
            return;
        }

        builder.append("Suppliers:\n");
        suppliers.forEach(supplier -> builder
                .append("Supplier #")
                .append(supplier.getId())
                .append(": ")
                .append(supplier.getName())
                .append(", email: ")
                .append(supplier.getContactEmail() != null ? supplier.getContactEmail() : "N/A")
                .append(", reliability: ")
                .append(supplier.getReliabilityScore())
                .append("\n"));
        builder.append("\n");
    }

    public static void appendProducts(StringBuilder builder, List<Product> products) {
        if (products.isEmpty()) {
            return;
        }

        builder.append("Products:\n");
        products.forEach(product -> builder
                .append("Product #")
                .append(product.getId())
                .append(": ")
                .append(product.getName())
                .append(" - ")
                .append(product.getDescription())
                .append(", price: ")
                .append(product.getPrice())
                .append(", quantity: ")
                .append(product.getQuantity())
                .append(", category: ")
                .append(product.getCategory() != null ? product.getCategory().getName() : "N/A")
                .append(", supplier: ")
                .append(product.getSupplier() != null ? product.getSupplier().getName() : "N/A")
                .append("\n"));
        builder.append("\n");
    }

    public static void appendCustomers(StringBuilder builder, List<Customer> customers) {
        if (customers.isEmpty()) {
            return;
        }

        builder.append("Customers:\n");
        customers.forEach(customer -> builder
                .append("Customer #")
                .append(customer.getId())
                .append(": ")
                .append(customer.getName())
                .append(", email: ")
                .append(customer.getEmail())
                .append(", loyalty tier: ")
                .append(customer.getLoyaltyTier())
                .append("\n"));
        builder.append("\n");
    }

    public static void appendPurchaseOrders(StringBuilder builder, List<PurchaseOrder> purchaseOrders) {
        if (purchaseOrders.isEmpty()) {
            return;
        }

        builder.append("Purchase Orders:\n");
        purchaseOrders.forEach(order -> builder
                .append("Order #")
                .append(order.getId())
                .append(", status: ")
                .append(order.getStatus())
                .append(", customer: ")
                .append(order.getCustomer() != null ? order.getCustomer().getName() : "N/A")
                .append("\n"));
        builder.append("\n");
    }

    public static void appendPurchaseOrderLines(StringBuilder builder, List<PurchaseOrderLine> purchaseOrderLines) {
        if (purchaseOrderLines.isEmpty()) {
            return;
        }

        builder.append("Purchase Order Lines:\n");
        purchaseOrderLines.forEach(line -> builder
                .append("Line #")
                .append(line.getId())
                .append(" for order #")
                .append(line.getOrder() != null ? line.getOrder().getId() : "N/A")
                .append(", product: ")
                .append(line.getProduct() != null ? line.getProduct().getName() : "N/A")
                .append(", quantity: ")
                .append(line.getQuantity())
                .append("\n"));
        builder.append("\n");
    }
}
