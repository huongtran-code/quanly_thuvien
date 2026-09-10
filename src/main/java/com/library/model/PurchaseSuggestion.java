package com.library.model;

import java.time.LocalDateTime;

/**
 * Entity: Đề xuất mua sách từ GV/SV
 */
public class PurchaseSuggestion {

    private int id;
    private String bookTitle;
    private String author;
    private String isbn;
    private String publisher;
    private int quantity;
    private String reason;        // Lý do đề xuất
    private String suggestedBy;   // Tên người đề xuất
    private String status;        // PENDING, APPROVED, REJECTED
    private String reviewNote;    // Ghi chú khi duyệt
    private Integer reviewedBy;   // user_id admin duyệt
    private String reviewedByName; // Transient
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public PurchaseSuggestion() {
        this.status = "PENDING";
        this.quantity = 1;
    }

    // ── Getters & Setters ──

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getSuggestedBy() { return suggestedBy; }
    public void setSuggestedBy(String suggestedBy) { this.suggestedBy = suggestedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
