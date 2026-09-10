package com.library.model;

import java.time.LocalDateTime;

/**
 * Entity: Giao dịch kho (nhập/xuất)
 */
public class StockTransaction {

    public static final String TYPE_IMPORT = "IMPORT";
    public static final String TYPE_EXPORT = "EXPORT";

    private int id;
    private int documentId;
    private String documentTitle; // Transient: khi JOIN
    private int changeQty;
    private String type;
    private String source;
    private Integer createdBy;
    private String createdByName; // Transient: khi JOIN
    private LocalDateTime createdAt;

    public StockTransaction() {}

    public StockTransaction(int documentId, int changeQty, String type, String source, Integer createdBy) {
        this.documentId = documentId;
        this.changeQty = changeQty;
        this.type = type;
        this.source = source;
        this.createdBy = createdBy;
    }

    // ── Getters & Setters ──

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }

    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }

    public int getChangeQty() { return changeQty; }
    public void setChangeQty(int changeQty) { this.changeQty = changeQty; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
