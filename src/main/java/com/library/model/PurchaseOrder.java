package com.library.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Đơn mua hàng
 */
public class PurchaseOrder {

    private int id;
    private String orderCode;
    private int supplierId;
    private String supplierName; // Transient: tên NCC khi JOIN
    private int budgetId;
    private String budgetName;   // Transient: tên ngân sách khi JOIN
    private LocalDateTime orderDate;
    private double totalAmount;
    private String status; // DRAFT, APPROVED, RECEIVED, CANCELLED
    private String notes;
    private int createdBy;
    private String createdByName; // Transient
    private LocalDateTime createdAt;
    private List<PurchaseOrderItem> items;
    private String invoiceNumber;   // Số hóa đơn NCC
    private LocalDateTime invoiceDate; // Ngày hóa đơn

    public PurchaseOrder() {
        this.status = "DRAFT";
        this.items = new ArrayList<>();
    }

    // ── Getters & Setters ──

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public int getBudgetId() { return budgetId; }
    public void setBudgetId(int budgetId) { this.budgetId = budgetId; }

    public String getBudgetName() { return budgetName; }
    public void setBudgetName(String budgetName) { this.budgetName = budgetName; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDateTime getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDateTime invoiceDate) { this.invoiceDate = invoiceDate; }

    public List<PurchaseOrderItem> getItems() { return items; }
    public void setItems(List<PurchaseOrderItem> items) { this.items = items; }

    /**
     * Tính tổng tiền từ danh sách items
     */
    public void calculateTotal() {
        this.totalAmount = items.stream()
                .mapToDouble(PurchaseOrderItem::getSubtotal)
                .sum();
    }

    @Override
    public String toString() {
        return orderCode + " - " + status;
    }
}
