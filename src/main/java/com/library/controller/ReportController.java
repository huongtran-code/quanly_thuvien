package com.library.controller;

import com.library.dao.*;
import com.library.model.Budget;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller: Báo cáo thống kê
 */
public class ReportController {

    private final PurchaseOrderDAO orderDAO = new PurchaseOrderDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final FineDAO fineDAO = new FineDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    /**
     * Chi tiêu theo tháng
     */
    public Map<Integer, Double> getMonthlySpending(int year) {
        return orderDAO.getMonthlySpending(year);
    }

    /**
     * Chi tiêu theo nhà cung cấp
     */
    public Map<String, Double> getSpendingBySupplier(int year) {
        return orderDAO.getSpendingBySupplier(year);
    }

    /**
     * Chi tiêu theo danh mục
     */
    public Map<String, Double> getSpendingByCategory(int year) {
        return orderDAO.getSpendingByCategory(year);
    }

    /**
     * Số đơn theo trạng thái
     */
    public Map<String, Integer> getOrderCountByStatus() {
        return orderDAO.getOrderCountByStatus();
    }

    /**
     * Tổng hợp ngân sách
     */
    public List<Budget> getBudgetSummary() {
        return budgetDAO.findAll();
    }

    /**
     * Năm hiện tại
     */
    public int getCurrentYear() {
        return LocalDate.now().getYear();
    }
    
    // ==================== FRONT-OFFICE REPORTS ====================
    
    /**
     * Thống kê khách hàng theo trạng thái
     */
    public Map<String, Integer> getCustomerCountByStatus() {
        Map<String, Integer> result = new HashMap<>();
        result.put("ACTIVE", customerDAO.getCustomersByStatus("ACTIVE").size());
        result.put("SUSPENDED", customerDAO.getCustomersByStatus("SUSPENDED").size());
        result.put("INACTIVE", customerDAO.getCustomersByStatus("INACTIVE").size());
        return result;
    }
    
    /**
     * Thống kê mượn sách theo trạng thái
     */
    public Map<String, Integer> getBorrowCountByStatus() {
        Map<String, Integer> result = new HashMap<>();
        result.put("BORROWED", borrowDAO.getBorrowsByStatus("BORROWED").size());
        result.put("RETURNED", borrowDAO.getBorrowsByStatus("RETURNED").size());
        result.put("OVERDUE", borrowDAO.getOverdueBorrows().size());
        return result;
    }
    
    /**
     * Doanh thu bán sách theo tháng
     */
    public Map<Integer, Double> getMonthlySalesRevenue(int year) {
        return saleDAO.getMonthlyRevenue(year);
    }
    
    /**
     * Tổng doanh thu bán sách trong năm
     */
    public double getTotalSalesRevenue(int year) {
        return getMonthlySalesRevenue(year).values().stream()
                .mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * Thống kê phạt theo trạng thái
     */
    public Map<String, Integer> getFineCountByStatus() {
        Map<String, Integer> result = new HashMap<>();
        result.put("UNPAID", fineDAO.getFinesByStatus("UNPAID").size());
        result.put("PAID", fineDAO.getFinesByStatus("PAID").size());
        result.put("WAIVED", fineDAO.getFinesByStatus("WAIVED").size());
        return result;
    }
    
    /**
     * Tổng tiền phạt chưa thanh toán
     */
    public double getTotalUnpaidFines() {
        return fineDAO.getFinesByStatus("UNPAID").stream()
                .mapToDouble(fine -> fine.getAmount())
                .sum();
    }
    
    /**
     * Tổng tiền phạt đã thu trong năm
     */
    public double getTotalPaidFines(int year) {
        return fineDAO.getFinesByStatus("PAID").stream()
                .filter(fine -> fine.getPaidDate() != null && 
                               fine.getPaidDate().getYear() == year)
                .mapToDouble(fine -> fine.getAmount())
                .sum();
    }
}
