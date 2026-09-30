package com.library.dao;

import com.library.model.Customer;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    
    /**
     * Tạo khách hàng mới
     */
    public boolean createCustomer(Customer customer) {
        String sql = "INSERT INTO customers (customer_code, full_name, email, phone, address, " +
                    "date_of_birth, id_card, registration_date, status, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, customer.getCustomerCode());
            stmt.setString(2, customer.getFullName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getAddress());
            stmt.setDate(6, customer.getDateOfBirth() != null ? Date.valueOf(customer.getDateOfBirth()) : null);
            stmt.setString(7, customer.getIdCard());
            stmt.setDate(8, Date.valueOf(customer.getRegistrationDate()));
            stmt.setString(9, customer.getStatus());
            stmt.setString(10, customer.getNotes());
            
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        customer.setId(rs.getInt(1));
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
     * Cập nhật thông tin khách hàng
     */
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET full_name=?, email=?, phone=?, address=?, " +
                    "date_of_birth=?, id_card=?, status=?, notes=? WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());
            stmt.setDate(5, customer.getDateOfBirth() != null ? Date.valueOf(customer.getDateOfBirth()) : null);
            stmt.setString(6, customer.getIdCard());
            stmt.setString(7, customer.getStatus());
            stmt.setString(8, customer.getNotes());
            stmt.setInt(9, customer.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Xóa khách hàng (soft delete - chuyển sang INACTIVE)
     */
    public boolean deleteCustomer(int customerId) {
        String sql = "UPDATE customers SET status='INACTIVE' WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Lấy khách hàng theo ID
     */
    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractCustomerFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Lấy khách hàng theo mã khách hàng
     */
    public Customer getCustomerByCode(String customerCode) {
        String sql = "SELECT * FROM customers WHERE customer_code=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customerCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractCustomerFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Lấy tất cả khách hàng
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY registration_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                customers.add(extractCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    
    /**
     * Tìm kiếm khách hàng theo từ khóa.
     * Hỗ trợ tìm từng từ trong họ tên (không cần gõ đầy đủ họ tên).
     * VD: gõ "An" sẽ tìm ra "Lý Văn An Bình", "Nguyễn Thị An", ...
     */
    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }

        // Split keyword into individual tokens (each word typed)
        String[] tokens = keyword.trim().split("\\s+");

        List<Customer> customers = new ArrayList<>();

        // Build SQL: each token must match at least one part of full_name (word boundary LIKE)
        // Also search by customer_code, email, phone (with full keyword)
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM customers WHERE ("
        );

        // For multi-token: each token must appear somewhere in full_name (as a word)
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) sql.append(" AND ");
            sql.append("(full_name LIKE ? COLLATE utf8mb4_general_ci)");
        }

        // Also match customer_code, email, phone with full keyword
        sql.append(") OR customer_code LIKE ? OR email LIKE ? OR phone LIKE ? ")
           .append("ORDER BY registration_date DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIdx = 1;
            for (String token : tokens) {
                stmt.setString(paramIdx++, "%" + token + "%");
            }
            // full keyword for code/email/phone
            String fullPattern = "%" + keyword.trim() + "%";
            stmt.setString(paramIdx++, fullPattern);
            stmt.setString(paramIdx++, fullPattern);
            stmt.setString(paramIdx++, fullPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(extractCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }


    /**
     * Lấy khách hàng theo trạng thái
     */
    public List<Customer> getCustomersByStatus(String status) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE status=? ORDER BY registration_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                customers.add(extractCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    
    /**
     * Cập nhật tổng số sách đã mượn
     */
    public boolean updateTotalBorrowed(int customerId, int totalBorrowed) {
        String sql = "UPDATE customers SET total_borrowed=? WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, totalBorrowed);
            stmt.setInt(2, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Cập nhật tổng tiền phạt
     */
    public boolean updateTotalFines(int customerId, double totalFines) {
        String sql = "UPDATE customers SET total_fines=? WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, totalFines);
            stmt.setInt(2, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Sinh mã khách hàng mới
     */
    public String generateCustomerCode() {
        String prefix = "KH";
        String sql = "SELECT customer_code FROM customers WHERE customer_code LIKE 'KH%'";
        
        int maxNumber = 0;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String code = rs.getString("customer_code");
                if (code != null && code.length() >= 3) {
                    try {
                        int number = Integer.parseInt(code.substring(2));
                        if (number > maxNumber) {
                            maxNumber = number;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.format("KH%06d", maxNumber + 1);
    }
    
    /**
     * Kiểm tra email đã tồn tại chưa
     */
    public boolean isEmailExists(String email, int excludeId) {
        String sql = "SELECT COUNT(*) FROM customers WHERE email=? AND id!=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setInt(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Trích xuất Customer từ ResultSet
     */
    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setCustomerCode(rs.getString("customer_code"));
        customer.setFullName(rs.getString("full_name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        
        Date dob = rs.getDate("date_of_birth");
        customer.setDateOfBirth(dob != null ? dob.toLocalDate() : null);
        
        customer.setIdCard(rs.getString("id_card"));
        customer.setRegistrationDate(rs.getDate("registration_date").toLocalDate());
        customer.setStatus(rs.getString("status"));
        customer.setTotalBorrowed(rs.getInt("total_borrowed"));
        customer.setTotalFines(rs.getDouble("total_fines"));
        customer.setNotes(rs.getString("notes"));
        customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        return customer;
    }
}
