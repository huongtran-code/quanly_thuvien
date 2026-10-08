package com.library.util;

import com.library.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Script Java tự động sinh 1.000 - 5.000 bản ghi dữ liệu mẫu qua 5 năm (2021 - 2026)
 * bao gồm: documents, purchase_orders, stock_transactions, purchase_suggestions,
 * customers, borrows, fines, và sales (doanh thu bán sách mỗi tháng liên tục)
 * chạy siêu tốc với JDBC Batch Processing trong < 1 giây!
 */
public class LargeDataSeeder {

    private static final Random random = new Random();

    // Mẫu từ vựng tạo tên sách tự động
    private static final String[] SUBJECTS = {
            "Giải tích", "Đại số tuyến tính", "Lập trình Java", "Cấu trúc dữ liệu",
            "Trí tuệ nhân tạo", "Hệ quản trị CSDL", "Kinh tế vi mô", "Quản trị doanh nghiệp",
            "Tiếng Anh giao tiếp", "Vật lý đại cương", "Triết học Mác - Lênin", "Tâm lý học",
            "Thiết kế Web", "An toàn thông tin", "Học máy chuyên sâu", "Kỹ thuật phần mềm",
            "Văn học hiện đại", "Số hóa doanh nghiệp", "Kế toán tài chính", "Marketing căn bản"
    };

    private static final String[] QUALIFIERS = {
            "Nâng cao", "Căn bản", "Ứng dụng thực tế", "Tập 1", "Tập 2", "Từ lý thuyết đến thực hành",
            "Dành cho kỹ sư", "Toàn tập", "Chuyên sâu", "Hướng dẫn tự học", "Giáo trình chuẩn"
    };

    private static final String[] AUTHORS = {
            "Nguyễn Văn An", "Trần Thị Bình", "Lê Văn Cường", "Phạm Thị Dung", "Hoàng Văn Ế",
            "Vũ Thị Giang", "Đặng Văn Hùng", "Bùi Thị Yến", "Robert C. Martin", "Andrew Ng",
            "Donald Knuth", "Guido van Rossum", "James Gosling", "Phạm Văn Minh", "Nguyễn Du"
    };

    private static final String[] PUBLISHERS = {
            "NXB Giáo Dục Việt Nam", "NXB ĐHQG Hà Nội", "NXB Kim Đồng", "NXB Khoa học và Kỹ thuật",
            "NXB Thông Tin và Truyền Thông", "Alpha Books", "Pearson", "Addison-Wesley", "Cambridge University Press"
    };

    private static final String[] REASONS = {
            "Tài liệu giảng dạy cho môn học mới", "Sách tham khảo quan trọng cho sinh viên đồ án",
            "Bổ sung tủ sách chuyên ngành CNTT", "Độc giả đề xuất mượn nhiều trong kỳ",
            "Cập nhật kiến thức công nghệ mới", "Tài liệu phục vụ nghiên cứu khoa học"
    };

    private static final String[] PERSON_NAMES = {
            "GV. Nguyễn Văn Bình", "GV. Trần Thị Cẩm", "SV. Lê Minh Đức", "SV. Phạm Quang Anh",
            "GV. Hoàng Văn Nam", "SV. Vũ Thị Lan", "GV. Đặng Quốc Huy", "SV. Bùi Thị Mai"
    };

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("🚀 BẮT ĐẦU SINH 1.000 - 5.000 BẢN GHI DỮ LIỆU MẪU (5 NĂM)");
        System.out.println("=================================================");

