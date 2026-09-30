package com.library.controller;

import com.library.dao.CustomerDAO;
import com.library.dao.FineDAO;
import com.library.model.Customer;
import com.library.model.Fine;
import com.library.util.AppConstants;

import java.util.List;

public class FineController {
    
    private final FineDAO fineDAO;
    private final CustomerDAO customerDAO;
    
    public FineController() {
        this.fineDAO = new FineDAO();
        this.customerDAO = new CustomerDAO();
    }
    
    /**
     * Tạo khoản phạt mới
     */
    public boolean createFine(Fine fine) {
        // Validate
        if (fine.getCustomerId() <= 0) {
            throw new IllegalArgumentException("ID khách hàng không hợp lệ");
        }
        if (fine.getAmount() <= 0) {
            throw new IllegalArgumentException("Số tiền phạt phải lớn hơn 0");
        }
        if (fine.getFineType() == null || fine.getFineType().trim().isEmpty()) {
            throw new IllegalArgumentException("Loại phạt không được để trống");
        }
        
        // Set default status
        if (fine.getStatus() == null) {
            fine.setStatus(AppConstants.FINE_STATUS_UNPAID);
        }
        
        boolean success = fineDAO.createFine(fine);
        
        if (success) {
            // Update customer total fines
            updateCustomerTotalFines(fine.getCustomerId());
        }
        
        return success;
    }
    
    /**
     * Thanh toán phạt
     */
    public boolean payFine(int fineId, Integer paidBy) {
        Fine fine = fineDAO.getFineById(fineId);
        if (fine == null) {
            throw new IllegalArgumentException("Không tìm thấy khoản phạt");
        }
        
        if (fine.isPaid()) {
            throw new IllegalStateException("Khoản phạt đã được thanh toán");
        }
        
        boolean success = fineDAO.payFine(fineId, paidBy);
        
        if (success) {
            // Update customer total fines
            updateCustomerTotalFines(fine.getCustomerId());
        }
        
        return success;
    }
    
    /**
     * Miễn phạt
     */
    public boolean waiveFine(int fineId, Integer waivedBy) {
        Fine fine = fineDAO.getFineById(fineId);
        if (fine == null) {
            throw new IllegalArgumentException("Không tìm thấy khoản phạt");
        }
        
        if (fine.isPaid()) {
            throw new IllegalStateException("Khoản phạt đã được thanh toán, không thể miễn");
        }
        
        boolean success = fineDAO.waiveFine(fineId, waivedBy);
        
        if (success) {
            // Update customer total fines
            updateCustomerTotalFines(fine.getCustomerId());
        }
        
        return success;
    }
    
    /**
     * Lấy khoản phạt theo ID
     */
    public Fine getFineById(int id) {
        return fineDAO.getFineById(id);
    }
    
    /**
     * Lấy tất cả khoản phạt
     */
    public List<Fine> getAllFines() {
        return fineDAO.getAllFines();
    }
    
    /**
     * Lấy khoản phạt theo khách hàng
     */
    public List<Fine> getFinesByCustomer(int customerId) {
        return fineDAO.getFinesByCustomer(customerId);
    }
    
    /**
     * Lấy khoản phạt theo trạng thái
     */
    public List<Fine> getFinesByStatus(String status) {
        return fineDAO.getFinesByStatus(status);
    }
    
    /**
     * Lấy các khoản phạt chưa thanh toán
     */
    public List<Fine> getUnpaidFines() {
        return fineDAO.getFinesByStatus(AppConstants.FINE_STATUS_UNPAID);
    }
    
    /**
     * Lấy các khoản phạt chưa thanh toán của khách hàng
     */
    public List<Fine> getUnpaidFinesByCustomer(int customerId) {
        return fineDAO.getUnpaidFinesByCustomer(customerId);
    }
    
    /**
     * Tính tổng tiền phạt chưa thanh toán của khách hàng
     */
    public double getTotalUnpaidFines(int customerId) {
        return fineDAO.getTotalUnpaidFines(customerId);
    }
    
    /**
     * Lấy khoản phạt theo phiếu mượn
     */
    public List<Fine> getFinesByBorrow(int borrowId) {
        return fineDAO.getFinesByBorrow(borrowId);
    }
    
    /**
     * Thanh toán tất cả phạt của khách hàng
     */
    public boolean payAllFines(int customerId, Integer paidBy) {
        List<Fine> unpaidFines = fineDAO.getUnpaidFinesByCustomer(customerId);
        
        if (unpaidFines.isEmpty()) {
            throw new IllegalStateException("Khách hàng không có khoản phạt nào chưa thanh toán");
        }
        
        boolean allSuccess = true;
        for (Fine fine : unpaidFines) {
            boolean success = fineDAO.payFine(fine.getId(), paidBy);
            if (!success) {
                allSuccess = false;
            }
        }
        
        if (allSuccess) {
            // Update customer total fines
            updateCustomerTotalFines(customerId);
        }
        
        return allSuccess;
    }
    
    /**
     * Cập nhật tổng tiền phạt của khách hàng
     */
    private void updateCustomerTotalFines(int customerId) {
        double totalUnpaid = fineDAO.getTotalUnpaidFines(customerId);
        customerDAO.updateTotalFines(customerId, totalUnpaid);
    }
    
    /**
     * Kiểm tra khách hàng có phạt chưa thanh toán không
     */
    public boolean hasUnpaidFines(int customerId) {
        return getTotalUnpaidFines(customerId) > 0;
    }
}
