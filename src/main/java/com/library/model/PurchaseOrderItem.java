package com.library.model;

/**
 * Entity: Chi tiết dòng trong đơn mua
 */
public class PurchaseOrderItem {

    private int id;
    private int orderId;
    private int documentId;
    private String documentTitle; // Transient: tên tài liệu khi JOIN
    private int quantity;
    private double unitPrice;
    private double subtotal;

    public PurchaseOrderItem() {}

    public PurchaseOrderItem(int documentId, int quantity, double unitPrice) {
        this.documentId = documentId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    // ── Getters & Setters ──

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }

    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = quantity * unitPrice;
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    @Override
    public String toString() {
        return documentTitle + " x" + quantity;
    }
}
