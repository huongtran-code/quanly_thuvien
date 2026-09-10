package com.library.model;

import java.time.LocalDateTime;

/**
 * Entity: Tài liệu thư viện
 */
public class Document {

    private int id;
    private String title;
    private String author;
    private String isbn;
    private int categoryId;
    private String categoryName; // Transient: tên danh mục khi JOIN
    private String publisher;
    private int publishYear;
    private double unitPrice;
    private int stockQuantity;
    private LocalDateTime createdAt;

    public Document() {}

    public Document(String title, String author, String isbn, int categoryId,
                    String publisher, int publishYear, double unitPrice, int stockQuantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.categoryId = categoryId;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
    }

    // ── Getters & Setters ──

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public int getPublishYear() { return publishYear; }
    public void setPublishYear(int publishYear) { this.publishYear = publishYear; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return title + " - " + author;
    }
}
