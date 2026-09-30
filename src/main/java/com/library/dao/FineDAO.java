package com.library.dao;

import com.library.model.Fine;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {
    
    /**
     * Tạo khoản phạt mới
     */
    public boolean createFine(Fine fine) {
        String sql = "INSERT INTO fines (customer_id, borrow_id, fine_type, amount, reason, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, fine.getCustomerId());
            stmt.setObject(2, fine.getBorrowId());
            stmt.setString(3, fine.getFineType());
            stmt.setDouble(4, fine.getAmount());
            stmt.setString(5, fine.getReason());
            stmt.setString(6, fine.getStatus());
            
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        fine.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Thanh toán phạt
     */
    public boolean payFine(int fineId, Integer paidBy) {
        String sql = "UPDATE fines SET status='PAID', paid_date=?, paid_by=? WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setObject(2, paidBy);
            stmt.setInt(3, fineId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Miễn phạt
     */
    public boolean waiveFine(int fineId, Integer waivedBy) {
        String sql = "UPDATE fines SET status='WAIVED', paid_by=? WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, waivedBy);
            stmt.setInt(2, fineId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Lấy khoản phạt theo ID
     */
    public Fine getFineById(int id) {
        String sql = "SELECT * FROM fines WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractFineFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Lấy tất cả khoản phạt
     */
    public List<Fine> getAllFines() {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT * FROM fines ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                fines.add(extractFineFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fines;
    }
    
    /**
     * Lấy khoản phạt theo khách hàng
     */
    public List<Fine> getFinesByCustomer(int customerId) {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT * FROM fines WHERE customer_id=? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                fines.add(extractFineFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fines;
    }
    
    /**
     * Lấy khoản phạt theo trạng thái
     */
    public List<Fine> getFinesByStatus(String status) {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT * FROM fines WHERE status=? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                fines.add(extractFineFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fines;
    }
    
    /**
     * Lấy các khoản phạt chưa thanh toán của khách hàng
     */
    public List<Fine> getUnpaidFinesByCustomer(int customerId) {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT * FROM fines WHERE customer_id=? AND status='UNPAID' ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                fines.add(extractFineFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fines;
    }
    
    /**
     * Tính tổng tiền phạt chưa thanh toán của khách hàng
     */
    public double getTotalUnpaidFines(int customerId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM fines " +
                    "WHERE customer_id=? AND status='UNPAID'";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    /**
     * Lấy khoản phạt theo phiếu mượn
     */
    public List<Fine> getFinesByBorrow(int borrowId) {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT * FROM fines WHERE borrow_id=? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, borrowId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                fines.add(extractFineFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fines;
    }
    
    /**
     * Trích xuất Fine từ ResultSet
     */
    private Fine extractFineFromResultSet(ResultSet rs) throws SQLException {
        Fine fine = new Fine();
        fine.setId(rs.getInt("id"));
        fine.setCustomerId(rs.getInt("customer_id"));
        
        Integer borrowId = (Integer) rs.getObject("borrow_id");
        fine.setBorrowId(borrowId);
        
        fine.setFineType(rs.getString("fine_type"));
        fine.setAmount(rs.getDouble("amount"));
        fine.setReason(rs.getString("reason"));
        fine.setStatus(rs.getString("status"));
        
        Timestamp paidDate = rs.getTimestamp("paid_date");
        fine.setPaidDate(paidDate != null ? paidDate.toLocalDateTime() : null);
        
        Integer paidBy = (Integer) rs.getObject("paid_by");
        fine.setPaidBy(paidBy);
        
        fine.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return fine;
    }
}
