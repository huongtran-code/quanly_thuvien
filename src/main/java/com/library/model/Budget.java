package com.library.model;

import java.time.LocalDateTime;

/**
 * Entity: Ngân sách thư viện
 */
public class Budget {

    private int id;
    private int fiscalYear;
    private String budgetName;
    private double totalAmount;
    private double spentAmount;
    private double remainingAmount;
    private String status; // ACTIVE, CLOSED
    private LocalDateTime createdAt;

    public Budget() {
        this.status = "ACTIVE";
    }

    public Budget(int fiscalYear, String budgetName, double totalAmount) {
        this.fiscalYear = fiscalYear;
        this.budgetName = budgetName;
        this.totalAmount = totalAmount;
        this.spentAmount = 0;
        this.remainingAmount = totalAmount;
        this.status = "ACTIVE";
    }

    // ── Getters & Setters ──

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }

    public String getBudgetName() { return budgetName; }
    public void setBudgetName(String budgetName) { this.budgetName = budgetName; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }

    public double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(double remainingAmount) { this.remainingAmount = remainingAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Tỷ lệ % đã chi tiêu
     */
    public double getSpentPercentage() {
        if (totalAmount <= 0) return 0;
        return (spentAmount / totalAmount) * 100;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    @Override
    public String toString() {
        return budgetName + " (" + fiscalYear + ")";
    }
}
