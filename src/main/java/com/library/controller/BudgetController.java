package com.library.controller;

import com.library.dao.BudgetDAO;
import com.library.model.Budget;
import com.library.util.CurrencyUtil;

import java.util.List;

/**
 * Controller: Quản lý ngân sách
 */
public class BudgetController {

    private final BudgetDAO budgetDAO = new BudgetDAO();

    public List<Budget> getAllBudgets() {
        return budgetDAO.findAll();
    }

    public List<Budget> getActiveBudgets() {
        return budgetDAO.findActive();
    }

    public Budget getBudgetById(int id) {
        return budgetDAO.findById(id);
    }

    public String saveBudget(Budget budget) {
        if (budget.getBudgetName() == null || budget.getBudgetName().isBlank()) {
            return "Tên ngân sách không được để trống!";
        }
        if (budget.getFiscalYear() < 2000 || budget.getFiscalYear() > 2100) {
            return "Năm tài chính không hợp lệ!";
        }
        if (budget.getTotalAmount() <= 0) {
            return "Tổng ngân sách phải lớn hơn 0!";
        }
        boolean success;
        if (budget.getId() > 0) {
            success = budgetDAO.update(budget);
        } else {
            success = budgetDAO.save(budget);
        }
        return success ? null : "Lỗi lưu ngân sách!";
    }

    public String closeBudget(int id) {
        return budgetDAO.close(id) ? null : "Lỗi đóng ngân sách!";
    }

    public String reopenBudget(int id, double newTotalAmount) {
        if (newTotalAmount <= 0) {
            return "Tổng ngân sách phải lớn hơn 0!";
        }
        Budget b = budgetDAO.findById(id);
        if (b == null) return "Không tìm thấy ngân sách!";
        if (newTotalAmount < b.getSpentAmount()) {
            return "Tổng ngân sách không được nhỏ hơn số đã chi (" + CurrencyUtil.format(b.getSpentAmount()) + ")!";
        }
        return budgetDAO.reopen(id, newTotalAmount) ? null : "Lỗi mở lại ngân sách!";
    }
}
