package com.library.dao;

import com.library.model.PurchaseOrder;
import com.library.model.PurchaseOrderItem;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO: Thao tác CRUD bảng purchase_orders và purchase_order_items
 */
public class PurchaseOrderDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String SELECT_ALL = """
            SELECT po.*, s.name AS supplier_name, b.budget_name, u.full_name AS created_by_name
            FROM purchase_orders po
            LEFT JOIN suppliers s ON po.supplier_id = s.id
            LEFT JOIN budgets b ON po.budget_id = b.id
            LEFT JOIN users u ON po.created_by = u.id
            ORDER BY po.created_at DESC
            """;

    /**
     * Lấy tất cả đơn mua kèm tên NCC, ngân sách, người tạo
     */
    public List<PurchaseOrder> findAll() {
        List<PurchaseOrder> list = new ArrayList<>();
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
     * Lấy đơn mua theo ID kèm chi tiết items
     */
    public PurchaseOrder findById(int id) {
        String sql = """
                SELECT po.*, s.name AS supplier_name, b.budget_name, u.full_name AS created_by_name
                FROM purchase_orders po
                LEFT JOIN suppliers s ON po.supplier_id = s.id
                LEFT JOIN budgets b ON po.budget_id = b.id
                LEFT JOIN users u ON po.created_by = u.id
                WHERE po.id = ?
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PurchaseOrder order = mapRow(rs);
                order.setItems(findItemsByOrderId(id));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách items của đơn mua
     */
    public List<PurchaseOrderItem> findItemsByOrderId(int orderId) {
        List<PurchaseOrderItem> items = new ArrayList<>();
        String sql = """
                SELECT poi.*, d.title AS document_title
                FROM purchase_order_items poi
                LEFT JOIN documents d ON poi.document_id = d.id
                WHERE poi.order_id = ?
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setDocumentId(rs.getInt("document_id"));
                item.setDocumentTitle(rs.getString("document_title"));
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
     * Tạo đơn mua mới kèm items (transaction)
     */
    public boolean save(PurchaseOrder order) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            conn.setAutoCommit(false);

            // Sinh mã đơn
            order.setOrderCode(generateOrderCode(conn));

            // Insert đơn mua
            String sqlOrder = """
                    INSERT INTO purchase_orders (order_code, supplier_id, budget_id, total_amount, status, notes, created_by)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
            PreparedStatement stmtOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            stmtOrder.setString(1, order.getOrderCode());
            stmtOrder.setInt(2, order.getSupplierId());
            stmtOrder.setInt(3, order.getBudgetId());
            stmtOrder.setDouble(4, order.getTotalAmount());
            stmtOrder.setString(5, order.getStatus());
            stmtOrder.setString(6, order.getNotes());
            stmtOrder.setInt(7, order.getCreatedBy());
            stmtOrder.executeUpdate();

            ResultSet keys = stmtOrder.getGeneratedKeys();
            if (keys.next()) {
                order.setId(keys.getInt(1));
            }

            // Insert từng item
            String sqlItem = "INSERT INTO purchase_order_items (order_id, document_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            PreparedStatement stmtItem = conn.prepareStatement(sqlItem);
            for (PurchaseOrderItem item : order.getItems()) {
                stmtItem.setInt(1, order.getId());
                stmtItem.setInt(2, item.getDocumentId());
                stmtItem.setInt(3, item.getQuantity());
                stmtItem.setDouble(4, item.getUnitPrice());
                stmtItem.addBatch();
            }
            stmtItem.executeBatch();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Cập nhật trạng thái đơn mua
     */
    public boolean updateStatus(int orderId, String newStatus) {
        String sql = "UPDATE purchase_orders SET status = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sinh mã đơn mua: PO-YYYYMMDD-XXX
     */
    private String generateOrderCode(Connection conn) throws SQLException {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PO-" + today + "-";
        String sql = "SELECT COUNT(*) FROM purchase_orders WHERE order_code LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, prefix + "%");
        ResultSet rs = stmt.executeQuery();
        int count = 0;
        if (rs.next()) count = rs.getInt(1);
        return prefix + String.format("%03d", count + 1);
    }

    // ── Thống kê cho Báo cáo ──

    /**
     * Thống kê chi tiêu theo tháng trong năm
     */
    public Map<Integer, Double> getMonthlySpending(int year) {
        Map<Integer, Double> data = new HashMap<>();
        String sql = """
                SELECT MONTH(order_date) AS month, SUM(total_amount) AS total
                FROM purchase_orders
                WHERE YEAR(order_date) = ? AND status IN ('APPROVED', 'RECEIVED')
                GROUP BY MONTH(order_date)
                ORDER BY month
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                data.put(rs.getInt("month"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Thống kê chi tiêu theo nhà cung cấp
     */
    public Map<String, Double> getSpendingBySupplier(int year) {
        Map<String, Double> data = new HashMap<>();
        String sql = """
                SELECT s.name, SUM(po.total_amount) AS total
                FROM purchase_orders po
                JOIN suppliers s ON po.supplier_id = s.id
                WHERE YEAR(po.order_date) = ? AND po.status IN ('APPROVED', 'RECEIVED')
                GROUP BY s.name
                ORDER BY total DESC
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("name"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Thống kê chi tiêu theo danh mục
     */
    public Map<String, Double> getSpendingByCategory(int year) {
        Map<String, Double> data = new HashMap<>();
        String sql = """
                SELECT c.name, SUM(poi.subtotal) AS total
                FROM purchase_order_items poi
                JOIN purchase_orders po ON poi.order_id = po.id
                JOIN documents d ON poi.document_id = d.id
                LEFT JOIN categories c ON d.category_id = c.id
                WHERE YEAR(po.order_date) = ? AND po.status IN ('APPROVED', 'RECEIVED')
                GROUP BY c.name
                ORDER BY total DESC
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                data.put(name != null ? name : "Chưa phân loại", rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Tổng đơn hàng theo trạng thái
     */
    public Map<String, Integer> getOrderCountByStatus() {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT status, COUNT(*) AS cnt FROM purchase_orders GROUP BY status";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Tìm đơn mua đang chờ (DRAFT/APPROVED) có chứa tài liệu này.
     * Dùng để cảnh báo tra trùng khi đặt mua.
     */
    public List<PurchaseOrder> findPendingOrdersForDocument(int documentId) {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = """
                SELECT DISTINCT po.*, s.name AS supplier_name, b.budget_name, u.full_name AS created_by_name
                FROM purchase_orders po
                JOIN purchase_order_items poi ON poi.order_id = po.id
                LEFT JOIN suppliers s ON po.supplier_id = s.id
                LEFT JOIN budgets b ON po.budget_id = b.id
                LEFT JOIN users u ON po.created_by = u.id
                WHERE poi.document_id = ? AND po.status IN ('DRAFT', 'APPROVED')
                ORDER BY po.created_at DESC
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, documentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private PurchaseOrder mapRow(ResultSet rs) throws SQLException {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(rs.getInt("id"));
        po.setOrderCode(rs.getString("order_code"));
        po.setSupplierId(rs.getInt("supplier_id"));
        try { po.setSupplierName(rs.getString("supplier_name")); } catch (SQLException ignored) {}
        po.setBudgetId(rs.getInt("budget_id"));
        try { po.setBudgetName(rs.getString("budget_name")); } catch (SQLException ignored) {}
        Timestamp od = rs.getTimestamp("order_date");
        if (od != null) po.setOrderDate(od.toLocalDateTime());
        po.setTotalAmount(rs.getDouble("total_amount"));
        po.setStatus(rs.getString("status"));
        po.setNotes(rs.getString("notes"));
        po.setCreatedBy(rs.getInt("created_by"));
        try { po.setCreatedByName(rs.getString("created_by_name")); } catch (SQLException ignored) {}
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) po.setCreatedAt(ca.toLocalDateTime());

        // Invoice fields
        try {
            po.setInvoiceNumber(rs.getString("invoice_number"));
            Timestamp id = rs.getTimestamp("invoice_date");
            if (id != null) po.setInvoiceDate(id.toLocalDateTime());
        } catch (SQLException ignored) {}

        return po;
    }

    /**
     * Cập nhật thông tin hóa đơn NCC
     */
    public boolean updateInvoice(int orderId, String invoiceNumber, LocalDateTime invoiceDate) {
        String sql = "UPDATE purchase_orders SET invoice_number = ?, invoice_date = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, invoiceNumber);
            if (invoiceDate != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(invoiceDate));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            stmt.setInt(3, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Thêm cột invoice nếu chưa có (migration)
     */
    public void migrateInvoiceColumns() {
        String checkSql = """
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'purchase_orders'
                AND COLUMN_NAME = 'invoice_number'
                """;
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("ALTER TABLE purchase_orders ADD COLUMN invoice_number VARCHAR(50) AFTER notes");
                stmt.execute("ALTER TABLE purchase_orders ADD COLUMN invoice_date DATETIME AFTER invoice_number");
                System.out.println("✓ Migrated: invoice columns added to purchase_orders");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

