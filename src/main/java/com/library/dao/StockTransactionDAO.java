package com.library.dao;

import com.library.model.StockTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: Bảng stock_transactions (lịch sử nhập/xuất kho)
 */
public class StockTransactionDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean save(StockTransaction tx) {
        String sql = """
                INSERT INTO stock_transactions (document_id, change_qty, type, source, created_by)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tx.getDocumentId());
            stmt.setInt(2, tx.getChangeQty());
            stmt.setString(3, tx.getType());
            stmt.setString(4, tx.getSource());
            if (tx.getCreatedBy() != null) {
                stmt.setInt(5, tx.getCreatedBy());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy các giao dịch gần nhất kèm tên tài liệu và người thực hiện
     */
    public List<StockTransaction> findRecent(int limit) {
        List<StockTransaction> list = new ArrayList<>();
        String sql = """
                SELECT t.*, d.title AS document_title, u.full_name AS created_by_name
                FROM stock_transactions t
                JOIN documents d ON t.document_id = d.id
                LEFT JOIN users u ON t.created_by = u.id
                ORDER BY t.created_at DESC, t.id DESC
                LIMIT ?
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                StockTransaction tx = new StockTransaction();
                tx.setId(rs.getInt("id"));
                tx.setDocumentId(rs.getInt("document_id"));
                tx.setDocumentTitle(rs.getString("document_title"));
                tx.setChangeQty(rs.getInt("change_qty"));
                tx.setType(rs.getString("type"));
                tx.setSource(rs.getString("source"));
                int createdBy = rs.getInt("created_by");
                tx.setCreatedBy(rs.wasNull() ? null : createdBy);
                tx.setCreatedByName(rs.getString("created_by_name"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) tx.setCreatedAt(ts.toLocalDateTime());
                list.add(tx);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy các giao dịch XUẤT KHO gần nhất kèm thông tin tài liệu chi tiết.
     * Dùng cho màn hình hiển thị khách hàng.
     */
    public List<StockTransaction> findExportRecent(int limit) {
        List<StockTransaction> list = new ArrayList<>();
        String sql = """
                SELECT t.*, d.title AS document_title, d.author AS document_author,
                       d.isbn AS document_isbn, d.unit_price AS document_price,
                       u.full_name AS created_by_name
                FROM stock_transactions t
                JOIN documents d ON t.document_id = d.id
                LEFT JOIN users u ON t.created_by = u.id
                WHERE t.type = 'EXPORT'
                ORDER BY t.created_at DESC, t.id DESC
                LIMIT ?
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                StockTransaction tx = new StockTransaction();
                tx.setId(rs.getInt("id"));
                tx.setDocumentId(rs.getInt("document_id"));
                tx.setDocumentTitle(rs.getString("document_title"));
                tx.setChangeQty(rs.getInt("change_qty"));
                tx.setType(rs.getString("type"));
                tx.setSource(rs.getString("source"));
                int createdBy = rs.getInt("created_by");
                tx.setCreatedBy(rs.wasNull() ? null : createdBy);
                tx.setCreatedByName(rs.getString("created_by_name"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) tx.setCreatedAt(ts.toLocalDateTime());
                list.add(tx);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
