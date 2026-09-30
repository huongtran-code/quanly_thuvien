package com.library.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Borrow {
    private int id;
    private String borrowCode;
    private int customerId;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status; // BORROWED, RETURNED, OVERDUE
    private int totalBooks;
    private double lateFee;
    private String notes;
    private Integer createdBy;
    private LocalDateTime createdAt;

    public Borrow() {
    }

    public Borrow(int id, String borrowCode, int customerId, LocalDateTime borrowDate,
                 LocalDateTime dueDate, LocalDateTime returnDate, String status,
                 int totalBooks, double lateFee, String notes, Integer createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.borrowCode = borrowCode;
        this.customerId = customerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.totalBooks = totalBooks;
        this.lateFee = lateFee;
        this.notes = notes;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public boolean isOverdue() {
        return returnDate == null && LocalDateTime.now().isAfter(dueDate);
    }

    public boolean isReturned() {
        return "RETURNED".equals(status);
    }

    public long calculateLateDays() {
        if (returnDate == null) {
            if (LocalDateTime.now().isAfter(dueDate)) {
                return ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
            }
            return 0;
        } else {
            if (returnDate.isAfter(dueDate)) {
                return ChronoUnit.DAYS.between(dueDate, returnDate);
            }
            return 0;
        }
    }

    public double calculateLateFee(double feePerDay) {
        return calculateLateDays() * feePerDay;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBorrowCode() {
        return borrowCode;
    }

    public void setBorrowCode(String borrowCode) {
        this.borrowCode = borrowCode;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(int totalBooks) {
        this.totalBooks = totalBooks;
    }

    public double getLateFee() {
        return lateFee;
    }

    public void setLateFee(double lateFee) {
        this.lateFee = lateFee;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
