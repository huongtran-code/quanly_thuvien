package com.library.model;

import java.time.LocalDateTime;

public class Fine {
    private int id;
    private int customerId;
    private Integer borrowId; // nullable
    private String fineType; // LATE_RETURN, DAMAGED, LOST
    private double amount;
    private String reason;
    private String status; // UNPAID, PAID, WAIVED
    private LocalDateTime paidDate;
    private Integer paidBy;
    private LocalDateTime createdAt;

    public Fine() {
    }

    public Fine(int id, int customerId, Integer borrowId, String fineType, double amount,
               String reason, String status, LocalDateTime paidDate, Integer paidBy, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.borrowId = borrowId;
        this.fineType = fineType;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.paidDate = paidDate;
        this.paidBy = paidBy;
        this.createdAt = createdAt;
    }

    public boolean isPaid() {
        return "PAID".equals(status);
    }

    public boolean isUnpaid() {
        return "UNPAID".equals(status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Integer getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Integer borrowId) {
        this.borrowId = borrowId;
    }

    public String getFineType() {
        return fineType;
    }

    public void setFineType(String fineType) {
        this.fineType = fineType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
    }

    public Integer getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(Integer paidBy) {
        this.paidBy = paidBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
