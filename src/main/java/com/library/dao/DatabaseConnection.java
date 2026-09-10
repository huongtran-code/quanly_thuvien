package com.library.dao;

import com.library.util.PasswordUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;

/**
 * Quản lý kết nối JDBC đến MySQL.
 * Singleton pattern, tự động tạo database và tables nếu chưa tồn tại.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private static boolean migrated = false;
    private String url;
    private String username;
    private String password;

    private DatabaseConnection() {
        loadConfig();
        initDatabase();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
            // Migrations phải chạy SAU khi instance đã gán
            // để tránh vòng lặp khi DAO gọi getInstance()
            if (!migrated) {
                migrated = true;
                instance.runMigrations();
            }
        }
        return instance;
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("Không tìm thấy file db.properties trong classpath!");
            }
            Properties props = new Properties();
            props.load(input);
            this.url = props.getProperty("db.url");
            this.username = props.getProperty("db.username");
            this.password = props.getProperty("db.password");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc file cấu hình: " + e.getMessage(), e);
        }
    }

    /**
     * Tự động tạo database và tables từ schema.sql
     * Sử dụng đọc từng dòng và build câu SQL hoàn chỉnh (thay vì split bằng ";")
     */
    private void initDatabase() {
        String serverUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true"
                + "&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
        try (Connection conn = DriverManager.getConnection(serverUrl, username, password)) {
            Statement stmt = conn.createStatement();

            // Tạo database
            stmt.execute("CREATE DATABASE IF NOT EXISTS library_procurement "
                    + "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            stmt.execute("USE library_procurement");

            // Đọc schema.sql và thực thi từng câu SQL hoàn chỉnh
            InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("schema.sql");
            if (schemaStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(schemaStream));
                StringBuilder sqlBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    String trimmedLine = line.trim();

                    // Bỏ qua comment và dòng trống
                    if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                        continue;
                    }
                    // Bỏ qua CREATE DATABASE và USE (đã xử lý ở trên)
                    if (trimmedLine.toUpperCase().startsWith("CREATE DATABASE")
                            || trimmedLine.toUpperCase().startsWith("USE ")) {
                        continue;
                    }

                    sqlBuilder.append(line).append("\n");

                    // Khi gặp dấu ";" ở cuối dòng → thực thi câu SQL
                    if (trimmedLine.endsWith(";")) {
                        String sql = sqlBuilder.toString().trim();
                        // Bỏ dấu ; cuối
                        sql = sql.substring(0, sql.length() - 1).trim();
                        if (!sql.isEmpty()) {
                            try {
                                stmt.execute(sql);
                            } catch (SQLException e) {
                                if (!e.getMessage().contains("Duplicate")) {
                                    System.err.println("SQL Warning: " + e.getMessage());
                                    System.err.println("SQL: " + sql.substring(0, Math.min(sql.length(), 100)));
                                }
                            }
                        }
                        sqlBuilder.setLength(0); // Reset builder
                    }
                }
                reader.close();
                System.out.println("✓ Database schema initialized.");
            }

            // Seed default users
            seedDefaultUsers(conn);
            // Seed dữ liệu mẫu (chỉ khi bảng trống)
            seedDefaultData(conn);

            System.out.println("✓ Database initialized successfully.");

        } catch (Exception e) {
            throw new RuntimeException("Không thể kết nối MySQL! Kiểm tra:\n"
                    + "1. MySQL đã khởi động?\n"
                    + "2. Username/password đúng không? (file db.properties)\n"
                    + "Chi tiết: " + e.getMessage(), e);
        }
    }

    private void seedDefaultUsers(Connection conn) {
        try {
            if (getCount(conn, "users") > 0) return;
            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)");

            insert.setString(1, "admin");
            insert.setString(2, PasswordUtil.hash("admin123"));
            insert.setString(3, "Quản trị viên");
            insert.setString(4, "ADMIN");
            insert.executeUpdate();

            insert.setString(1, "librarian");
            insert.setString(2, PasswordUtil.hash("admin123"));
            insert.setString(3, "Thủ thư");
            insert.setString(4, "LIBRARIAN");
            insert.executeUpdate();

            System.out.println("✓ Default users created (admin/admin123, librarian/admin123)");
        } catch (SQLException e) {
            System.err.println("Warning: Could not seed default users: " + e.getMessage());
        }
    }

    private void seedDefaultData(Connection conn) {
        try {
            Statement stmt = conn.createStatement();

            // ── Danh mục ──
            if (getCount(conn, "categories") == 0) {
                stmt.executeUpdate("INSERT INTO categories (name, description) VALUES "
                        + "('Khoa học tự nhiên', 'Sách về vật lý, hóa học, sinh học, toán học'), "
                        + "('Công nghệ thông tin', 'Sách về lập trình, mạng máy tính, trí tuệ nhân tạo'), "
                        + "('Văn học', 'Tiểu thuyết, thơ, truyện ngắn trong và ngoài nước'), "
                        + "('Kinh tế', 'Sách về kinh tế, tài chính, quản trị doanh nghiệp'), "
                        + "('Ngoại ngữ', 'Sách học tiếng Anh, Pháp, Nhật, Hàn'), "
                        + "('Kỹ thuật - Công nghệ', 'Sách kỹ thuật cơ khí, điện, xây dựng, tự động hóa'), "
                        + "('Triết học - Xã hội', 'Triết học, tâm lý học, xã hội học, lịch sử'), "
                        + "('Y học - Sức khỏe', 'Sách y khoa, dinh dưỡng, chăm sóc sức khỏe')");
                System.out.println("✓ Default categories created");
            }

            // ── Nhà cung cấp ──
            if (getCount(conn, "suppliers") == 0) {
                stmt.executeUpdate("INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES "
                        + "('NXB Giáo Dục Việt Nam', 'Nguyễn Văn An', '0241234567', 'lienhe@nxbgd.vn', '81 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội'), "
                        + "('NXB Kim Đồng', 'Trần Thị Bình', '0287654321', 'info@nxbkimdong.vn', '55 Quang Trung, Hai Bà Trưng, Hà Nội'), "
                        + "('Fahasa', 'Lê Văn Cường', '0283823456', 'order@fahasa.com', '60-62 Lê Lợi, Q1, TP.HCM'), "
                        + "('NXB Khoa học và Kỹ thuật', 'Phạm Thị Dung', '0246789123', 'nxbkhkt@gmail.com', '70 Trần Hưng Đạo, Hà Nội'), "
                        + "('Nhà sách Tiki', 'Hoàng Văn Ế', '0286666888', 'b2b@tiki.vn', '52 Út Tịch, Tân Bình, TP.HCM'), "
                        + "('Alpha Books', 'Vũ Thị Giang', '0246543210', 'contact@alphabooks.vn', '39 Lý Quốc Sư, Hoàn Kiếm, Hà Nội')");
                System.out.println("✓ Default suppliers created");
            }

            // ── Tài liệu (20 cuốn mẫu) ──
            if (getCount(conn, "documents") == 0 && getCount(conn, "categories") > 0) {
                stmt.executeUpdate("INSERT INTO documents (title, author, isbn, category_id, publisher, publish_year, unit_price, stock_quantity) VALUES "
                        // Khoa học tự nhiên
                        + "('Giải tích 1', 'Nguyễn Đình Trí', '978-604-0-1001', (SELECT id FROM categories WHERE name='Khoa học tự nhiên'), 'NXB Giáo Dục', 2020, 85000, 10), "
                        + "('Đại số tuyến tính', 'Trần Đức Long', '978-604-0-1005', (SELECT id FROM categories WHERE name='Khoa học tự nhiên'), 'NXB ĐHQG Hà Nội', 2021, 90000, 4), "
                        + "('Vật lý đại cương tập 1', 'Lương Duyên Bình', '978-604-0-1006', (SELECT id FROM categories WHERE name='Khoa học tự nhiên'), 'NXB Giáo Dục', 2019, 78000, 2), "
                        // Công nghệ thông tin
                        + "('Cấu trúc dữ liệu và giải thuật', 'Lê Minh Hoàng', '978-604-0-1002', (SELECT id FROM categories WHERE name='Công nghệ thông tin'), 'NXB ĐHQG', 2021, 120000, 5), "
                        + "('Nhập môn trí tuệ nhân tạo', 'Stuart Russell', '978-0-13-461099-3', (SELECT id FROM categories WHERE name='Công nghệ thông tin'), 'Pearson', 2022, 350000, 3), "
                        + "('Clean Code', 'Robert C. Martin', '978-0-13-235088-4', (SELECT id FROM categories WHERE name='Công nghệ thông tin'), 'Prentice Hall', 2008, 320000, 7), "
                        + "('Học Python trong 30 ngày', 'Nguyễn Hữu Phước', '978-604-0-2010', (SELECT id FROM categories WHERE name='Công nghệ thông tin'), 'NXB Thông Tin', 2023, 165000, 12), "
                        // Văn học
                        + "('Truyện Kiều', 'Nguyễn Du', '978-604-0-1003', (SELECT id FROM categories WHERE name='Văn học'), 'NXB Văn Học', 2019, 65000, 15), "
                        + "('Số đỏ', 'Vũ Trọng Phụng', '978-604-0-3001', (SELECT id FROM categories WHERE name='Văn học'), 'NXB Hội Nhà Văn', 2020, 55000, 8), "
                        + "('Nhà thờ Đức Bà Paris', 'Victor Hugo', '978-604-0-3002', (SELECT id FROM categories WHERE name='Văn học'), 'NXB Văn Học', 2018, 120000, 6), "
                        // Kinh tế
                        + "('Kinh tế vi mô', 'Phạm Văn Minh', '978-604-0-1004', (SELECT id FROM categories WHERE name='Kinh tế'), 'NXB Kinh tế', 2022, 95000, 8), "
                        + "('Quản trị học hiện đại', 'Nguyễn Thị Liên Diệp', '978-604-0-4001', (SELECT id FROM categories WHERE name='Kinh tế'), 'NXB Lao Động', 2021, 110000, 5), "
                        + "('Tư duy nhanh và chậm', 'Daniel Kahneman', '978-604-0-4002', (SELECT id FROM categories WHERE name='Kinh tế'), 'Alpha Books', 2022, 185000, 9), "
                        // Ngoại ngữ
                        + "('English Grammar in Use', 'Raymond Murphy', '978-1-108-45774-3', (SELECT id FROM categories WHERE name='Ngoại ngữ'), 'Cambridge University Press', 2023, 250000, 3), "
                        + "('Toeic 900 - Chinh phục mọi câu hỏi', 'Kim Daegyeom', '978-604-0-5001', (SELECT id FROM categories WHERE name='Ngoại ngữ'), 'NXB Tổng Hợp TP.HCM', 2022, 195000, 11), "
                        // Kỹ thuật
                        + "('Kỹ thuật điện tử số', 'Nguyễn Hữu Phương', '978-604-0-6001', (SELECT id FROM categories WHERE name='Kỹ thuật - Công nghệ'), 'NXB Khoa học Kỹ thuật', 2020, 130000, 6), "
                        + "('Cơ học kết cấu', 'Lều Thọ Trình', '978-604-0-6002', (SELECT id FROM categories WHERE name='Kỹ thuật - Công nghệ'), 'NXB Giáo Dục', 2018, 115000, 4), "
                        // Triết học
                        + "('Đắc nhân tâm', 'Dale Carnegie', '978-604-0-7001', (SELECT id FROM categories WHERE name='Triết học - Xã hội'), 'NXB Tổng Hợp TP.HCM', 2021, 88000, 20), "
                        + "('Tâm lý học đám đông', 'Gustave Le Bon', '978-604-0-7002', (SELECT id FROM categories WHERE name='Triết học - Xã hội'), 'NXB Tri Thức', 2020, 75000, 7), "
                        // Y học
                        + "('Giải phẫu học người', 'Frank H. Netter', '978-0-323-39297-5', (SELECT id FROM categories WHERE name='Y học - Sức khỏe'), 'Elsevier', 2019, 750000, 2)");
                System.out.println("✓ Default documents created (20 books)");
            }

            // ── Ngân sách ──
            if (getCount(conn, "budgets") == 0) {
                stmt.executeUpdate("INSERT INTO budgets (fiscal_year, budget_name, total_amount, spent_amount, status) VALUES "
                        + "(2025, 'Ngân sách mua sách năm 2025', 120000000, 95600000, 'CLOSED'), "
                        + "(2026, 'Ngân sách Quý 1/2026 - KHTN & CNTT', 45000000, 32500000, 'ACTIVE'), "
                        + "(2026, 'Ngân sách Quý 2/2026 - Văn học & Ngoại ngữ', 30000000, 18750000, 'ACTIVE'), "
                        + "(2026, 'Ngân sách Quý 3/2026 - Tổng hợp', 60000000, 8200000, 'ACTIVE'), "
                        + "(2026, 'Ngân sách dự phòng 2026', 20000000, 0, 'ACTIVE')");
                System.out.println("✓ Default budgets created");
            }

            // ── Đơn mua hàng & Chi tiết ──
            if (getCount(conn, "purchase_orders") == 0
                    && getCount(conn, "suppliers") > 0
                    && getCount(conn, "budgets") > 0
                    && getCount(conn, "documents") > 0) {
                // Lấy IDs cần dùng
                int adminId = getIdByColumn(conn, "users", "username", "admin");
                int sup1 = getIdByColumn(conn, "suppliers", "name", "NXB Giáo Dục Việt Nam");
                int sup2 = getIdByColumn(conn, "suppliers", "name", "Fahasa");
                int sup3 = getIdByColumn(conn, "suppliers", "name", "Alpha Books");
                int bud1 = getFirstId(conn, "budgets", "fiscal_year = 2026 AND status='ACTIVE' LIMIT 1");
                int doc1 = getFirstId(conn, "documents", "1=1 LIMIT 1 OFFSET 0"); // Giải tích 1
                int doc2 = getFirstId(conn, "documents", "1=1 LIMIT 1 OFFSET 3"); // CTDL
                int doc3 = getFirstId(conn, "documents", "1=1 LIMIT 1 OFFSET 7"); // Truyện Kiều
                int doc4 = getFirstId(conn, "documents", "1=1 LIMIT 1 OFFSET 13"); // Toeic

                if (sup1 > 0 && bud1 > 0 && doc1 > 0) {
                    // Đơn 1: RECEIVED
                    stmt.executeUpdate(
                        "INSERT INTO purchase_orders (order_code, supplier_id, budget_id, order_date, total_amount, status, notes, invoice_number, invoice_date, created_by) VALUES "
                        + "('PO-2026-001', " + sup1 + ", " + bud1 + ", '2026-07-15 09:00:00', 2550000, 'RECEIVED', "
                        + "'Mua sách khoa học tự nhiên đầu năm học', 'INV-2026-001', '2026-07-20 00:00:00', " + adminId + ")");
                    int po1 = getFirstId(conn, "purchase_orders", "order_code='PO-2026-001'");

                    // Đơn 2: APPROVED
                    stmt.executeUpdate(
                        "INSERT INTO purchase_orders (order_code, supplier_id, budget_id, order_date, total_amount, status, notes, created_by) VALUES "
                        + "('PO-2026-002', " + sup2 + ", " + bud1 + ", '2026-08-01 10:30:00', 5850000, 'APPROVED', "
                        + "'Đặt sách văn học và ngoại ngữ cho học kỳ 2', " + adminId + ")");
                    int po2 = getFirstId(conn, "purchase_orders", "order_code='PO-2026-002'");

                    // Đơn 3: DRAFT
                    stmt.executeUpdate(
                        "INSERT INTO purchase_orders (order_code, supplier_id, budget_id, order_date, total_amount, status, notes, created_by) VALUES "
                        + "('PO-2026-003', " + sup3 + ", " + bud1 + ", '2026-08-25 14:00:00', 1760000, 'DRAFT', "
                        + "'Mua sách kinh tế và tư duy', " + adminId + ")");
                    int po3 = getFirstId(conn, "purchase_orders", "order_code='PO-2026-003'");

                    // Chi tiết đơn 1
                    if (po1 > 0 && doc1 > 0 && doc2 > 0) {
                        stmt.executeUpdate("INSERT INTO purchase_order_items (order_id, document_id, quantity, unit_price) VALUES "
                                + "(" + po1 + ", " + doc1 + ", 15, 85000), "
                                + "(" + po1 + ", " + doc2 + ", 10, 120000)");
                    }
                    // Chi tiết đơn 2
                    if (po2 > 0 && doc3 > 0 && doc4 > 0) {
                        stmt.executeUpdate("INSERT INTO purchase_order_items (order_id, document_id, quantity, unit_price) VALUES "
                                + "(" + po2 + ", " + doc3 + ", 30, 65000), "
                                + "(" + po2 + ", " + doc4 + ", 20, 195000)");
                    }
                    // Chi tiết đơn 3
                    if (po3 > 0) {
                        int doc5 = getFirstId(conn, "documents", "1=1 LIMIT 1 OFFSET 11"); // Quản trị học
                        int doc6 = getFirstId(conn, "documents", "1=1 LIMIT 1 OFFSET 12"); // Tư duy nhanh chậm
                        if (doc5 > 0 && doc6 > 0) {
                            stmt.executeUpdate("INSERT INTO purchase_order_items (order_id, document_id, quantity, unit_price) VALUES "
                                    + "(" + po3 + ", " + doc5 + ", 8, 110000), "
                                    + "(" + po3 + ", " + doc6 + ", 4, 185000)");
                        }
                    }
                    System.out.println("✓ Default purchase orders created");
                }
            }

            // ── Đề xuất mua sách ──
            if (getCount(conn, "purchase_suggestions") == 0) {
                int adminId = getIdByColumn(conn, "users", "username", "admin");
                stmt.executeUpdate("INSERT INTO purchase_suggestions "
                        + "(book_title, author, isbn, publisher, quantity, reason, suggested_by, status, review_note, reviewed_by, reviewed_at) VALUES "
                        + "('Design Patterns: Elements of Reusable Object-Oriented Software', 'Gang of Four', '978-0-201-63361-0', 'Addison-Wesley', 5, "
                        + "'Sách kinh điển về design patterns, rất cần thiết cho sinh viên CNTT năm 3-4', 'GV. Nguyễn Văn Bình', 'APPROVED', "
                        + "'Đồng ý, sẽ đặt mua trong đơn tiếp theo', " + adminId + ", '2026-08-01 09:00:00'), "

                        + "('The Pragmatic Programmer', 'David Thomas', '978-0-13-595705-9', 'Addison-Wesley', 3, "
                        + "'Sách tham khảo kỹ năng lập trình thực chiến cho sinh viên', 'SV. Trần Thị Cẩm', 'PENDING', "
                        + "NULL, NULL, NULL), "

                        + "('Atomic Habits', 'James Clear', '978-0-7352-1129-2', 'Avery', 10, "
                        + "'Sách phát triển bản thân rất được yêu thích, phù hợp cho tủ sách tham khảo', 'GV. Lê Minh Đức', 'APPROVED', "
                        + "'Phê duyệt, số lượng hợp lý', " + adminId + ", '2026-07-15 14:30:00'), "

                        + "('Sapiens: Lược sử loài người', 'Yuval Noah Harari', '978-604-0-8001', 'NXB Tri Thức', 8, "
                        + "'Sách lịch sử nhân loại được đánh giá cao, nên bổ sung vào thư viện', 'SV. Phạm Quang Ế', 'PENDING', "
                        + "NULL, NULL, NULL), "

                        + "('Bác sĩ Zhivago', 'Boris Pasternak', '978-604-0-3010', 'NXB Văn Học', 4, "
                        + "'Kiệt tác văn học Nga, sinh viên năm 2 ngành Văn học rất cần', 'GV. Hoàng Thị Giang', 'REJECTED', "
                        + "'Hiện có sẵn 6 cuốn trong kho, chưa cần mua thêm', " + adminId + ", '2026-08-10 11:00:00'), "

                        + "('Giáo trình Hóa học đại cương', 'Nguyễn Đình Huề', '978-604-0-1020', 'NXB Giáo Dục', 20, "
                        + "'Tài liệu giảng dạy bắt buộc cho sinh viên năm nhất khối tự nhiên', 'GV. Trần Văn Hải', 'PENDING', "
                        + "NULL, NULL, NULL)");
                System.out.println("✓ Default purchase suggestions created");
            }

        } catch (SQLException e) {
            System.err.println("Warning: Could not seed default data: " + e.getMessage());
        }
    }

    private int getIdByColumn(Connection conn, String table, String col, String val) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM " + table + " WHERE " + col + " = ? LIMIT 1");
            ps.setString(1, val);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { /* ignore */ }
        return -1;
    }

    private int getFirstId(Connection conn, String table, String where) {
        try {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT id FROM " + table + " WHERE " + where);
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { /* ignore */ }
        return -1;
    }

    private int getCount(Connection conn, String tableName) throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + tableName);
        rs.next();
        return rs.getInt(1);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Chạy các migration cho DB đã tồn tại
     */
    private void runMigrations() {
        // Migration 1: Invoice columns
        new PurchaseOrderDAO().migrateInvoiceColumns();
        // Migration 2: Purchase suggestions table
        new PurchaseSuggestionDAO().createTableIfNotExists();
    }
}
