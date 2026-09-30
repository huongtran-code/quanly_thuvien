package com.library.dao;

import com.library.model.CustomerMembership;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerMembershipDAO {
    
    /**
     * Tạo gói thành viên cho khách hàng
     */
    public boolean createMembership(CustomerMembership membership) {
        String sql = "INSERT INTO customer_memberships (customer_id, tier_id, start_date, end_date, status) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, membership.getCustomerId());
            stmt.setInt(2, membership.getTierId());
            stmt.setDate(3, Date.valueOf(membership.getStartDate()));
            stmt.setDate(4, Date.valueOf(membership.getEndDate()));
            stmt.setString(5, membership.getStatus());
            
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        membership.setId(rs.getInt(1));
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
     * Cập nhật gói thành viên
     */
    public boolean updateMembership(CustomerMembership membership) {
        String sql = "UPDATE customer_memberships SET tier_id=?, start_date=?, end_date=?, status=? WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, membership.getTierId());
            stmt.setDate(2, Date.valueOf(membership.getStartDate()));
            stmt.setDate(3, Date.valueOf(membership.getEndDate()));
            stmt.setString(4, membership.getStatus());
            stmt.setInt(5, membership.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Gia hạn gói thành viên
     */
    public boolean renewMembership(int membershipId, LocalDate newEndDate) {
        String sql = "UPDATE customer_memberships SET end_date=?, status='ACTIVE' WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(newEndDate));
            stmt.setInt(2, membershipId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Hủy gói thành viên
     */
    public boolean cancelMembership(int membershipId) {
        String sql = "UPDATE customer_memberships SET status='SUSPENDED' WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, membershipId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Lấy gói thành viên hiện tại của khách hàng
     */
    public CustomerMembership getActiveMembershipByCustomer(int customerId) {
        String sql = "SELECT * FROM customer_memberships WHERE customer_id=? AND status='ACTIVE' " +
                    "ORDER BY end_date DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractMembershipFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Lấy tất cả gói thành viên của khách hàng
     */
    public List<CustomerMembership> getMembershipsByCustomer(int customerId) {
        List<CustomerMembership> memberships = new ArrayList<>();
        String sql = "SELECT * FROM customer_memberships WHERE customer_id=? ORDER BY start_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                memberships.add(extractMembershipFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memberships;
    }
    
    /**
     * Lấy gói thành viên theo ID
     */
    public CustomerMembership getMembershipById(int id) {
        String sql = "SELECT * FROM customer_memberships WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractMembershipFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Lấy tất cả gói thành viên
     */
    public List<CustomerMembership> getAllMemberships() {
        List<CustomerMembership> memberships = new ArrayList<>();
        String sql = "SELECT * FROM customer_memberships ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                memberships.add(extractMembershipFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memberships;
    }
    
    /**
     * Lấy các gói thành viên sắp hết hạn (còn <= N ngày)
     */
    public List<CustomerMembership> getExpiringMemberships(int daysBeforeExpiry) {
        List<CustomerMembership> memberships = new ArrayList<>();
        String sql = "SELECT * FROM customer_memberships WHERE status='ACTIVE' " +
                    "AND end_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                    "ORDER BY end_date ASC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, daysBeforeExpiry);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                memberships.add(extractMembershipFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memberships;
    }
    
    /**
     * Cập nhật trạng thái hết hạn cho các gói đã hết hạn
     */
    public int updateExpiredMemberships() {
        String sql = "UPDATE customer_memberships SET status='EXPIRED' " +
                    "WHERE status='ACTIVE' AND end_date < CURDATE()";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Trích xuất CustomerMembership từ ResultSet
     */
    private CustomerMembership extractMembershipFromResultSet(ResultSet rs) throws SQLException {
        CustomerMembership membership = new CustomerMembership();
        membership.setId(rs.getInt("id"));
        membership.setCustomerId(rs.getInt("customer_id"));
        membership.setTierId(rs.getInt("tier_id"));
        membership.setStartDate(rs.getDate("start_date").toLocalDate());
        membership.setEndDate(rs.getDate("end_date").toLocalDate());
        membership.setStatus(rs.getString("status"));
        membership.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return membership;
    }
}
