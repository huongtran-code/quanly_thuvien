package com.library.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Customer {
    private int id;
    private String customerCode;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String idCard;
    private LocalDate registrationDate;
    private String status; // ACTIVE, SUSPENDED, INACTIVE
    private int totalBorrowed;
    private double totalFines;
    private String notes;
    private LocalDateTime createdAt;

    public Customer() {
    }

    public Customer(int id, String customerCode, String fullName, String email, String phone, 
                   String address, LocalDate dateOfBirth, String idCard, LocalDate registrationDate,
                   String status, int totalBorrowed, double totalFines, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.customerCode = customerCode;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.idCard = idCard;
        this.registrationDate = registrationDate;
        this.status = status;
        this.totalBorrowed = totalBorrowed;
        this.totalFines = totalFines;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean canBorrow() {
        return isActive() && totalFines == 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalBorrowed() {
        return totalBorrowed;
    }

    public void setTotalBorrowed(int totalBorrowed) {
        this.totalBorrowed = totalBorrowed;
    }

    public double getTotalFines() {
        return totalFines;
    }

    public void setTotalFines(double totalFines) {
        this.totalFines = totalFines;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
