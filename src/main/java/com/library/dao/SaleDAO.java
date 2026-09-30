package com.library.dao;

import com.library.model.Sale;
import com.library.model.SaleItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleDAO {
    
    /**
     * Tạo đơn bán mới
     */
    public boolean createSale(Sale sale, List<SaleItem> items) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // 1. Thêm sale
            String saleSql = "INSERT INTO sales (sale_code, customer_id, sale_date, total_amount, " +
                            "discount_amount, final_amount, payment_method, notes, created_by) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            int saleId;
            try (PreparedStatement stmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, sale.getSaleCode());
                stmt.setObject(2, sale.getCustomerId());
                stmt.setTimestamp(3, Timestamp.valueOf(sale.getSaleDate()));
                stmt.setDouble(4, sale.getTotalAmount());
                stmt.setDouble(5, sale.getDiscountAmount());
                stmt.setDouble(6, sale.getFinalAmount());
                stmt.setString(7, sale.getPaymentMethod());
                stmt.setString(8, sale.getNotes());
                stmt.setObject(9, sale.getCreatedBy());
                
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    saleId = rs.getInt(1);
                    sale.setId(saleId);
                } else {
                    throw new SQLException("Creating sale failed, no ID obtained.");
                }
            }
            
            // 2. Thêm sale_items (subtotal là generated column, không cần INSERT)
            String itemSql = "INSERT INTO sale_items (sale_id, document_id, quantity, unit_price) " +
                            "VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                for (SaleItem item : items) {
                    stmt.setInt(1, saleId);
                    stmt.setInt(2, item.getDocumentId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setDouble(4, item.getUnitPrice());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            // 3. Cập nhật stock trong documents
            String stockSql = "UPDATE documents SET stock_quantity = stock_quantity - ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(stockSql)) {
                for (SaleItem item : items) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setInt(2, item.getDocumentId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Lấy đơn bán theo ID
     */
    public Sale getSaleById(int id) {
        String sql = "SELECT * FROM sales WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractSaleFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Lấy đơn bán theo mã
     */
    public Sale getSaleByCode(String saleCode) {
        String sql = "SELECT * FROM sales WHERE sale_code=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, saleCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractSaleFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Lấy tất cả đơn bán
     */
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                sales.add(extractSaleFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }
    
    /**
     * Lấy đơn bán theo khách hàng
     */
    public List<Sale> getSalesByCustomer(int customerId) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE customer_id=? ORDER BY sale_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                sales.add(extractSaleFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }
    
    /**
     * Lấy đơn bán trong khoảng thời gian
     */
    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE sale_date BETWEEN ? AND ? ORDER BY sale_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                sales.add(extractSaleFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }
    
    /**
     * Lấy chi tiết items của đơn bán
     */
    public List<SaleItem> getSaleItems(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT * FROM sale_items WHERE sale_id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                SaleItem item = new SaleItem();
                item.setId(rs.getInt("id"));
                item.setSaleId(rs.getInt("sale_id"));
                item.setDocumentId(rs.getInt("document_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setSubtotal(rs.getDouble("subtotal"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    /**
     * Sinh mã đơn bán mới (SL + YYYYMM + số thứ tự)
     */
    public String generateSaleCode() {
        String yearMonth = String.format("%tY%<tm", System.currentTimeMillis());
        String prefix = "SL" + yearMonth;
        String sql = "SELECT sale_code FROM sales WHERE sale_code LIKE ?";
        
        int maxNumber = 0;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String code = rs.getString("sale_code");
                if (code != null && code.length() >= prefix.length()) {
                    try {
                        int number = Integer.parseInt(code.substring(prefix.length()));
                        if (number > maxNumber) {
                            maxNumber = number;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.format("%s%03d", prefix, maxNumber + 1);
    }
    
    /**
     * Tính tổng doanh thu trong khoảng thời gian
     */
    public double getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT COALESCE(SUM(final_amount), 0) as total FROM sales " +
                    "WHERE sale_date BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
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
     * Lấy doanh thu theo tháng trong năm
     */
    public Map<Integer, Double> getMonthlyRevenue(int year) {
        Map<Integer, Double> result = new HashMap<>();
        // Initialize all months with 0
        for (int i = 1; i <= 12; i++) {
            result.put(i, 0.0);
        }
        
        String sql = "SELECT MONTH(sale_date) as month, COALESCE(SUM(final_amount), 0) as total " +
                    "FROM sales WHERE YEAR(sale_date) = ? " +
                    "GROUP BY MONTH(sale_date)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, year);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int month = rs.getInt("month");
                double total = rs.getDouble("total");
                result.put(month, total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * Tìm kiếm đơn bán
     */
    public List<Sale> searchSales(String keyword) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.* FROM sales s " +
                    "LEFT JOIN customers c ON s.customer_id = c.id " +
                    "WHERE s.sale_code LIKE ? OR c.full_name LIKE ? OR c.customer_code LIKE ? " +
                    "ORDER BY s.sale_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sales.add(extractSaleFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }
    
    /**
     * Trích xuất Sale từ ResultSet
     */
    private Sale extractSaleFromResultSet(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setId(rs.getInt("id"));
        sale.setSaleCode(rs.getString("sale_code"));
        
        Integer customerId = (Integer) rs.getObject("customer_id");
        sale.setCustomerId(customerId);
        
        sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
        sale.setTotalAmount(rs.getDouble("total_amount"));
        sale.setDiscountAmount(rs.getDouble("discount_amount"));
        sale.setFinalAmount(rs.getDouble("final_amount"));
        sale.setPaymentMethod(rs.getString("payment_method"));
        sale.setNotes(rs.getString("notes"));
        
        Integer createdBy = (Integer) rs.getObject("created_by");
        sale.setCreatedBy(createdBy);
        
        sale.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return sale;
    }
}
