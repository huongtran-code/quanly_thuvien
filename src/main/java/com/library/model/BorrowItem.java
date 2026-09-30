package com.library.model;

import java.time.LocalDateTime;

public class BorrowItem {
    private int id;
    private int borrowId;
    private int documentId;
    private int quantity;
    private boolean returned;
    private LocalDateTime returnDate;

    public BorrowItem() {
    }

    public BorrowItem(int id, int borrowId, int documentId, int quantity, 
                     boolean returned, LocalDateTime returnDate) {
        this.id = id;
        this.borrowId = borrowId;
        this.documentId = documentId;
        this.quantity = quantity;
        this.returned = returned;
        this.returnDate = returnDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(int borrowId) {
        this.borrowId = borrowId;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }
}
