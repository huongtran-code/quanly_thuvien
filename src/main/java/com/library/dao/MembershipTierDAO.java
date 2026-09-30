package com.library.dao;

import com.library.model.MembershipTier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembershipTierDAO {
    
    /**
     * Lấy tất cả gói thành viên
     */
    public List<MembershipTier> getAllTiers() {
        List<MembershipTier> tiers = new ArrayList<>();
        String sql = "SELECT * FROM membership_tiers ORDER BY price ASC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tiers.add(extractTierFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tiers;
    }
    
    /**
     * Lấy gói thành viên theo ID
     */
    public MembershipTier getTierById(int id) {
        String sql = "SELECT * FROM membership_tiers WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractTierFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Lấy gói thành viên theo tên
     */
    public MembershipTier getTierByName(String tierName) {
        String sql = "SELECT * FROM membership_tiers WHERE tier_name=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tierName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractTierFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Lấy các gói thành viên đang hoạt động
     */
    public List<MembershipTier> getActiveTiers() {
        List<MembershipTier> tiers = new ArrayList<>();
        String sql = "SELECT * FROM membership_tiers WHERE active=1 ORDER BY price ASC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tiers.add(extractTierFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tiers;
    }
    
    /**
     * Cập nhật thông tin gói thành viên
     */
    public boolean updateTier(MembershipTier tier) {
        String sql = "UPDATE membership_tiers SET max_books=?, borrow_days=?, " +
                    "late_fee_per_day=?, price=?, discount_percent=?, description=?, active=? " +
                    "WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tier.getMaxBooks());
            stmt.setInt(2, tier.getBorrowDays());
            stmt.setDouble(3, tier.getLateFeePerDay());
            stmt.setDouble(4, tier.getPrice());
            stmt.setDouble(5, tier.getDiscountPercent());
            stmt.setString(6, tier.getDescription());
            stmt.setBoolean(7, tier.isActive());
            stmt.setInt(8, tier.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Kích hoạt/Vô hiệu hóa gói thành viên
     */
    public boolean toggleTierActive(int tierId, boolean active) {
        String sql = "UPDATE membership_tiers SET active=? WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, active);
            stmt.setInt(2, tierId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Trích xuất MembershipTier từ ResultSet
     */
    private MembershipTier extractTierFromResultSet(ResultSet rs) throws SQLException {
        MembershipTier tier = new MembershipTier();
        tier.setId(rs.getInt("id"));
        tier.setTierName(rs.getString("tier_name"));
        tier.setMaxBooks(rs.getInt("max_books"));
        tier.setBorrowDays(rs.getInt("borrow_days"));
        tier.setLateFeePerDay(rs.getDouble("late_fee_per_day"));
        tier.setPrice(rs.getDouble("price"));
        tier.setDiscountPercent(rs.getDouble("discount_percent"));
        tier.setDescription(rs.getString("description"));
        tier.setActive(rs.getBoolean("active"));
        tier.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return tier;
    }
}
