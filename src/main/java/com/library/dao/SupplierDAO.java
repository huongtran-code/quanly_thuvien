package com.library.dao;

import com.library.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: Thao tác CRUD bảng suppliers
 */
public class SupplierDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public List<Supplier> findAll() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY name";
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
     * Lấy chỉ nhà cung cấp đang hoạt động (cho dropdown)
     */
    public List<Supplier> findActive() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE active = 1 ORDER BY name";
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

    public Supplier findById(int id) {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
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

    public boolean save(Supplier sup) {
        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address, active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sup.getName());
            stmt.setString(2, sup.getContactPerson());
            stmt.setString(3, sup.getPhone());
            stmt.setString(4, sup.getEmail());
            stmt.setString(5, sup.getAddress());
            stmt.setBoolean(6, sup.isActive());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) sup.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Supplier sup) {
        String sql = "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=?, active=? WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sup.getName());
            stmt.setString(2, sup.getContactPerson());
            stmt.setString(3, sup.getPhone());
            stmt.setString(4, sup.getEmail());
            stmt.setString(5, sup.getAddress());
            stmt.setBoolean(6, sup.isActive());
            stmt.setInt(7, sup.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean toggleActive(int id, boolean active) {
        String sql = "UPDATE suppliers SET active = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier sup = new Supplier();
        sup.setId(rs.getInt("id"));
        sup.setName(rs.getString("name"));
        sup.setContactPerson(rs.getString("contact_person"));
        sup.setPhone(rs.getString("phone"));
        sup.setEmail(rs.getString("email"));
        sup.setAddress(rs.getString("address"));
        sup.setActive(rs.getBoolean("active"));
        return sup;
    }
}
