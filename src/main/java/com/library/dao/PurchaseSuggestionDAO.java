package com.library.dao;

import com.library.model.PurchaseSuggestion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: Đề xuất mua sách
 */
public class PurchaseSuggestionDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    /**
     * Tạo bảng nếu chưa tồn tại (gọi khi khởi động)
     */
    public void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS purchase_suggestions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    book_title NVARCHAR(300) NOT NULL,
                    author NVARCHAR(200),
                    isbn VARCHAR(20),
                    publisher NVARCHAR(200),
                    quantity INT NOT NULL DEFAULT 1,
                    reason NVARCHAR(500),
                    suggested_by NVARCHAR(100) NOT NULL,
                    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
                    review_note NVARCHAR(500),
                    reviewed_by INT,
                    reviewed_at DATETIME,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean save(PurchaseSuggestion s) {
        String sql = """
                INSERT INTO purchase_suggestions (book_title, author, isbn, publisher, quantity, reason, suggested_by)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, s.getBookTitle());
            stmt.setString(2, s.getAuthor());
            stmt.setString(3, s.getIsbn());
            stmt.setString(4, s.getPublisher());
            stmt.setInt(5, s.getQuantity());
            stmt.setString(6, s.getReason());
            stmt.setString(7, s.getSuggestedBy());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<PurchaseSuggestion> findAll() {
        List<PurchaseSuggestion> list = new ArrayList<>();
        String sql = """
                SELECT ps.*, u.full_name AS reviewed_by_name
                FROM purchase_suggestions ps
                LEFT JOIN users u ON ps.reviewed_by = u.id
                ORDER BY ps.created_at DESC
                """;
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

    public boolean updateStatus(int id, String status, String reviewNote, int reviewedBy) {
        String sql = """
                UPDATE purchase_suggestions
                SET status = ?, review_note = ?, reviewed_by = ?, reviewed_at = NOW()
                WHERE id = ?
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, reviewNote);
            stmt.setInt(3, reviewedBy);
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM purchase_suggestions WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countPending() {
        String sql = "SELECT COUNT(*) FROM purchase_suggestions WHERE status = 'PENDING'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private PurchaseSuggestion mapRow(ResultSet rs) throws SQLException {
        PurchaseSuggestion s = new PurchaseSuggestion();
        s.setId(rs.getInt("id"));
        s.setBookTitle(rs.getString("book_title"));
        s.setAuthor(rs.getString("author"));
        s.setIsbn(rs.getString("isbn"));
        s.setPublisher(rs.getString("publisher"));
        s.setQuantity(rs.getInt("quantity"));
        s.setReason(rs.getString("reason"));
        s.setSuggestedBy(rs.getString("suggested_by"));
        s.setStatus(rs.getString("status"));
        s.setReviewNote(rs.getString("review_note"));
        int reviewedBy = rs.getInt("reviewed_by");
        s.setReviewedBy(rs.wasNull() ? null : reviewedBy);
        try { s.setReviewedByName(rs.getString("reviewed_by_name")); } catch (SQLException ignored) {}
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) s.setCreatedAt(ca.toLocalDateTime());
        Timestamp ra = rs.getTimestamp("reviewed_at");
        if (ra != null) s.setReviewedAt(ra.toLocalDateTime());
        return s;
    }
}
