package com.library.controller;

import com.library.dao.BorrowDAO;
import com.library.dao.CustomerDAO;
import com.library.dao.CustomerMembershipDAO;
import com.library.dao.DocumentDAO;
import com.library.dao.FineDAO;
import com.library.dao.MembershipTierDAO;
import com.library.model.Borrow;
import com.library.model.BorrowItem;
import com.library.model.Customer;
import com.library.model.CustomerMembership;
import com.library.model.Document;
import com.library.model.Fine;
import com.library.model.MembershipTier;
import com.library.util.AppConstants;

import java.time.LocalDateTime;
import java.util.List;

public class BorrowController {
    
    private final BorrowDAO borrowDAO;
    private final CustomerDAO customerDAO;
    private final DocumentDAO documentDAO;
    private final FineDAO fineDAO;
    private final MembershipTierDAO tierDAO;
    private final CustomerMembershipDAO membershipDAO;
    
    public BorrowController() {
        this.borrowDAO = new BorrowDAO();
        this.customerDAO = new CustomerDAO();
        this.documentDAO = new DocumentDAO();
        this.fineDAO = new FineDAO();
        this.tierDAO = new MembershipTierDAO();
        this.membershipDAO = new CustomerMembershipDAO();
    }
    
    /**
     * Tạo phiếu mượn mới
     */
    public boolean createBorrow(Borrow borrow, List<BorrowItem> items, Integer createdBy) {
        // Validate customer
        Customer customer = customerDAO.getCustomerById(borrow.getCustomerId());
        if (customer == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng");
        }
        if (!customer.isActive()) {
            throw new IllegalStateException("Khách hàng đang bị tạm ngưng hoặc không hoạt động");
        }
        
        // Check unpaid fines
        double unpaidFines = fineDAO.getTotalUnpaidFines(borrow.getCustomerId());
        if (unpaidFines > 0) {
            throw new IllegalStateException("Khách hàng có tiền phạt chưa thanh toán: " + unpaidFines + " VNĐ");
        }
        
        // Get active membership
        CustomerMembership membership = membershipDAO.getActiveMembershipByCustomer(borrow.getCustomerId());
        if (membership == null || !membership.isActive()) {
            throw new IllegalStateException("Khách hàng không có gói thành viên hoặc đã hết hạn");
        }
        
        // Get tier info
        MembershipTier tier = tierDAO.getTierById(membership.getTierId());
        if (tier == null) {
            throw new IllegalStateException("Không tìm thấy thông tin gói thành viên");
        }
        
        // Validate items
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sách mượn không được rỗng");
        }
        
        // Check max books limit
        int totalBooks = items.stream().mapToInt(BorrowItem::getQuantity).sum();
        if (totalBooks > tier.getMaxBooks()) {
            throw new IllegalStateException("Vượt quá số lượng sách được phép mượn (" + tier.getMaxBooks() + " quyển)");
        }
        
        // Validate stock
        for (BorrowItem item : items) {
            Document doc = documentDAO.findById(item.getDocumentId());
            if (doc == null) {
                throw new IllegalArgumentException("Không tìm thấy tài liệu ID: " + item.getDocumentId());
            }
            if (doc.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Không đủ tồn kho cho tài liệu: " + doc.getTitle());
            }
        }
        
        // Set borrow info
        borrow.setBorrowCode(borrowDAO.generateBorrowCode());
        borrow.setBorrowDate(LocalDateTime.now());
        borrow.setDueDate(LocalDateTime.now().plusDays(tier.getBorrowDays()));
        borrow.setStatus(AppConstants.BORROW_STATUS_BORROWED);
        borrow.setTotalBooks(totalBooks);
        borrow.setLateFee(0.0);
        borrow.setCreatedBy(createdBy);
        
        // Create borrow
        boolean success = borrowDAO.createBorrow(borrow, items);
        
        if (success) {
            // Update customer total borrowed
            customerDAO.updateTotalBorrowed(borrow.getCustomerId(), customer.getTotalBorrowed() + 1);
        }
        
        return success;
    }
    
    /**
     * Trả sách
     */
    public boolean returnBorrow(int borrowId, Integer userId) {
        Borrow borrow = borrowDAO.getBorrowById(borrowId);
        if (borrow == null) {
            throw new IllegalArgumentException("Không tìm thấy phiếu mượn");
        }
        
        if (borrow.isReturned()) {
            throw new IllegalStateException("Phiếu mượn đã được trả");
        }
        
        // Get membership to calculate late fee
        CustomerMembership membership = membershipDAO.getActiveMembershipByCustomer(borrow.getCustomerId());
        MembershipTier tier = null;
        if (membership != null) {
            tier = tierDAO.getTierById(membership.getTierId());
        }
        
        // Calculate late fee
        double lateFee = 0.0;
        if (borrow.isOverdue()) {
            long lateDays = borrow.calculateLateDays();
            double feePerDay = tier != null ? tier.getLateFeePerDay() : 5000; // Default 5,000 VNĐ/day
            lateFee = lateDays * feePerDay;
            
            // Create fine record
            if (lateFee > 0) {
                Fine fine = new Fine();
                fine.setCustomerId(borrow.getCustomerId());
                fine.setBorrowId(borrowId);
                fine.setFineType(AppConstants.FINE_TYPE_LATE_RETURN);
                fine.setAmount(lateFee);
                fine.setReason("Trả sách trễ " + lateDays + " ngày");
                fine.setStatus(AppConstants.FINE_STATUS_UNPAID);
                fineDAO.createFine(fine);
                
                // Update customer total fines
                customerDAO.updateTotalFines(borrow.getCustomerId(), 
                    customerDAO.getCustomerById(borrow.getCustomerId()).getTotalFines() + lateFee);
            }
        }
        
        return borrowDAO.returnBorrow(borrowId, lateFee, userId);
    }
    
    /**
     * Lấy phiếu mượn theo ID
     */
    public Borrow getBorrowById(int id) {
        return borrowDAO.getBorrowById(id);
    }
    
    /**
     * Lấy phiếu mượn theo mã
     */
    public Borrow getBorrowByCode(String code) {
        return borrowDAO.getBorrowByCode(code);
    }
    
    /**
     * Lấy tất cả phiếu mượn
     */
    public List<Borrow> getAllBorrows() {
        return borrowDAO.getAllBorrows();
    }
    
    /**
     * Lấy phiếu mượn theo khách hàng
     */
    public List<Borrow> getBorrowsByCustomer(int customerId) {
        return borrowDAO.getBorrowsByCustomer(customerId);
    }
    
    /**
     * Lấy phiếu mượn theo trạng thái
     */
    public List<Borrow> getBorrowsByStatus(String status) {
        return borrowDAO.getBorrowsByStatus(status);
    }
    
    /**
     * Lấy phiếu mượn quá hạn
     */
    public List<Borrow> getOverdueBorrows() {
        return borrowDAO.getOverdueBorrows();
    }
    
    /**
     * Lấy chi tiết items
     */
    public List<BorrowItem> getBorrowItems(int borrowId) {
        return borrowDAO.getBorrowItems(borrowId);
    }
    
    /**
     * Tìm kiếm phiếu mượn
     */
    public List<Borrow> searchBorrows(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBorrows();
        }
        return borrowDAO.searchBorrows(keyword);
    }
    
    /**
     * Cập nhật trạng thái quá hạn
     */
    public int updateOverdueStatus() {
        return borrowDAO.updateOverdueStatus();
    }
}
