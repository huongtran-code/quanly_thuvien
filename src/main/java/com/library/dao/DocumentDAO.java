package com.library.dao;

import com.library.model.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: Thao tác CRUD bảng documents
 */
public class DocumentDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String SELECT_ALL = """
            SELECT d.*, c.name AS category_name
            FROM documents d
            LEFT JOIN categories c ON d.category_id = c.id
            ORDER BY d.id ASC
            """;

    /**
     * Lấy tất cả tài liệu kèm tên danh mục
     */
    public List<Document> findAll() {
        List<Document> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm tài liệu theo từ khóa (title, author, isbn)
     */
    public List<Document> findByKeyword(String keyword) {
        List<Document> list = new ArrayList<>();
        String sql = """
                SELECT d.*, c.name AS category_name
                FROM documents d
                LEFT JOIN categories c ON d.category_id = c.id
                WHERE d.title LIKE ? OR d.author LIKE ? OR d.isbn LIKE ?
                ORDER BY d.id ASC
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lọc theo danh mục
     */
    public List<Document> findByCategoryId(int categoryId) {
        List<Document> list = new ArrayList<>();
        String sql = """
                SELECT d.*, c.name AS category_name
                FROM documents d
                LEFT JOIN categories c ON d.category_id = c.id
                WHERE d.category_id = ?
                ORDER BY d.id ASC
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Document findById(int id) {
        String sql = """
                SELECT d.*, c.name AS category_name
                FROM documents d
                LEFT JOIN categories c ON d.category_id = c.id
                WHERE d.id = ?
                """;
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

    public boolean save(Document doc) {
        String sql = """
                INSERT INTO documents (title, author, isbn, category_id, publisher, publish_year, unit_price, stock_quantity)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, doc.getTitle());
            stmt.setString(2, doc.getAuthor());
            stmt.setString(3, doc.getIsbn());
            stmt.setInt(4, doc.getCategoryId());
            stmt.setString(5, doc.getPublisher());
            stmt.setInt(6, doc.getPublishYear());
            stmt.setDouble(7, doc.getUnitPrice());
            stmt.setInt(8, doc.getStockQuantity());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) doc.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Document doc) {
        String sql = """
                UPDATE documents SET title=?, author=?, isbn=?, category_id=?,
                publisher=?, publish_year=?, unit_price=?, stock_quantity=?
                WHERE id=?
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, doc.getTitle());
            stmt.setString(2, doc.getAuthor());
            stmt.setString(3, doc.getIsbn());
            stmt.setInt(4, doc.getCategoryId());
            stmt.setString(5, doc.getPublisher());
            stmt.setInt(6, doc.getPublishYear());
            stmt.setDouble(7, doc.getUnitPrice());
            stmt.setInt(8, doc.getStockQuantity());
            stmt.setInt(9, doc.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM documents WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Tìm tài liệu theo ISBN (so khớp bỏ dấu gạch ngang và khoảng trắng)
     */
    public Document findByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) return null;
        String normalized = isbn.replaceAll("[\\s-]", "");
        String sql = """
                SELECT d.*, c.name AS category_name
                FROM documents d
                LEFT JOIN categories c ON d.category_id = c.id
                WHERE REPLACE(REPLACE(d.isbn, '-', ''), ' ', '') = ?
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, normalized);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật tồn kho (delta âm = xuất kho).
     * Điều kiện stock_quantity + delta >= 0 chặn xuất quá tồn ngay tại SQL.
     */
    public boolean updateStock(int docId, int addQuantity) {
        String sql = "UPDATE documents SET stock_quantity = stock_quantity + ? "
                + "WHERE id = ? AND stock_quantity + ? >= 0";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, addQuantity);
            stmt.setInt(2, docId);
            stmt.setInt(3, addQuantity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Đọc tồn kho thực tế trực tiếp từ DB (dùng sau khi updateStock).
     */
    public int getStockQuantity(int docId) {
        String sql = "SELECT stock_quantity FROM documents WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, docId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("stock_quantity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Tìm tài liệu có tồn kho thấp hơn ngưỡng
     */
    public List<Document> findLowStock(int threshold) {
        List<Document> list = new ArrayList<>();
        String sql = """
                SELECT d.*, c.name AS category_name
                FROM documents d
                LEFT JOIN categories c ON d.category_id = c.id
                WHERE d.stock_quantity <= ? AND d.stock_quantity >= 0
                ORDER BY d.stock_quantity ASC, d.title
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, threshold);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm kiếm nâng cao với nhiều tiêu chí.
     * Tham số null hoặc rỗng sẽ được bỏ qua.
     */
    public List<Document> advancedSearch(String title, String author, String isbn,
                                          String publisher, Integer yearFrom, Integer yearTo,
                                          Double priceFrom, Double priceTo,
                                          Integer stockFrom, Integer stockTo,
                                          Integer categoryId) {
        List<Document> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT d.*, c.name AS category_name
                FROM documents d
                LEFT JOIN categories c ON d.category_id = c.id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            sql.append(" AND d.title LIKE ?");
            params.add("%" + title.trim() + "%");
        }
        if (author != null && !author.isBlank()) {
            sql.append(" AND d.author LIKE ?");
            params.add("%" + author.trim() + "%");
        }
        if (isbn != null && !isbn.isBlank()) {
            sql.append(" AND d.isbn LIKE ?");
            params.add("%" + isbn.trim() + "%");
        }
        if (publisher != null && !publisher.isBlank()) {
            sql.append(" AND d.publisher LIKE ?");
            params.add("%" + publisher.trim() + "%");
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND d.category_id = ?");
            params.add(categoryId);
        }
        if (yearFrom != null) {
            sql.append(" AND d.publish_year >= ?");
            params.add(yearFrom);
        }
        if (yearTo != null) {
            sql.append(" AND d.publish_year <= ?");
            params.add(yearTo);
        }
        if (priceFrom != null) {
            sql.append(" AND d.unit_price >= ?");
            params.add(priceFrom);
        }
        if (priceTo != null) {
            sql.append(" AND d.unit_price <= ?");
            params.add(priceTo);
        }
        if (stockFrom != null) {
            sql.append(" AND d.stock_quantity >= ?");
            params.add(stockFrom);
        }
        if (stockTo != null) {
            sql.append(" AND d.stock_quantity <= ?");
            params.add(stockTo);
        }
        sql.append(" ORDER BY d.id ASC");

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String s) stmt.setString(i + 1, s);
                else if (p instanceof Integer n) stmt.setInt(i + 1, n);
                else if (p instanceof Double d) stmt.setDouble(i + 1, d);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Document mapRow(ResultSet rs) throws SQLException {
        Document doc = new Document();
        doc.setId(rs.getInt("id"));
        doc.setTitle(rs.getString("title"));
        doc.setAuthor(rs.getString("author"));
        doc.setIsbn(rs.getString("isbn"));
        doc.setCategoryId(rs.getInt("category_id"));
        try {
            doc.setCategoryName(rs.getString("category_name"));
        } catch (SQLException ignored) {}
        doc.setPublisher(rs.getString("publisher"));
        doc.setPublishYear(rs.getInt("publish_year"));
        doc.setUnitPrice(rs.getDouble("unit_price"));
        doc.setStockQuantity(rs.getInt("stock_quantity"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) doc.setCreatedAt(ts.toLocalDateTime());
        return doc;
    }
}
