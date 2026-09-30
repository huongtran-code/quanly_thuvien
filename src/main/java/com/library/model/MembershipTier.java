package com.library.model;

import java.time.LocalDateTime;

public class MembershipTier {
    private int id;
    private String tierName; // BRONZE, SILVER, GOLD, PLATINUM
    private int maxBooks;
    private int borrowDays;
    private double lateFeePerDay;
    private double price;
    private double discountPercent;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;

    public MembershipTier() {
    }

    public MembershipTier(int id, String tierName, int maxBooks, int borrowDays, 
                         double lateFeePerDay, double price, double discountPercent,
                         String description, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.tierName = tierName;
        this.maxBooks = maxBooks;
        this.borrowDays = borrowDays;
        this.lateFeePerDay = lateFeePerDay;
        this.price = price;
        this.discountPercent = discountPercent;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public int getMaxBooks() {
        return maxBooks;
    }

    public void setMaxBooks(int maxBooks) {
        this.maxBooks = maxBooks;
    }

    public int getBorrowDays() {
        return borrowDays;
    }

    public void setBorrowDays(int borrowDays) {
        this.borrowDays = borrowDays;
    }

    public double getLateFeePerDay() {
        return lateFeePerDay;
    }

    public void setLateFeePerDay(double lateFeePerDay) {
        this.lateFeePerDay = lateFeePerDay;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
