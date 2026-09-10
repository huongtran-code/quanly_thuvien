package com.library.dao;

import com.library.model.Budget;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: Thao tác CRUD bảng budgets
 */
public class BudgetDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public List<Budget> findAll() {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM budgets ORDER BY fiscal_year DESC, budget_name";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy ngân sách đang hoạt động (cho dropdown tạo đơn mua)
     */
    public List<Budget> findActive() {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM budgets WHERE status = 'ACTIVE' ORDER BY fiscal_year DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Budget findById(int id) {
        String sql = "SELECT * FROM budgets WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(Budget budget) {
        String sql = "INSERT INTO budgets (fiscal_year, budget_name, total_amount, spent_amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, budget.getFiscalYear());
            stmt.setString(2, budget.getBudgetName());
            stmt.setDouble(3, budget.getTotalAmount());
            stmt.setDouble(4, budget.getSpentAmount());
            stmt.setString(5, budget.getStatus());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) budget.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Budget budget) {
        String sql = "UPDATE budgets SET fiscal_year=?, budget_name=?, total_amount=?, status=? WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, budget.getFiscalYear());
            stmt.setString(2, budget.getBudgetName());
            stmt.setDouble(3, budget.getTotalAmount());
            stmt.setString(4, budget.getStatus());
            stmt.setInt(5, budget.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật số tiền đã chi (khi duyệt đơn mua)
     */
    public boolean updateSpent(int budgetId, double amount) {
        String sql = "UPDATE budgets SET spent_amount = spent_amount + ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, budgetId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Đóng ngân sách — cắt ngân sách còn lại (total = spent)
     */
    public boolean close(int budgetId) {
        String sql = "UPDATE budgets SET status = 'CLOSED', total_amount = spent_amount WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, budgetId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mở lại ngân sách đã đóng
     */
    public boolean reopen(int budgetId, double newTotalAmount) {
        String sql = "UPDATE budgets SET status = 'ACTIVE', total_amount = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newTotalAmount);
            stmt.setInt(2, budgetId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Budget mapRow(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setId(rs.getInt("id"));
        b.setFiscalYear(rs.getInt("fiscal_year"));
        b.setBudgetName(rs.getString("budget_name"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setSpentAmount(rs.getDouble("spent_amount"));
        b.setRemainingAmount(rs.getDouble("remaining_amount"));
        b.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) b.setCreatedAt(ts.toLocalDateTime());
        return b;
    }
}