        long startTime = System.currentTimeMillis();

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false); // Tắt autocommit để batch insert siêu tốc

            // 0. Dọn dẹp dữ liệu cũ trước khi seed lại
            cleanupSeededData(conn);
            System.out.println("✓ Da don dep du lieu cu");

            // 1. Kiểm tra / Tạo Ngân sách cho 5 năm (2021 - 2026)
            List<Integer> budgetIds = seedBudgets(conn);
            System.out.println("✓ Da tao Ngan sach cho 5 nam (2021-2026)");

            // 2. Lấy danh sách Category & Supplier IDs
            List<Integer> categoryIds = getIds(conn, "categories");
            List<Integer> supplierIds = getIds(conn, "suppliers");
            int adminId = getAdminUserId(conn);

            // 3. Sinh 1.500 Tài liệu / Sách (documents)
            List<Integer> documentIds = seedDocuments(conn, categoryIds, 1500);
            System.out.println("✓ Da sinh 1.500 bản ghi Tai lieu / Sach (documents)");

            // 4. Sinh 1.000 Đơn mua hàng (purchase_orders) trải dài 5 năm (2021-2026)
            List<Integer> orderIds = seedPurchaseOrders(conn, supplierIds, budgetIds, adminId, 1000);
            System.out.println("✓ Da sinh 1.000 bản ghi Don mua hang (purchase_orders)");

            // 5. Sinh 2.500 Chi tiết đơn mua (purchase_order_items)
            seedPurchaseOrderItems(conn, orderIds, documentIds, 2500);
            System.out.println("✓ Da sinh 2.500 bản ghi Chi tiet don mua (purchase_order_items)");

            // 6. Sinh 1.200 Giao dịch kho (stock_transactions) trải dài 5 năm
            seedStockTransactions(conn, documentIds, adminId, 1200);
            System.out.println("✓ Da sinh 1.200 bản ghi Giao dich kho (stock_transactions)");

            // 7. Sinh 1.000 Đề xuất mua sách (purchase_suggestions)
            seedPurchaseSuggestions(conn, adminId, 1000);
            System.out.println("✓ Da sinh 1.000 bản ghi De xuat mua sach (purchase_suggestions)");

            // 8. Sinh dữ liệu khách hàng, mượn sách, và phạt
            List<Integer> customerIds = seedCustomers(conn, 100);
            System.out.println("✓ Da sinh 100 bản ghi Khach hang (customers)");
            
            List<Integer> borrowIds = seedBorrows(conn, customerIds, documentIds, 200);
            System.out.println("✓ Da sinh 200 bản ghi Muon sach (borrows)");
            
            seedFines(conn, customerIds, borrowIds, 50);
            System.out.println("✓ Da sinh 50 bản ghi Phat (fines)");

            // 9. Sinh dữ liệu bán sách (sales) - mỗi tháng từ 01/2021 đến 10/2026
            seedSales(conn, customerIds, documentIds, adminId);
            System.out.println("✓ Da sinh du lieu Don ban sach (sales) tu 01/2021 den 10/2026");

            conn.commit(); // Commit toàn bộ dữ liệu
            conn.setAutoCommit(true);

            long endTime = System.currentTimeMillis();
            System.out.println("=================================================");
            System.out.println("🎉 HOÀN THÀNH SINH DỮ LIỆU THÀNH CÔNG NGHỆ BATCH INSERTS!");
            System.out.println("⏱️ Tổng thời gian thực thi: " + (endTime - startTime) + " ms (~" + String.format("%.2f", (endTime - startTime) / 1000.0) + " giây)");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("❌ Lỗi sinh dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Integer> seedBudgets(Connection conn) throws Exception {
        List<Integer> ids = new ArrayList<>();
        String sql = "INSERT INTO budgets (fiscal_year, budget_name, total_amount, spent_amount, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int year = 2021; year <= 2026; year++) {
                String status = (year < 2026) ? "CLOSED" : "ACTIVE";
                
                // 3 gói ngân sách mỗi năm
                for (int q = 1; q <= 3; q++) {
                    double total = 50_000_000 + random.nextInt(100) * 1_000_000.0;
                    double spent = (year < 2026) ? total * (0.85 + random.nextDouble() * 0.14) : total * 0.4;

                    ps.setInt(1, year);
                    ps.setString(2, "Ngân sách Quý " + q + "/" + year + " - Mua sắm tài liệu");
                    ps.setDouble(3, total);
                    ps.setDouble(4, spent);
                    ps.setString(5, status);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        if (ids.isEmpty()) ids = getIds(conn, "budgets");
        return ids;
    }

    private static List<Integer> seedDocuments(Connection conn, List<Integer> categoryIds, int count) throws Exception {
        List<Integer> ids = new ArrayList<>();
        String sql = "INSERT INTO documents (title, author, isbn, category_id, publisher, publish_year, unit_price, stock_quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 1; i <= count; i++) {
                String subject = SUBJECTS[random.nextInt(SUBJECTS.length)];
                String qual = QUALIFIERS[random.nextInt(QUALIFIERS.length)];
                String title = subject + " " + qual + " #" + i;
                String author = AUTHORS[random.nextInt(AUTHORS.length)];
                String isbn = String.format("978-604-%04d-%03d", random.nextInt(9000) + 1000, i % 1000);
                int catId = categoryIds.get(random.nextInt(categoryIds.size()));
                String pub = PUBLISHERS[random.nextInt(PUBLISHERS.length)];
                int year = 2018 + random.nextInt(9); // 2018-2026
                double price = (50 + random.nextInt(400)) * 1000.0;
                int stock = random.nextInt(50) + 2;

                ps.setString(1, title);
                ps.setString(2, author);
                ps.setString(3, isbn);
                ps.setInt(4, catId);
                ps.setString(5, pub);
                ps.setInt(6, year);
                ps.setDouble(7, price);
                ps.setInt(8, stock);
                ps.addBatch();
            }
            ps.executeBatch();

            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        if (ids.isEmpty()) ids = getIds(conn, "documents");
        return ids;
    }

    private static List<Integer> seedPurchaseOrders(Connection conn, List<Integer> supplierIds, List<Integer> budgetIds, int adminId, int count) throws Exception {
        List<Integer> ids = new ArrayList<>();
        String sql = "INSERT INTO purchase_orders (order_code, supplier_id, budget_id, order_date, total_amount, status, notes, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String[] statuses = {"DRAFT", "APPROVED", "RECEIVED", "CANCELLED"};

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 1; i <= count; i++) {
                String orderCode = String.format("PO-%04d-%04d", 2021 + (i % 6), i);
                int supId = supplierIds.get(random.nextInt(supplierIds.size()));
                int budId = budgetIds.get(random.nextInt(budgetIds.size()));
                
                // Ngày ngẫu nhiên từ 2021 đến 2026
                LocalDateTime orderDate = randomDateTime(2021, 2026);
                double total = (1_000 + random.nextInt(45_000)) * 1_000.0;
                String status = statuses[random.nextInt(statuses.length)];
                String notes = "Đơn nhập tài liệu đợt " + i + " năm " + orderDate.getYear();

                ps.setString(1, orderCode);
                ps.setInt(2, supId);
                ps.setInt(3, budId);
                ps.setString(4, orderDate.toString().replace("T", " "));
                ps.setDouble(5, total);
                ps.setString(6, status);
                ps.setString(7, notes);
                ps.setInt(8, adminId);
                ps.addBatch();
            }
            ps.executeBatch();

            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        if (ids.isEmpty()) ids = getIds(conn, "purchase_orders");
        return ids;
    }

    private static void seedPurchaseOrderItems(Connection conn, List<Integer> orderIds, List<Integer> docIds, int count) throws Exception {
        String sql = "INSERT INTO purchase_order_items (order_id, document_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < count; i++) {
                int orderId = orderIds.get(random.nextInt(orderIds.size()));
                int docId = docIds.get(random.nextInt(docIds.size()));
                int qty = random.nextInt(30) + 1;
                double price = (60 + random.nextInt(300)) * 1000.0;

                ps.setInt(1, orderId);
                ps.setInt(2, docId);
                ps.setInt(3, qty);
                ps.setDouble(4, price);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedStockTransactions(Connection conn, List<Integer> docIds, int adminId, int count) throws Exception {
        String sql = "INSERT INTO stock_transactions (document_id, change_qty, type, source, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        String[] types = {"IMPORT", "EXPORT"};
        String[] sources = {"SCANNER", "PURCHASE_ORDER", "MANUAL"};

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < count; i++) {
                int docId = docIds.get(random.nextInt(docIds.size()));
                int qty = random.nextInt(20) + 1;
                String type = types[random.nextInt(types.length)];
                String source = sources[random.nextInt(sources.length)];
                LocalDateTime dt = randomDateTime(2021, 2026);

                ps.setInt(1, docId);
                ps.setInt(2, qty);
                ps.setString(3, type);
                ps.setString(4, source);
                ps.setInt(5, adminId);
                ps.setString(6, dt.toString().replace("T", " "));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedPurchaseSuggestions(Connection conn, int adminId, int count) throws Exception {
        String sql = "INSERT INTO purchase_suggestions (book_title, author, isbn, publisher, quantity, reason, suggested_by, status, review_note, reviewed_by, reviewed_at, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String[] statuses = {"PENDING", "APPROVED", "REJECTED"};

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= count; i++) {
                String subject = SUBJECTS[random.nextInt(SUBJECTS.length)];
                String title = subject + " Tham khảo nâng cao #" + i;
                String author = AUTHORS[random.nextInt(AUTHORS.length)];
                String isbn = String.format("978-604-%04d-%03d", random.nextInt(9000) + 1000, i % 1000);
                String pub = PUBLISHERS[random.nextInt(PUBLISHERS.length)];
                int qty = random.nextInt(10) + 1;
                String reason = REASONS[random.nextInt(REASONS.length)];
                String suggestedBy = PERSON_NAMES[random.nextInt(PERSON_NAMES.length)];
                String status = statuses[random.nextInt(statuses.length)];
                
                LocalDateTime createdAt = randomDateTime(2021, 2026);
                String reviewNote = "PENDING".equals(status) ? null : ("APPROVED".equals(status) ? "Đồng ý đặt mua" : "Đã có đủ trong kho");
                Integer reviewedBy = "PENDING".equals(status) ? null : adminId;
                String reviewedAt = "PENDING".equals(status) ? null : createdAt.plusDays(2).toString().replace("T", " ");

                ps.setString(1, title);
                ps.setString(2, author);
                ps.setString(3, isbn);
                ps.setString(4, pub);
                ps.setInt(5, qty);
                ps.setString(6, reason);
                ps.setString(7, suggestedBy);
                ps.setString(8, status);
                ps.setString(9, reviewNote);
                if (reviewedBy != null) ps.setInt(10, reviewedBy); else ps.setNull(10, java.sql.Types.INTEGER);
                ps.setString(11, reviewedAt);
                ps.setString(12, createdAt.toString().replace("T", " "));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static List<Integer> getIds(Connection conn, String table) throws Exception {
        List<Integer> ids = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT id FROM " + table)) {
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    private static int getAdminUserId(Connection conn) {
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT id FROM users WHERE username='admin' LIMIT 1")) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return 1;
    }

    /**
     * Dọn dẹp dữ liệu do seeder sinh ra (giữ nguyên dữ liệu gốc từ schema.sql).
     * Tắt FK check tạm thời để TRUNCATE an toàn.
     */
    private static void cleanupSeededData(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            // Xoá toàn bộ bảng do seeder sinh (không ảnh hưởng bảng nằm trong schema.sql)
            st.execute("TRUNCATE TABLE sale_items");
            st.execute("TRUNCATE TABLE sales");
            st.execute("TRUNCATE TABLE fines");
            st.execute("TRUNCATE TABLE borrow_items");
            st.execute("TRUNCATE TABLE borrows");
            st.execute("TRUNCATE TABLE customers");
            st.execute("TRUNCATE TABLE stock_transactions");
            st.execute("TRUNCATE TABLE purchase_suggestions");
            st.execute("TRUNCATE TABLE purchase_order_items");
            // Chỉ xóa đơn mua do seeder tạo (format PO-YYYY-NNNN)
            st.execute("DELETE FROM purchase_orders WHERE order_code LIKE 'PO-%-%'");
            // Chỉ xóa ngân sách do seeder tạo (chứa 'Quý')
            st.execute("DELETE FROM budgets WHERE budget_name LIKE 'Ngân sách Quý%'");
            // Chỉ xóa documents do seeder tạo (ISBN format 978-604-XXXX)
            st.execute("DELETE FROM documents WHERE isbn LIKE '978-604-%'");
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private static LocalDateTime randomDateTime(int startYear, int endYear) {
        int year = startYear + random.nextInt(endYear - startYear + 1);
        // Nếu là năm hiện tại (endYear), chỉ sinh đến tháng hiện tại
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        int maxMonth = (year == endYear) ? currentMonth : 12;
        int month = random.nextInt(maxMonth) + 1;
        int day = random.nextInt(28) + 1;
        int hour = random.nextInt(12) + 8; // 8am to 8pm
        int min = random.nextInt(60);
        return LocalDateTime.of(year, month, day, hour, min);
    }

    private static List<Integer> seedCustomers(Connection conn, int count) throws Exception {
        List<Integer> ids = new ArrayList<>();
        String sql = "INSERT INTO customers (customer_code, full_name, email, phone, address, " +
                     "registration_date, status, total_borrowed, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 1; i <= count; i++) {
                String name = PERSON_NAMES[random.nextInt(PERSON_NAMES.length)] + " " + i;
                LocalDateTime createdAt = randomDateTime(2021, 2026);
                ps.setString(1, String.format("CUS%06d", i));                         // customer_code
                ps.setString(2, name);                                                  // full_name
                ps.setString(3, "customer" + i + "@example.com");                       // email
                ps.setString(4, "09" + String.format("%08d", random.nextInt(100000000))); // phone
                ps.setString(5, "Địa chỉ " + i + ", Hà Nội");                          // address
                ps.setObject(6, createdAt.toLocalDate());                               // registration_date
                ps.setString(7, "ACTIVE");                                              // status
                ps.setInt(8, random.nextInt(10));                                       // total_borrowed
                ps.setObject(9, createdAt);                                             // created_at
                ps.addBatch();
            }
            ps.executeBatch();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    private static List<Integer> seedBorrows(Connection conn, List<Integer> customerIds, List<Integer> documentIds, int count) throws Exception {
        List<Integer> ids = new ArrayList<>();
        String sql = "INSERT INTO borrows (borrow_code, customer_id, borrow_date, due_date, return_date, status, total_books, late_fee, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 1; i <= count; i++) {
                LocalDateTime borrowDate = randomDateTime(2021, 2026);
                LocalDateTime dueDate = borrowDate.plusDays(14);
                boolean isReturned = random.nextBoolean();
                LocalDateTime returnDate = isReturned ? borrowDate.plusDays(random.nextInt(20)) : null;
                String status = isReturned ? "RETURNED" : (LocalDateTime.now().isAfter(dueDate) ? "OVERDUE" : "BORROWED");
                double lateFee = status.equals("OVERDUE") ? random.nextInt(10) * 5000.0 : 0;
                
                ps.setString(1, "BRW" + String.format("%06d", i));
                ps.setInt(2, customerIds.get(random.nextInt(customerIds.size())));
                ps.setObject(3, borrowDate);
                ps.setObject(4, dueDate);
                ps.setObject(5, returnDate);
                ps.setString(6, status);
                ps.setInt(7, random.nextInt(5) + 1);
                ps.setDouble(8, lateFee);
                ps.setObject(9, borrowDate);
                ps.addBatch();
            }
            ps.executeBatch();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    private static void seedFines(Connection conn, List<Integer> customerIds, List<Integer> borrowIds, int count) throws Exception {
        String sql = "INSERT INTO fines (customer_id, borrow_id, fine_type, amount, reason, status, paid_date, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        String[] fineTypes = {"LATE_RETURN", "DAMAGED", "LOST"};
        String[] statuses = {"UNPAID", "PAID", "WAIVED"};
        String[] reasons = {
            "Trả sách trễ hạn 5 ngày",
            "Sách bị rách trang",
            "Sách bị mất bìa",
            "Trả sách trễ hạn 10 ngày",
            "Sách bị ướt nước",
            "Mất sách không tìm thấy",
            "Trả sách trễ 2 tuần"
        };
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= count; i++) {
                String fineType = fineTypes[random.nextInt(fineTypes.length)];
                String status = statuses[random.nextInt(statuses.length)];
                double amount;
                
                switch (fineType) {
                    case "LATE_RETURN":
                        amount = (random.nextInt(10) + 1) * 5000.0; // 5k-50k
                        break;
                    case "DAMAGED":
                        amount = (random.nextInt(10) + 5) * 10000.0; // 50k-150k
                        break;
                    case "LOST":
                        amount = (random.nextInt(20) + 10) * 10000.0; // 100k-300k
                        break;
                    default:
                        amount = 50000.0;
                }
                
                LocalDateTime createdAt = randomDateTime(2021, 2026);
                LocalDateTime paidDate = status.equals("PAID") ? createdAt.plusDays(random.nextInt(30)) : null;
                
                ps.setInt(1, customerIds.get(random.nextInt(customerIds.size())));
                ps.setInt(2, borrowIds.get(random.nextInt(borrowIds.size())));
                ps.setString(3, fineType);
                ps.setDouble(4, amount);
                ps.setString(5, reasons[random.nextInt(reasons.length)]);
                ps.setString(6, status);
                ps.setObject(7, paidDate);
                ps.setObject(8, createdAt);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Sinh dữ liệu bán sách (sales + sale_items) đảm bảo mỗi tháng
     * từ 01/2021 đến 10/2026 đều có dữ liệu doanh thu.
     * Mỗi tháng sinh 5-10 đơn bán, mỗi đơn có 1-3 sách.
     */
    private static void seedSales(Connection conn, List<Integer> customerIds,
                                   List<Integer> documentIds, int adminId) throws Exception {
        String saleSql = "INSERT INTO sales (sale_code, customer_id, sale_date, " +
                         "total_amount, discount_amount, final_amount, payment_method, notes, created_by) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO sale_items (sale_id, document_id, quantity, unit_price) " +
                         "VALUES (?, ?, ?, ?)";

        String[] paymentMethods = {"CASH", "CARD", "TRANSFER"};
        int saleCounter = 1;

        // Duyệt từng năm-tháng từ 2021/01 đến 2026/10
        for (int year = 2021; year <= 2026; year++) {
            int maxMonth = (year == 2026) ? 10 : 12; // chỉ đến tháng 10 năm 2026
            for (int month = 1; month <= maxMonth; month++) {
                int ordersThisMonth = 5 + random.nextInt(6); // 5-10 đơn mỗi tháng
                int maxDay = java.time.YearMonth.of(year, month).lengthOfMonth();

                for (int o = 0; o < ordersThisMonth; o++) {
                    // Ngày ngẫu nhiên trong tháng
                    int day = 1 + random.nextInt(maxDay);
                    int hour = 8 + random.nextInt(10);
                    int min  = random.nextInt(60);
                    java.time.LocalDateTime saleDate =
                            java.time.LocalDateTime.of(year, month, day, hour, min);

                    // Khách hàng (có thể null = khách vãng lai)
                    Integer custId = (random.nextInt(3) == 0)
                            ? null
                            : customerIds.get(random.nextInt(customerIds.size()));

                    // Tính tổng tiền từ 1-3 sách
                    int numItems = 1 + random.nextInt(3);
                    double totalAmount = 0;
                    java.util.List<int[]> cartItems = new java.util.ArrayList<>(); // [docId, qty, price]
                    for (int k = 0; k < numItems; k++) {
                        int docId  = documentIds.get(random.nextInt(documentIds.size()));
                        int qty    = 1 + random.nextInt(3);
                        double price = (100 + random.nextInt(900)) * 1000.0;
                        totalAmount += qty * price;
                        cartItems.add(new int[]{docId, qty, (int) price});
                    }
                    double discount    = (random.nextInt(5) == 0) ? totalAmount * 0.05 : 0;
                    double finalAmount = totalAmount - discount;
                    String payMethod   = paymentMethods[random.nextInt(paymentMethods.length)];
                    String saleCode    = String.format("SL%d%02d%05d", year, month, saleCounter++);
                    String notes       = "Bán sách tháng " + month + "/" + year;

                    // Insert sale
                    try (PreparedStatement ps = conn.prepareStatement(saleSql,
                            java.sql.Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, saleCode);
                        if (custId != null) ps.setInt(2, custId); else ps.setNull(2, java.sql.Types.INTEGER);
                        ps.setObject(3, saleDate);
                        ps.setDouble(4, totalAmount);
                        ps.setDouble(5, discount);
                        ps.setDouble(6, finalAmount);
                        ps.setString(7, payMethod);
                        ps.setString(8, notes);
                        ps.setInt(9, adminId);
                        ps.executeUpdate();

                        java.sql.ResultSet keys = ps.getGeneratedKeys();
                        if (keys.next()) {
                            int saleId = keys.getInt(1);
                            // Insert sale_items
                            try (PreparedStatement pi = conn.prepareStatement(itemSql)) {
                                for (int[] item : cartItems) {
                                    pi.setInt(1, saleId);
                                    pi.setInt(2, item[0]);
                                    pi.setInt(3, item[1]);
                                    pi.setDouble(4, item[2]);
                                    pi.addBatch();
                                }
                                pi.executeBatch();
                            }
                        }
                    }
                }
            }
        }
    }
}
