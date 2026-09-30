package com.library.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerMembership {
    private int id;
    private int customerId;
    private int tierId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // ACTIVE, EXPIRED, SUSPENDED
    private LocalDateTime createdAt;

    public CustomerMembership() {
    }

    public CustomerMembership(int id, int customerId, int tierId, LocalDate startDate,
                            LocalDate endDate, String status, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.tierId = tierId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status) && LocalDate.now().isBefore(endDate.plusDays(1));
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
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

    public int getTierId() {
        return tierId;
    }

    public void setTierId(int tierId) {
        this.tierId = tierId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
