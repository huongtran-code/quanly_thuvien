package com.library.controller;

import com.library.dao.CustomerDAO;
import com.library.dao.CustomerMembershipDAO;
import com.library.dao.FineDAO;
import com.library.model.Customer;
import com.library.model.CustomerMembership;
import com.library.util.AppConstants;

import java.time.LocalDate;
import java.util.List;

public class CustomerController {
    
    private final CustomerDAO customerDAO;
    private final CustomerMembershipDAO membershipDAO;
    private final FineDAO fineDAO;
    
    public CustomerController() {
        this.customerDAO = new CustomerDAO();
        this.membershipDAO = new CustomerMembershipDAO();
        this.fineDAO = new FineDAO();
    }
    
    /**
     * Tạo khách hàng mới
     */
    public boolean createCustomer(Customer customer) {
        // Validate
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được để trống");
        }
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }
        
        // Check email exists
        if (customerDAO.isEmailExists(customer.getEmail(), 0)) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống");
        }
        
        // Generate customer code
        customer.setCustomerCode(customerDAO.generateCustomerCode());
        
        // Set default values
        if (customer.getRegistrationDate() == null) {
            customer.setRegistrationDate(LocalDate.now());
        }
        if (customer.getStatus() == null) {
            customer.setStatus(AppConstants.CUSTOMER_STATUS_ACTIVE);
        }
        if (customer.getTotalBorrowed() == 0) {
            customer.setTotalBorrowed(0);
        }
        if (customer.getTotalFines() == 0.0) {
            customer.setTotalFines(0.0);
        }
        
        return customerDAO.createCustomer(customer);
    }
    
    /**
     * Cập nhật khách hàng
     */
    public boolean updateCustomer(Customer customer) {
        // Validate
        if (customer.getId() <= 0) {
            throw new IllegalArgumentException("ID khách hàng không hợp lệ");
        }
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được để trống");
        }
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        
        // Check email exists (exclude current customer)
        if (customerDAO.isEmailExists(customer.getEmail(), customer.getId())) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống");
        }
        
        return customerDAO.updateCustomer(customer);
    }
    
    /**
     * Xóa khách hàng
     */
    public boolean deleteCustomer(int customerId) {
        // Check if customer has active borrows
        CustomerMembership membership = membershipDAO.getActiveMembershipByCustomer(customerId);
        if (membership != null && membership.isActive()) {
            throw new IllegalStateException("Không thể xóa khách hàng đang có gói thành viên hoạt động");
        }
        
        return customerDAO.deleteCustomer(customerId);
    }
    
    /**
     * Lấy khách hàng theo ID
     */
    public Customer getCustomerById(int id) {
        return customerDAO.getCustomerById(id);
    }
    
    /**
     * Lấy khách hàng theo mã
     */
    public Customer getCustomerByCode(String code) {
        return customerDAO.getCustomerByCode(code);
    }
    
    /**
     * Lấy tất cả khách hàng
     */
    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }
    
    /**
     * Tìm kiếm khách hàng
     */
    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerDAO.searchCustomers(keyword);
    }
    
    /**
     * Lấy khách hàng theo trạng thái
     */
    public List<Customer> getCustomersByStatus(String status) {
        return customerDAO.getCustomersByStatus(status);
    }
    
    /**
     * Tạm ngưng khách hàng
     */
    public boolean suspendCustomer(int customerId) {
        Customer customer = customerDAO.getCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng");
        }
        
        customer.setStatus(AppConstants.CUSTOMER_STATUS_SUSPENDED);
        return customerDAO.updateCustomer(customer);
    }
    
    /**
     * Kích hoạt lại khách hàng
     */
    public boolean activateCustomer(int customerId) {
        Customer customer = customerDAO.getCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng");
        }
        
        customer.setStatus(AppConstants.CUSTOMER_STATUS_ACTIVE);
        return customerDAO.updateCustomer(customer);
    }
    
    /**
     * Kiểm tra khách hàng có thể mượn sách không
     */
    public boolean canCustomerBorrow(int customerId) {
        Customer customer = customerDAO.getCustomerById(customerId);
        if (customer == null) {
            return false;
        }
        
        // Check status
        if (!customer.isActive()) {
            return false;
        }
        
        // Check unpaid fines
        double unpaidFines = fineDAO.getTotalUnpaidFines(customerId);
        if (unpaidFines > 0) {
            return false;
        }
        
        // Check membership
        CustomerMembership membership = membershipDAO.getActiveMembershipByCustomer(customerId);
        if (membership == null || !membership.isActive()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Cập nhật tổng số lần mượn
     */
    public boolean updateTotalBorrowed(int customerId) {
        Customer customer = customerDAO.getCustomerById(customerId);
        if (customer == null) {
            return false;
        }
        
        int totalBorrowed = customer.getTotalBorrowed() + 1;
        return customerDAO.updateTotalBorrowed(customerId, totalBorrowed);
    }
    
    /**
     * Cập nhật tổng tiền phạt
     */
    public boolean updateTotalFines(int customerId) {
        double totalFines = fineDAO.getTotalUnpaidFines(customerId);
        return customerDAO.updateTotalFines(customerId, totalFines);
    }
}
