package com.library.dao;

import com.library.model.Borrow;
import com.library.model.BorrowItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {
    
    /**
     * Tạo phiếu mượn mới
     */
    public boolean createBorrow(Borrow borrow, List<BorrowItem> items) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // 1. Thêm borrow
            String borrowSql = "INSERT INTO borrows (borrow_code, customer_id, borrow_date, due_date, " +
                              "status, total_books, notes, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            int borrowId;
            try (PreparedStatement stmt = conn.prepareStatement(borrowSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, borrow.getBorrowCode());
                stmt.setInt(2, borrow.getCustomerId());
                stmt.setTimestamp(3, Timestamp.valueOf(borrow.getBorrowDate()));
                stmt.setTimestamp(4, Timestamp.valueOf(borrow.getDueDate()));
                stmt.setString(5, borrow.getStatus());
                stmt.setInt(6, borrow.getTotalBooks());
                stmt.setString(7, borrow.getNotes());
                stmt.setObject(8, borrow.getCreatedBy());
                
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    borrowId = rs.getInt(1);
                    borrow.setId(borrowId);
                } else {
                    throw new SQLException("Creating borrow failed, no ID obtained.");
                }
            }
            
            // 2. Thêm borrow_items
            String itemSql = "INSERT INTO borrow_items (borrow_id, document_id, quantity, returned) " +
                            "VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                for (BorrowItem item : items) {
                    stmt.setInt(1, borrowId);
                    stmt.setInt(2, item.getDocumentId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setBoolean(4, false);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            // 3. Cập nhật stock trong documents
            String stockSql = "UPDATE documents SET stock_quantity = stock_quantity - ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(stockSql)) {
                for (BorrowItem item : items) {
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
     * Trả sách
     */
    public boolean returnBorrow(int borrowId, double lateFee, Integer userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // 1. Cập nhật trạng thái borrow
            String borrowSql = "UPDATE borrows SET status='RETURNED', return_date=?, late_fee=? WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(borrowSql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setDouble(2, lateFee);
                stmt.setInt(3, borrowId);
                stmt.executeUpdate();
            }
            
            // 2. Lấy danh sách items
            List<BorrowItem> items = getBorrowItems(borrowId);
            
            // 3. Cập nhật stock
            String stockSql = "UPDATE documents SET stock_quantity = stock_quantity + ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(stockSql)) {
                for (BorrowItem item : items) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setInt(2, item.getDocumentId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            // 4. Cập nhật borrow_items
            String itemSql = "UPDATE borrow_items SET returned=1, return_date=? WHERE borrow_id=?";
            try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(2, borrowId);
                stmt.executeUpdate();
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
        return false;
    }
    
    /**
     * Lấy phiếu mượn theo ID
     */
    public Borrow getBorrowById(int id) {
        String sql = "SELECT * FROM borrows WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractBorrowFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Lấy phiếu mượn theo mã
     */
    public Borrow getBorrowByCode(String borrowCode) {
        String sql = "SELECT * FROM borrows WHERE borrow_code=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, borrowCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractBorrowFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Lấy tất cả phiếu mượn
     */
    public List<Borrow> getAllBorrows() {
        List<Borrow> borrows = new ArrayList<>();
        String sql = "SELECT * FROM borrows ORDER BY borrow_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                borrows.add(extractBorrowFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrows;
    }
    
    /**
     * Lấy phiếu mượn theo khách hàng
     */
    public List<Borrow> getBorrowsByCustomer(int customerId) {
        List<Borrow> borrows = new ArrayList<>();
        String sql = "SELECT * FROM borrows WHERE customer_id=? ORDER BY borrow_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                borrows.add(extractBorrowFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrows;
    }
    
    /**
     * Lấy phiếu mượn theo trạng thái
     */
    public List<Borrow> getBorrowsByStatus(String status) {
        List<Borrow> borrows = new ArrayList<>();
        String sql = "SELECT * FROM borrows WHERE status=? ORDER BY borrow_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                borrows.add(extractBorrowFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrows;
    }
    
    /**
     * Lấy các phiếu mượn quá hạn
     */
    public List<Borrow> getOverdueBorrows() {
        List<Borrow> borrows = new ArrayList<>();
        String sql = "SELECT * FROM borrows WHERE status='BORROWED' AND due_date < NOW() " +
                    "ORDER BY due_date ASC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                borrows.add(extractBorrowFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrows;
    }
    
    /**
     * Lấy chi tiết items của phiếu mượn
     */
    public List<BorrowItem> getBorrowItems(int borrowId) {
        List<BorrowItem> items = new ArrayList<>();
        String sql = "SELECT * FROM borrow_items WHERE borrow_id=?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, borrowId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                BorrowItem item = new BorrowItem();
                item.setId(rs.getInt("id"));
                item.setBorrowId(rs.getInt("borrow_id"));
                item.setDocumentId(rs.getInt("document_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setReturned(rs.getBoolean("returned"));
                
                Timestamp returnDate = rs.getTimestamp("return_date");
                item.setReturnDate(returnDate != null ? returnDate.toLocalDateTime() : null);
                
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    /**
     * Sinh mã phiếu mượn mới (BR + YYYYMM + số thứ tự)
     */
    public String generateBorrowCode() {
        String yearMonth = String.format("%tY%<tm", System.currentTimeMillis());
        String prefix = "BR" + yearMonth;
        String sql = "SELECT borrow_code FROM borrows WHERE borrow_code LIKE ?";
        
        int maxNumber = 0;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String code = rs.getString("borrow_code");
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
     * Cập nhật trạng thái quá hạn
     */
    public int updateOverdueStatus() {
        String sql = "UPDATE borrows SET status='OVERDUE' WHERE status='BORROWED' AND due_date < NOW()";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Tìm kiếm phiếu mượn.
     * Hỗ trợ tìm từng từ trong họ tên khách hàng.
     */
    public List<Borrow> searchBorrows(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBorrows();
        }

        String[] tokens = keyword.trim().split("\\s+");
        List<Borrow> borrows = new ArrayList<>();

        // Build SQL: each token must appear in customer full_name, OR match borrow_code/customer_code
        StringBuilder sql = new StringBuilder(
            "SELECT b.* FROM borrows b LEFT JOIN customers c ON b.customer_id = c.id WHERE ("
        );
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) sql.append(" AND ");
            sql.append("(c.full_name LIKE ? COLLATE utf8mb4_general_ci)");
        }
        sql.append(") OR b.borrow_code LIKE ? OR c.customer_code LIKE ? ")
           .append("ORDER BY b.borrow_date DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIdx = 1;
            for (String token : tokens) {
                stmt.setString(paramIdx++, "%" + token + "%");
            }
            String fullPattern = "%" + keyword.trim() + "%";
            stmt.setString(paramIdx++, fullPattern);
            stmt.setString(paramIdx++, fullPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                borrows.add(extractBorrowFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrows;
    }

    /**
     * Trích xuất Borrow từ ResultSet
     */
    private Borrow extractBorrowFromResultSet(ResultSet rs) throws SQLException {
        Borrow borrow = new Borrow();
        borrow.setId(rs.getInt("id"));
        borrow.setBorrowCode(rs.getString("borrow_code"));
        borrow.setCustomerId(rs.getInt("customer_id"));
        borrow.setBorrowDate(rs.getTimestamp("borrow_date").toLocalDateTime());
        borrow.setDueDate(rs.getTimestamp("due_date").toLocalDateTime());
        
        Timestamp returnDate = rs.getTimestamp("return_date");
        borrow.setReturnDate(returnDate != null ? returnDate.toLocalDateTime() : null);
        
        borrow.setStatus(rs.getString("status"));
        borrow.setTotalBooks(rs.getInt("total_books"));
        borrow.setLateFee(rs.getDouble("late_fee"));
        borrow.setNotes(rs.getString("notes"));
        
        Integer createdBy = (Integer) rs.getObject("created_by");
        borrow.setCreatedBy(createdBy);
        
        borrow.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return borrow;
    }
}
