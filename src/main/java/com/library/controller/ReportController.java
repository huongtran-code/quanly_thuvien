package com.library.controller;

import com.library.dao.BudgetDAO;
import com.library.dao.PurchaseOrderDAO;
import com.library.model.Budget;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller: Báo cáo thống kê
 */
public class ReportController {

    private final PurchaseOrderDAO orderDAO = new PurchaseOrderDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();

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
}
