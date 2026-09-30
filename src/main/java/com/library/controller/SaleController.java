package com.library.controller;

import com.library.dao.CustomerDAO;
import com.library.dao.CustomerMembershipDAO;
import com.library.dao.DocumentDAO;
import com.library.dao.MembershipTierDAO;
import com.library.dao.SaleDAO;
import com.library.model.Customer;
import com.library.model.CustomerMembership;
import com.library.model.Document;
import com.library.model.MembershipTier;
import com.library.model.Sale;
import com.library.model.SaleItem;
import com.library.util.AppConstants;

import java.time.LocalDateTime;
import java.util.List;

public class SaleController {
    
    private final SaleDAO saleDAO;
    private final CustomerDAO customerDAO;
    private final DocumentDAO documentDAO;
    private final MembershipTierDAO tierDAO;
    private final CustomerMembershipDAO membershipDAO;
    
    public SaleController() {
        this.saleDAO = new SaleDAO();
        this.customerDAO = new CustomerDAO();
        this.documentDAO = new DocumentDAO();
        this.tierDAO = new MembershipTierDAO();
        this.membershipDAO = new CustomerMembershipDAO();
    }
    
    /**
     * Tạo đơn bán mới
     */
    public boolean createSale(Sale sale, List<SaleItem> items, Integer createdBy) {
        // Validate items
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sách bán không được rỗng");
        }
        
        // Validate stock and calculate total
        double totalAmount = 0.0;
        for (SaleItem item : items) {
            Document doc = documentDAO.findById(item.getDocumentId());
            if (doc == null) {
                throw new IllegalArgumentException("Không tìm thấy tài liệu ID: " + item.getDocumentId());
            }
            if (doc.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Không đủ tồn kho cho tài liệu: " + doc.getTitle());
            }
            
            // Set unit price and calculate subtotal
            item.setUnitPrice(doc.getUnitPrice());
            item.setSubtotal(item.getQuantity() * doc.getUnitPrice());
            totalAmount += item.getSubtotal();
        }
        
        // Calculate discount if customer has membership
        double discountAmount = 0.0;
        if (sale.getCustomerId() != null) {
            Customer customer = customerDAO.getCustomerById(sale.getCustomerId());
            if (customer != null && customer.isActive()) {
                CustomerMembership membership = membershipDAO.getActiveMembershipByCustomer(sale.getCustomerId());
                if (membership != null && membership.isActive()) {
                    MembershipTier tier = tierDAO.getTierById(membership.getTierId());
                    if (tier != null && tier.getDiscountPercent() > 0) {
                        discountAmount = totalAmount * (tier.getDiscountPercent() / 100.0);
                    }
                }
            }
        }
        
        // Set sale info
        sale.setSaleCode(saleDAO.generateSaleCode());
        sale.setSaleDate(LocalDateTime.now());
        sale.setTotalAmount(totalAmount);
        sale.setDiscountAmount(discountAmount);
        sale.setFinalAmount(totalAmount - discountAmount);
        sale.setCreatedBy(createdBy);
        
        // Set default payment method
        if (sale.getPaymentMethod() == null) {
            sale.setPaymentMethod(AppConstants.PAYMENT_CASH);
        }
        
        return saleDAO.createSale(sale, items);
    }
    
    /**
     * Lấy đơn bán theo ID
     */
    public Sale getSaleById(int id) {
        return saleDAO.getSaleById(id);
    }
    
    /**
     * Lấy đơn bán theo mã
     */
    public Sale getSaleByCode(String code) {
        return saleDAO.getSaleByCode(code);
    }
    
    /**
     * Lấy tất cả đơn bán
     */
    public List<Sale> getAllSales() {
        return saleDAO.getAllSales();
    }
    
    /**
     * Lấy đơn bán theo khách hàng
     */
    public List<Sale> getSalesByCustomer(int customerId) {
        return saleDAO.getSalesByCustomer(customerId);
    }
    
    /**
     * Lấy đơn bán trong khoảng thời gian
     */
    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return saleDAO.getSalesByDateRange(startDate, endDate);
    }
    
    /**
     * Lấy chi tiết items
     */
    public List<SaleItem> getSaleItems(int saleId) {
        return saleDAO.getSaleItems(saleId);
    }
    
    /**
     * Tìm kiếm đơn bán
     */
    public List<Sale> searchSales(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSales();
        }
        return saleDAO.searchSales(keyword);
    }
    
    /**
     * Tính tổng doanh thu trong khoảng thời gian
     */
    public double getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return saleDAO.getTotalRevenue(startDate, endDate);
    }
    
    /**
     * Tính discount cho khách hàng
     */
    public double calculateDiscount(Integer customerId, double totalAmount) {
        if (customerId == null) {
            return 0.0;
        }
        
        Customer customer = customerDAO.getCustomerById(customerId);
        if (customer == null || !customer.isActive()) {
            return 0.0;
        }
        
        CustomerMembership membership = membershipDAO.getActiveMembershipByCustomer(customerId);
        if (membership == null || !membership.isActive()) {
            return 0.0;
        }
        
        MembershipTier tier = tierDAO.getTierById(membership.getTierId());
        if (tier == null || tier.getDiscountPercent() <= 0) {
            return 0.0;
        }
        
        return totalAmount * (tier.getDiscountPercent() / 100.0);
    }
}
