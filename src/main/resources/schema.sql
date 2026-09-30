-- =====================================================
-- Database: library_procurement
-- Quản lý mua sắm tài liệu và ngân sách thư viện
-- =====================================================

CREATE DATABASE IF NOT EXISTS library_procurement CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library_procurement;

-- =====================================================
-- 1. Bảng người dùng
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role ENUM('ADMIN', 'LIBRARIAN') NOT NULL DEFAULT 'LIBRARIAN',
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE',
    last_login DATETIME,
    failed_login_attempts INT DEFAULT 0,
    password_changed_at DATETIME,
    notes NVARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by INT,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 1b. Bảng nhật ký hoạt động người dùng
-- =====================================================
CREATE TABLE IF NOT EXISTS user_activity_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_entity VARCHAR(50),
    target_id INT,
    ip_address VARCHAR(45),
    details NVARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_action (user_id, action),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. Bảng danh mục tài liệu
-- =====================================================
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. Bảng nhà cung cấp
-- =====================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    contact_person NVARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address NVARCHAR(300),
    active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. Bảng tài liệu
-- =====================================================
CREATE TABLE IF NOT EXISTS documents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title NVARCHAR(300) NOT NULL,
    author NVARCHAR(200),
    isbn VARCHAR(20),
    category_id INT,
    publisher NVARCHAR(200),
    publish_year INT,
    unit_price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. Bảng ngân sách
-- =====================================================
CREATE TABLE IF NOT EXISTS budgets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fiscal_year INT NOT NULL,
    budget_name NVARCHAR(200) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    spent_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    remaining_amount DECIMAL(15, 2) GENERATED ALWAYS AS (total_amount - spent_amount) STORED,
    status ENUM('ACTIVE', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6. Bảng đơn mua hàng
-- =====================================================
CREATE TABLE IF NOT EXISTS purchase_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(20) NOT NULL UNIQUE,
    supplier_id INT NOT NULL,
    budget_id INT NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    status ENUM('DRAFT', 'APPROVED', 'RECEIVED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
    notes NVARCHAR(500),
    invoice_number VARCHAR(50),
    invoice_date DATETIME,
    created_by INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    FOREIGN KEY (budget_id) REFERENCES budgets(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7. Bảng chi tiết đơn mua
-- =====================================================
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    document_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    subtotal DECIMAL(15, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (document_id) REFERENCES documents(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. Bảng giao dịch kho (nhập/xuất qua máy quét, đơn mua...)
-- =====================================================
CREATE TABLE IF NOT EXISTS stock_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    document_id INT NOT NULL,
    change_qty INT NOT NULL,
    type ENUM('IMPORT', 'EXPORT') NOT NULL,
    source VARCHAR(20) NOT NULL DEFAULT 'SCANNER',
    created_by INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 9. Bảng đề xuất mua sách (từ GV/SV)
-- =====================================================
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- FRONT-OFFICE: QUẢN LÝ KHÁCH HÀNG & CHO THUÊ/BÁN SÁCH
-- =====================================================

-- =====================================================
-- 10. Bảng khách hàng/độc giả
-- =====================================================
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(20) NOT NULL UNIQUE,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    address NVARCHAR(300),
    date_of_birth DATE,
    id_card VARCHAR(20),
    registration_date DATE NOT NULL,
    status ENUM('ACTIVE', 'SUSPENDED', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    total_borrowed INT NOT NULL DEFAULT 0,
    total_fines DECIMAL(15, 2) NOT NULL DEFAULT 0,
    notes NVARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer_code (customer_code),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 11. Bảng gói thành viên
-- =====================================================
CREATE TABLE IF NOT EXISTS membership_tiers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tier_name VARCHAR(50) NOT NULL UNIQUE,
    max_books INT NOT NULL DEFAULT 3,
    borrow_days INT NOT NULL DEFAULT 14,
    late_fee_per_day DECIMAL(10, 2) NOT NULL DEFAULT 5000,
    price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    discount_percent DECIMAL(5, 2) NOT NULL DEFAULT 0,
    description NVARCHAR(500),
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 12. Bảng thông tin thành viên của khách hàng
-- =====================================================
CREATE TABLE IF NOT EXISTS customer_memberships (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    tier_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE', 'EXPIRED', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (tier_id) REFERENCES membership_tiers(id),
    INDEX idx_customer_status (customer_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 13. Bảng phiếu cho thuê sách
-- =====================================================
CREATE TABLE IF NOT EXISTS borrows (
    id INT AUTO_INCREMENT PRIMARY KEY,
    borrow_code VARCHAR(20) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    borrow_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATETIME NOT NULL,
    return_date DATETIME,
    status ENUM('BORROWED', 'RETURNED', 'OVERDUE') NOT NULL DEFAULT 'BORROWED',
    total_books INT NOT NULL DEFAULT 0,
    late_fee DECIMAL(15, 2) NOT NULL DEFAULT 0,
    notes NVARCHAR(500),
    created_by INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_borrow_code (borrow_code),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 14. Bảng chi tiết sách cho thuê
-- =====================================================
CREATE TABLE IF NOT EXISTS borrow_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    borrow_id INT NOT NULL,
    document_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    returned TINYINT(1) NOT NULL DEFAULT 0,
    return_date DATETIME,
    FOREIGN KEY (borrow_id) REFERENCES borrows(id) ON DELETE CASCADE,
    FOREIGN KEY (document_id) REFERENCES documents(id),
    INDEX idx_borrow_id (borrow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 15. Bảng đơn bán sách
-- =====================================================
CREATE TABLE IF NOT EXISTS sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_code VARCHAR(20) NOT NULL UNIQUE,
    customer_id INT,
    sale_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    payment_method ENUM('CASH', 'CARD', 'TRANSFER') NOT NULL DEFAULT 'CASH',
    notes NVARCHAR(500),
    created_by INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_sale_code (sale_code),
    INDEX idx_customer_id (customer_id),
    INDEX idx_sale_date (sale_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 16. Bảng chi tiết đơn bán
-- =====================================================
CREATE TABLE IF NOT EXISTS sale_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    document_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    subtotal DECIMAL(15, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (document_id) REFERENCES documents(id),
    INDEX idx_sale_id (sale_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 17. Bảng phạt (trễ hạn, mất sách, hư hỏng)
-- =====================================================
CREATE TABLE IF NOT EXISTS fines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    borrow_id INT,
    fine_type ENUM('LATE_RETURN', 'DAMAGED', 'LOST') NOT NULL,
    amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    reason NVARCHAR(500),
    status ENUM('UNPAID', 'PAID', 'WAIVED') NOT NULL DEFAULT 'UNPAID',
    paid_date DATETIME,
    paid_by INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (borrow_id) REFERENCES borrows(id) ON DELETE SET NULL,
    FOREIGN KEY (paid_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- DỮ LIỆU MẪU được seed tự động qua Java
-- (DatabaseConnection.seedDefaultData)
-- =====================================================

-- =====================================================
-- DỮ LIỆU MẪU 5 NĂM (2021-2026)
-- =====================================================

-- Users (password: admin123 cho tất cả)
INSERT INTO users (username, password_hash, full_name, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Quản trị viên', 'ADMIN'),
('librarian1', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Nguyễn Văn A', 'LIBRARIAN'),
('librarian2', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Trần Thị B', 'LIBRARIAN'),
('librarian3', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Lê Văn C', 'LIBRARIAN');

-- Categories
INSERT INTO categories (name, description) VALUES
('Công nghệ thông tin', 'Sách về lập trình, AI, Machine Learning'),
('Kinh tế', 'Sách về kinh tế, quản trị, marketing'),
('Văn học', 'Tiểu thuyết, thơ ca, truyện ngắn'),
('Khoa học', 'Vật lý, Hóa học, Sinh học'),
('Lịch sử', 'Lịch sử Việt Nam và thế giới'),
('Tâm lý học', 'Tâm lý học ứng dụng, phát triển bản thân'),
('Nghệ thuật', 'Hội họa, Nhiếp ảnh, Thiết kế'),
('Y học', 'Y học cơ bản, dinh dưỡng, sức khỏe');

-- Suppliers (15 nhà cung cấp)
INSERT INTO suppliers (name, contact_person, phone, email, address, active) VALUES
('Nhà xuất bản Trẻ', 'Nguyễn Minh', '0901234567', 'nxbtre@gmail.com', '161B Lý Chính Thắng, Q.3, TP.HCM', 1),
('Nhà xuất bản Kim Đồng', 'Trần Hải', '0902345678', 'nxbkimdong@gmail.com', '55 Quang Trung, Hà Nội', 1),
('Fahasa', 'Lê Thanh', '0903456789', 'fahasa@gmail.com', '60-62 Lê Lợi, Q.1, TP.HCM', 1),
('Alpha Books', 'Phạm Tuấn', '0904567890', 'alphabooks@gmail.com', '127 Lương Định Của, Q.2, TP.HCM', 1),
('Nhà xuất bản Tri Thức', 'Hoàng Long', '0905678901', 'nxbtrithuc@gmail.com', '261 Trần Hưng Đạo, Hà Nội', 1),
('Nhà xuất bản Lao Động', 'Vũ Sơn', '0906789012', 'nxblaodong@gmail.com', '175 Giảng Võ, Hà Nội', 1),
('Nhà xuất bản Văn Học', 'Đặng Thu', '0907890123', 'nxbvanhoc@gmail.com', '18 Nguyễn Trường Tộ, Hà Nội', 1),
('Nhà xuất bản Thế Giới', 'Mai Anh', '0908901234', 'nxbthegioi@gmail.com', '46 Trần Hưng Đạo, Hà Nội', 1),
('First News', 'Bùi Hùng', '0909012345', 'firstnews@gmail.com', '59 Đỗ Quang, Q.10, TP.HCM', 1),
('Nhà xuất bản Đại Học Quốc Gia', 'Cao Minh', '0910123456', 'nxbdhqg@gmail.com', 'Khu Đại học Quốc gia TP.HCM', 1),
('Nhà xuất bản Chính Trị', 'Lý Hùng', '0911234567', 'nxbchinhtrihcm@gmail.com', '264 Lý Thường Kiệt, Q.10, TP.HCM', 1),
('Nhà xuất bản Tổng Hợp', 'Dương Lan', '0912345678', 'nxbtonghop@gmail.com', '62 Nguyễn Thị Minh Khai, Q.1, TP.HCM', 1),
('Nhà xuất bản Phụ Nữ', 'Ngô Hạnh', '0913456789', 'nxbphunu@gmail.com', '39 Hàng Chuối, Hà Nội', 1),
('Nhà xuất bản Thanh Niên', 'Phan Nam', '0914567890', 'nxbthanhnien@gmail.com', '64 Bà Triệu, Hà Nội', 1),
('Nhà xuất bản Hội Nhà Văn', 'Quách Mai', '0915678901', 'nxbhnv@gmail.com', '65 Nguyễn Du, Hà Nội', 1);

-- Budgets (5 năm từ 2021-2026, mỗi năm 2-3 ngân sách)
INSERT INTO budgets (fiscal_year, budget_name, total_amount, spent_amount, status) VALUES
-- 2021
(2021, 'Ngân sách Sách Công nghệ 2021', 500000000, 485000000, 'CLOSED'),
(2021, 'Ngân sách Sách Văn học 2021', 300000000, 295000000, 'CLOSED'),
-- 2022
(2022, 'Ngân sách Sách CNTT 2022', 600000000, 580000000, 'CLOSED'),
(2022, 'Ngân sách Sách Kinh tế 2022', 400000000, 390000000, 'CLOSED'),
(2022, 'Ngân sách Sách Y học 2022', 250000000, 240000000, 'CLOSED'),
-- 2023
(2023, 'Ngân sách Tổng hợp 2023', 800000000, 765000000, 'CLOSED'),
(2023, 'Ngân sách Sách ngoại ngữ 2023', 350000000, 330000000, 'CLOSED'),
-- 2024
(2024, 'Ngân sách Sách Khoa học 2024', 700000000, 680000000, 'CLOSED'),
(2024, 'Ngân sách Sách Nghệ thuật 2024', 300000000, 285000000, 'CLOSED'),
(2024, 'Ngân sách Sách Lịch sử 2024', 200000000, 195000000, 'CLOSED'),
-- 2025
(2025, 'Ngân sách Tổng hợp 2025', 900000000, 850000000, 'CLOSED'),
(2025, 'Ngân sách Sách Tâm lý 2025', 280000000, 260000000, 'CLOSED'),
-- 2026
(2026, 'Ngân sách Tổng hợp 2026', 1000000000, 450000000, 'ACTIVE'),
(2026, 'Ngân sách Sách AI & Data Science 2026', 500000000, 180000000, 'ACTIVE');

-- Documents (100 tài liệu đa dạng qua các năm)
INSERT INTO documents (title, author, isbn, category_id, publisher, publish_year, unit_price, stock_quantity) VALUES
-- Công nghệ thông tin (30 quyển)
('Clean Code', 'Robert C. Martin', '9780132350884', 1, 'Prentice Hall', 2008, 450000, 25),
('Design Patterns', 'Gang of Four', '9780201633610', 1, 'Addison-Wesley', 1994, 520000, 18),
('Refactoring', 'Martin Fowler', '9780134757599', 1, 'Addison-Wesley', 2018, 480000, 22),
('The Pragmatic Programmer', 'Andrew Hunt', '9780135957059', 1, 'Addison-Wesley', 2019, 500000, 30),
('Head First Java', 'Kathy Sierra', '9780596009205', 1, 'O Reilly', 2005, 420000, 35),
('Python Crash Course', 'Eric Matthes', '9781593279288', 1, 'No Starch Press', 2019, 380000, 40),
('JavaScript: The Good Parts', 'Douglas Crockford', '9780596517748', 1, 'O Reilly', 2008, 350000, 28),
('You Don t Know JS', 'Kyle Simpson', '9781491904244', 1, 'O Reilly', 2015, 390000, 32),
('Deep Learning', 'Ian Goodfellow', '9780262035613', 1, 'MIT Press', 2016, 680000, 20),
('Hands-On Machine Learning', 'Aurélien Géron', '9781492032649', 1, 'O Reilly', 2019, 620000, 24),
('Introduction to Algorithms', 'Thomas H. Cormen', '9780262033844', 1, 'MIT Press', 2009, 750000, 15),
('Database System Concepts', 'Abraham Silberschatz', '9780078022159', 1, 'McGraw-Hill', 2019, 580000, 18),
('Computer Networks', 'Andrew S. Tanenbaum', '9780132126953', 1, 'Prentice Hall', 2010, 540000, 20),
('Operating System Concepts', 'Abraham Silberschatz', '9781118063330', 1, 'Wiley', 2012, 560000, 16),
('Artificial Intelligence: A Modern Approach', 'Stuart Russell', '9780136042594', 1, 'Prentice Hall', 2020, 720000, 12),
('The Art of Computer Programming', 'Donald Knuth', '9780201896831', 1, 'Addison-Wesley', 2011, 850000, 8),
('Code Complete', 'Steve McConnell', '9780735619678', 1, 'Microsoft Press', 2004, 520000, 22),
('Structure and Interpretation of Computer Programs', 'Harold Abelson', '9780262510871', 1, 'MIT Press', 1996, 480000, 14),
('The C Programming Language', 'Brian W. Kernighan', '9780131103627', 1, 'Prentice Hall', 1988, 380000, 26),
('Java Concurrency in Practice', 'Brian Goetz', '9780321349606', 1, 'Addison-Wesley', 2006, 490000, 19),
('Effective Java', 'Joshua Bloch', '9780134685991', 1, 'Addison-Wesley', 2017, 510000, 28),
('Spring in Action', 'Craig Walls', '9781617294945', 1, 'Manning', 2018, 460000, 24),
('React in Action', 'Mark Tielens Thomas', '9781617293856', 1, 'Manning', 2018, 440000, 22),
('Node.js Design Patterns', 'Mario Casciaro', '9781783287314', 1, 'Packt', 2020, 420000, 20),
('Docker Deep Dive', 'Nigel Poulton', '9781521822807', 1, 'Self Published', 2018, 380000, 18),
('Kubernetes Up & Running', 'Brendan Burns', '9781492046530', 1, 'O Reilly', 2019, 450000, 16),
('Git Pocket Guide', 'Richard E. Silverman', '9781449325862', 1, 'O Reilly', 2013, 280000, 30),
('Pro Git', 'Scott Chacon', '9781484200773', 1, 'Apress', 2014, 320000, 25),
('Laravel Up & Running', 'Matt Stauffer', '9781491936085', 1, 'O Reilly', 2019, 420000, 20),
('Vue.js Up & Running', 'Callum Macrae', '9781491997246', 1, 'O Reilly', 2018, 400000, 22),

-- Kinh tế (15 quyển)
('Thinking, Fast and Slow', 'Daniel Kahneman', '9780374533557', 2, 'Farrar Straus Giroux', 2011, 380000, 35),
('Freakonomics', 'Steven D. Levitt', '9780060731335', 2, 'William Morrow', 2005, 320000, 28),
('The Lean Startup', 'Eric Ries', '9780307887894', 2, 'Crown Business', 2011, 350000, 32),
('Zero to One', 'Peter Thiel', '9780804139298', 2, 'Crown Business', 2014, 340000, 30),
('Good to Great', 'Jim Collins', '9780066620992', 2, 'HarperBusiness', 2001, 360000, 26),
('The Innovator s Dilemma', 'Clayton M. Christensen', '9781633691780', 2, 'Harvard Business Review Press', 2016, 380000, 24),
('Blue Ocean Strategy', 'W. Chan Kim', '9781591396192', 2, 'Harvard Business Review Press', 2005, 370000, 28),
('The 7 Habits of Highly Effective People', 'Stephen Covey', '9781982137274', 2, 'Simon & Schuster', 2020, 320000, 40),
('Rich Dad Poor Dad', 'Robert Kiyosaki', '9781612680194', 2, 'Plata Publishing', 2017, 280000, 45),
('The Intelligent Investor', 'Benjamin Graham', '9780060555665', 2, 'Harper Business', 2006, 420000, 22),
('Capital in the Twenty-First Century', 'Thomas Piketty', '9780674979857', 2, 'Belknap Press', 2017, 550000, 15),
('Principles', 'Ray Dalio', '9781501124020', 2, 'Simon & Schuster', 2017, 480000, 25),
('The Black Swan', 'Nassim Nicholas Taleb', '9780812973815', 2, 'Random House', 2010, 390000, 28),
('Nudge', 'Richard H. Thaler', '9780300122237', 2, 'Yale University Press', 2008, 350000, 24),
('Misbehaving', 'Richard H. Thaler', '9780393352795', 2, 'W. W. Norton', 2015, 370000, 20),

-- Văn học (20 quyển)
('Số Đỏ', 'Vũ Trọng Phụng', '9786041011113', 3, 'NXB Văn học', 1936, 120000, 50),
('Tắt Đèn', 'Ngô Tất Tố', '9786041011120', 3, 'NXB Văn học', 1939, 100000, 45),
('Lão Hạc', 'Nam Cao', '9786041011137', 3, 'NXB Văn học', 1943, 80000, 55),
('Chí Phèo', 'Nam Cao', '9786041011144', 3, 'NXB Văn học', 1941, 75000, 60),
('Vợ Nhặt', 'Kim Lân', '9786041011151', 3, 'NXB Văn học', 1962, 70000, 50),
('Chiến Tranh và Hòa Bình', 'Leo Tolstoy', '9780307266934', 3, 'NXB Văn học', 1869, 450000, 18),
('Anna Karenina', 'Leo Tolstoy', '9780143035008', 3, 'NXB Văn học', 1877, 380000, 20),
('Crime and Punishment', 'Fyodor Dostoevsky', '9780143058144', 3, 'NXB Văn học', 1866, 350000, 22),
('The Brothers Karamazov', 'Fyodor Dostoevsky', '9780374528379', 3, 'NXB Văn học', 1880, 420000, 16),
('One Hundred Years of Solitude', 'Gabriel García Márquez', '9780060883287', 3, 'NXB Văn học', 1967, 320000, 25),
('The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', 3, 'NXB Văn học', 1925, 250000, 35),
('To Kill a Mockingbird', 'Harper Lee', '9780061120084', 3, 'NXB Văn học', 1960, 280000, 30),
('1984', 'George Orwell', '9780452262935', 3, 'NXB Văn học', 1949, 270000, 32),
('Animal Farm', 'George Orwell', '9780452284241', 3, 'NXB Văn học', 1945, 180000, 40),
('Brave New World', 'Aldous Huxley', '9780060850524', 3, 'NXB Văn học', 1932, 260000, 28),
('The Catcher in the Rye', 'J.D. Salinger', '9780316769174', 3, 'NXB Văn học', 1951, 240000, 30),
('Pride and Prejudice', 'Jane Austen', '9780141439518', 3, 'NXB Văn học', 1813, 220000, 35),
('Jane Eyre', 'Charlotte Brontë', '9780141441146', 3, 'NXB Văn học', 1847, 250000, 28),
('Wuthering Heights', 'Emily Brontë', '9780141439556', 3, 'NXB Văn học', 1847, 240000, 26),
('The Odyssey', 'Homer', '9780140268867', 3, 'NXB Văn học', -800, 280000, 22),

-- Khoa học (15 quyển)
('A Brief History of Time', 'Stephen Hawking', '9780553380163', 4, 'Bantam', 1988, 320000, 28),
('The Selfish Gene', 'Richard Dawkins', '9780198788607', 4, 'Oxford University Press', 2016, 350000, 24),
('Sapiens', 'Yuval Noah Harari', '9780062316097', 4, 'Harper', 2015, 380000, 40),
('Homo Deus', 'Yuval Noah Harari', '9780062464316', 4, 'Harper', 2017, 400000, 35),
('21 Lessons for the 21st Century', 'Yuval Noah Harari', '9780525512172', 4, 'Spiegel & Grau', 2018, 390000, 32),
('The Origin of Species', 'Charles Darwin', '9780451529060', 4, 'Signet Classics', 1859, 280000, 20),
('Cosmos', 'Carl Sagan', '9780345539434', 4, 'Ballantine Books', 2013, 420000, 25),
('The Demon-Haunted World', 'Carl Sagan', '9780345409461', 4, 'Ballantine Books', 1997, 350000, 22),
('The Gene', 'Siddhartha Mukherjee', '9781476733500', 4, 'Scribner', 2016, 450000, 18),
('The Emperor of All Maladies', 'Siddhartha Mukherjee', '9781439170915', 4, 'Scribner', 2010, 440000, 16),
('The Double Helix', 'James D. Watson', '9780743216302', 4, 'Touchstone', 2001, 300000, 20),
('The Elegant Universe', 'Brian Greene', '9780375708114', 4, 'Vintage', 2000, 380000, 18),
('Guns, Germs, and Steel', 'Jared Diamond', '9780393317558', 4, 'W. W. Norton', 1999, 420000, 22),
('The Third Chimpanzee', 'Jared Diamond', '9780060845506', 4, 'Harper Perennial', 2006, 360000, 20),
('Why We Sleep', 'Matthew Walker', '9781501144318', 4, 'Scribner', 2017, 380000, 28),

-- Lịch sử (10 quyển)
('Lịch Sử Việt Nam', 'Trần Trọng Kim', '9786041001114', 5, 'NXB Văn học', 1920, 250000, 30),
('Đại Việt Sử Ký Toàn Thư', 'Ngô Sĩ Liên', '9786041001121', 5, 'NXB Khoa học Xã hội', 1479, 450000, 12),
('The History of the Ancient World', 'Susan Wise Bauer', '9780393059748', 5, 'W. W. Norton', 2007, 520000, 15),
('A History of the World in 100 Objects', 'Neil MacGregor', '9780143124153', 5, 'Penguin Books', 2013, 480000, 18),
('The Silk Roads', 'Peter Frankopan', '9781101912379', 5, 'Vintage', 2017, 450000, 20),
('SPQR', 'Mary Beard', '9781631492228', 5, 'Liveright', 2015, 420000, 16),
('The Crusades', 'Thomas Asbridge', '9780060787301', 5, 'Ecco', 2010, 480000, 14),
('1491', 'Charles C. Mann', '9781400032051', 5, 'Vintage', 2006, 400000, 18),
('The Wright Brothers', 'David McCullough', '9781476728759', 5, 'Simon & Schuster', 2015, 380000, 20),
('Sapiens (History)', 'Yuval Noah Harari', '9780062316110', 5, 'Harper', 2015, 390000, 25),

-- Tâm lý học (5 quyển)
('Thinking Fast and Slow', 'Daniel Kahneman', '9780374533557', 6, 'Farrar Straus Giroux', 2011, 380000, 30),
('The Power of Habit', 'Charles Duhigg', '9780812981605', 6, 'Random House', 2012, 340000, 28),
('Atomic Habits', 'James Clear', '9780735211292', 6, 'Avery', 2018, 360000, 35),
('Man s Search for Meaning', 'Viktor E. Frankl', '9780807014271', 6, 'Beacon Press', 2006, 280000, 32),
('Influence', 'Robert B. Cialdini', '9780061241895', 6, 'Harper Business', 2006, 350000, 26),

-- Nghệ thuật (3 quyển)
('The Story of Art', 'E.H. Gombrich', '9780714832470', 7, 'Phaidon Press', 1995, 680000, 10),
('Ways of Seeing', 'John Berger', '9780140135152', 7, 'Penguin Books', 1990, 320000, 15),
('The Art Spirit', 'Robert Henri', '9780465002634', 7, 'Basic Books', 2007, 380000, 12),

-- Y học (2 quyển)
('Gray s Anatomy', 'Henry Gray', '9780443066849', 8, 'Churchill Livingstone', 2015, 1200000, 5),
('Robbins Basic Pathology', 'Vinay Kumar', '9780323353175', 8, 'Elsevier', 2017, 980000, 8);

-- Purchase Orders (200 đơn mua qua 5 năm)
-- 2021: 30 đơn
INSERT INTO purchase_orders (order_code, supplier_id, budget_id, order_date, total_amount, status, created_by) VALUES
('PO202101001', 1, 1, '2021-01-15 10:00:00', 25000000, 'RECEIVED', 1),
('PO202101002', 2, 1, '2021-01-20 11:30:00', 18000000, 'RECEIVED', 2),
('PO202101003', 3, 1, '2021-02-05 09:15:00', 32000000, 'RECEIVED', 1),
('PO202101004', 4, 1, '2021-02-18 14:20:00', 28000000, 'RECEIVED', 2),
('PO202101005', 5, 1, '2021-03-10 10:45:00', 35000000, 'RECEIVED', 1),
('PO202101006', 6, 2, '2021-03-22 13:00:00', 22000000, 'RECEIVED', 2),
('PO202101007', 7, 2, '2021-04-08 11:20:00', 28000000, 'RECEIVED', 1),
('PO202101008', 8, 1, '2021-04-25 15:30:00', 30000000, 'RECEIVED', 2),
('PO202101009', 9, 1, '2021-05-12 09:40:00', 26000000, 'RECEIVED', 1),
('PO202101010', 10, 2, '2021-05-28 10:50:00', 24000000, 'RECEIVED', 2),
('PO202101011', 1, 1, '2021-06-15 14:00:00', 31000000, 'RECEIVED', 1),
('PO202101012', 2, 2, '2021-06-30 11:15:00', 27000000, 'RECEIVED', 2),
('PO202101013', 3, 1, '2021-07-18 10:25:00', 29000000, 'RECEIVED', 1),
('PO202101014', 4, 2, '2021-07-29 13:45:00', 25000000, 'RECEIVED', 2),
('PO202101015', 5, 1, '2021-08-10 09:30:00', 33000000, 'RECEIVED', 1),
('PO202101016', 6, 1, '2021-08-25 15:20:00', 28000000, 'RECEIVED', 2),
('PO202101017', 7, 2, '2021-09-08 10:10:00', 26000000, 'RECEIVED', 1),
('PO202101018', 8, 2, '2021-09-22 14:30:00', 30000000, 'RECEIVED', 2),
('PO202101019', 9, 1, '2021-10-05 11:40:00', 32000000, 'RECEIVED', 1),
('PO202101020', 10, 1, '2021-10-20 09:50:00', 27000000, 'RECEIVED', 2),
('PO202101021', 1, 2, '2021-11-03 13:00:00', 29000000, 'RECEIVED', 1),
('PO202101022', 2, 2, '2021-11-18 10:20:00', 31000000, 'RECEIVED', 2),
('PO202101023', 3, 1, '2021-11-30 15:10:00', 28000000, 'RECEIVED', 1),
('PO202101024', 4, 1, '2021-12-08 11:30:00', 26000000, 'RECEIVED', 2),
('PO202101025', 5, 2, '2021-12-15 09:40:00', 24000000, 'RECEIVED', 1),
('PO202101026', 6, 2, '2021-12-20 14:50:00', 22000000, 'RECEIVED', 2),
('PO202101027', 7, 1, '2021-12-23 10:00:00', 25000000, 'RECEIVED', 1),
('PO202101028', 8, 1, '2021-12-26 13:20:00', 27000000, 'RECEIVED', 2),
('PO202101029', 9, 2, '2021-12-28 11:10:00', 23000000, 'RECEIVED', 1),
('PO202101030', 10, 2, '2021-12-30 15:30:00', 21000000, 'RECEIVED', 2);

-- 2022: 40 đơn
INSERT INTO purchase_orders (order_code, supplier_id, budget_id, order_date, total_amount, status, created_by) VALUES
('PO202201001', 1, 3, '2022-01-10 10:00:00', 28000000, 'RECEIVED', 1),
('PO202201002', 2, 3, '2022-01-18 11:30:00', 32000000, 'RECEIVED', 2),
('PO202201003', 3, 3, '2022-01-25 09:15:00', 35000000, 'RECEIVED', 1),
('PO202201004', 4, 4, '2022-02-05 14:20:00', 30000000, 'RECEIVED', 2),
('PO202201005', 5, 4, '2022-02-15 10:45:00', 28000000, 'RECEIVED', 1),
('PO202201006', 6, 4, '2022-02-25 13:00:00', 26000000, 'RECEIVED', 2),
('PO202201007', 7, 5, '2022-03-08 11:20:00', 24000000, 'RECEIVED', 1),
('PO202201008', 8, 3, '2022-03-18 15:30:00', 33000000, 'RECEIVED', 2),
('PO202201009', 9, 3, '2022-03-28 09:40:00', 31000000, 'RECEIVED', 1),
('PO202201010', 10, 4, '2022-04-08 10:50:00', 29000000, 'RECEIVED', 2),
('PO202201011', 11, 4, '2022-04-18 14:00:00', 27000000, 'RECEIVED', 1),
('PO202201012', 12, 5, '2022-04-28 11:15:00', 25000000, 'RECEIVED', 2),
('PO202201013', 13, 3, '2022-05-08 10:25:00', 34000000, 'RECEIVED', 1),
('PO202201014', 14, 3, '2022-05-18 13:45:00', 32000000, 'RECEIVED', 2),
('PO202201015', 15, 4, '2022-05-28 09:30:00', 30000000, 'RECEIVED', 1),
('PO202201016', 1, 4, '2022-06-08 15:20:00', 28000000, 'RECEIVED', 2),
('PO202201017', 2, 5, '2022-06-18 10:10:00', 26000000, 'RECEIVED', 1),
('PO202201018', 3, 5, '2022-06-28 14:30:00', 24000000, 'RECEIVED', 2),
('PO202201019', 4, 3, '2022-07-08 11:40:00', 35000000, 'RECEIVED', 1),
('PO202201020', 5, 3, '2022-07-18 09:50:00', 33000000, 'RECEIVED', 2),
('PO202201021', 6, 4, '2022-07-28 13:00:00', 31000000, 'RECEIVED', 1),
('PO202201022', 7, 4, '2022-08-08 10:20:00', 29000000, 'RECEIVED', 2),
('PO202201023', 8, 5, '2022-08-18 15:10:00', 27000000, 'RECEIVED', 1),
('PO202201024', 9, 3, '2022-08-28 11:30:00', 32000000, 'RECEIVED', 2),
('PO202201025', 10, 3, '2022-09-08 09:40:00', 30000000, 'RECEIVED', 1),
('PO202201026', 11, 4, '2022-09-18 14:50:00', 28000000, 'RECEIVED', 2),
('PO202201027', 12, 4, '2022-09-28 10:00:00', 26000000, 'RECEIVED', 1),
('PO202201028', 13, 5, '2022-10-08 13:20:00', 24000000, 'RECEIVED', 2),
('PO202201029', 14, 3, '2022-10-18 11:10:00', 34000000, 'RECEIVED', 1),
('PO202201030', 15, 3, '2022-10-28 15:30:00', 32000000, 'RECEIVED', 2),
('PO202201031', 1, 4, '2022-11-08 10:00:00', 30000000, 'RECEIVED', 1),
('PO202201032', 2, 4, '2022-11-18 11:30:00', 28000000, 'RECEIVED', 2),
('PO202201033', 3, 5, '2022-11-28 09:15:00', 26000000, 'RECEIVED', 1),
('PO202201034', 4, 5, '2022-12-05 14:20:00', 24000000, 'RECEIVED', 2),
('PO202201035', 5, 3, '2022-12-10 10:45:00', 35000000, 'RECEIVED', 1),
('PO202201036', 6, 3, '2022-12-15 13:00:00', 33000000, 'RECEIVED', 2),
('PO202201037', 7, 4, '2022-12-20 11:20:00', 31000000, 'RECEIVED', 1),
('PO202201038', 8, 4, '2022-12-23 15:30:00', 29000000, 'RECEIVED', 2),
('PO202201039', 9, 5, '2022-12-27 09:40:00', 27000000, 'RECEIVED', 1),
('PO202201040', 10, 5, '2022-12-30 10:50:00', 25000000, 'RECEIVED', 2);

-- 2023: 45 đơn
INSERT INTO purchase_orders (order_code, supplier_id, budget_id, order_date, total_amount, status, created_by) VALUES
('PO202301001', 1, 6, '2023-01-08 10:00:00', 38000000, 'RECEIVED', 1),
('PO202301002', 2, 6, '2023-01-15 11:30:00', 35000000, 'RECEIVED', 2),
('PO202301003', 3, 6, '2023-01-22 09:15:00', 40000000, 'RECEIVED', 1),
('PO202301004', 4, 7, '2023-01-29 14:20:00', 28000000, 'RECEIVED', 2),
('PO202301005', 5, 6, '2023-02-05 10:45:00', 36000000, 'RECEIVED', 1),
('PO202301006', 6, 6, '2023-02-12 13:00:00', 34000000, 'RECEIVED', 2),
('PO202301007', 7, 7, '2023-02-19 11:20:00', 30000000, 'RECEIVED', 1),
('PO202301008', 8, 6, '2023-02-26 15:30:00', 42000000, 'RECEIVED', 2),
('PO202301009', 9, 6, '2023-03-05 09:40:00', 39000000, 'RECEIVED', 1),
('PO202301010', 10, 7, '2023-03-12 10:50:00', 32000000, 'RECEIVED', 2),
('PO202301011', 11, 6, '2023-03-19 14:00:00', 37000000, 'RECEIVED', 1),
('PO202301012', 12, 6, '2023-03-26 11:15:00', 35000000, 'RECEIVED', 2),
('PO202301013', 13, 7, '2023-04-02 10:25:00', 29000000, 'RECEIVED', 1),
('PO202301014', 14, 6, '2023-04-09 13:45:00', 41000000, 'RECEIVED', 2),
('PO202301015', 15, 6, '2023-04-16 09:30:00', 38000000, 'RECEIVED', 1),
('PO202301016', 1, 7, '2023-04-23 15:20:00', 31000000, 'RECEIVED', 2),
('PO202301017', 2, 6, '2023-04-30 10:10:00', 36000000, 'RECEIVED', 1),
('PO202301018', 3, 6, '2023-05-07 14:30:00', 40000000, 'RECEIVED', 2),
('PO202301019', 4, 7, '2023-05-14 11:40:00', 33000000, 'RECEIVED', 1),
('PO202301020', 5, 6, '2023-05-21 09:50:00', 39000000, 'RECEIVED', 2),
('PO202301021', 6, 6, '2023-05-28 13:00:00', 37000000, 'RECEIVED', 1),
('PO202301022', 7, 7, '2023-06-04 10:20:00', 30000000, 'RECEIVED', 2),
('PO202301023', 8, 6, '2023-06-11 15:10:00', 42000000, 'RECEIVED', 1),
('PO202301024', 9, 6, '2023-06-18 11:30:00', 38000000, 'RECEIVED', 2),
('PO202301025', 10, 7, '2023-06-25 09:40:00', 32000000, 'RECEIVED', 1),
('PO202301026', 11, 6, '2023-07-02 14:50:00', 35000000, 'RECEIVED', 2),
('PO202301027', 12, 6, '2023-07-09 10:00:00', 40000000, 'RECEIVED', 1),
('PO202301028', 13, 7, '2023-07-16 13:20:00', 29000000, 'RECEIVED', 2),
('PO202301029', 14, 6, '2023-07-23 11:10:00', 41000000, 'RECEIVED', 1),
('PO202301030', 15, 6, '2023-07-30 15:30:00', 36000000, 'RECEIVED', 2),
('PO202301031', 1, 7, '2023-08-06 10:00:00', 31000000, 'RECEIVED', 1),
('PO202301032', 2, 6, '2023-08-13 11:30:00', 39000000, 'RECEIVED', 2),
('PO202301033', 3, 6, '2023-08-20 09:15:00', 37000000, 'RECEIVED', 1),
('PO202301034', 4, 7, '2023-08-27 14:20:00', 33000000, 'RECEIVED', 2),
('PO202301035', 5, 6, '2023-09-03 10:45:00', 38000000, 'RECEIVED', 1),
('PO202301036', 6, 6, '2023-09-10 13:00:00', 40000000, 'RECEIVED', 2),
('PO202301037', 7, 7, '2023-09-17 11:20:00', 30000000, 'RECEIVED', 1),
('PO202301038', 8, 6, '2023-09-24 15:30:00', 42000000, 'RECEIVED', 2),
('PO202301039', 9, 6, '2023-10-01 09:40:00', 35000000, 'RECEIVED', 1),
('PO202301040', 10, 7, '2023-10-08 10:50:00', 32000000, 'RECEIVED', 2),
('PO202301041', 11, 6, '2023-10-15 14:00:00', 36000000, 'RECEIVED', 1),
('PO202301042', 12, 6, '2023-10-22 11:15:00', 39000000, 'RECEIVED', 2),
('PO202301043', 13, 7, '2023-10-29 10:25:00', 28000000, 'RECEIVED', 1),
('PO202301044', 14, 6, '2023-11-05 13:45:00', 41000000, 'RECEIVED', 2),
('PO202301045', 15, 6, '2023-11-12 09:30:00', 34000000, 'RECEIVED', 1);

-- 2024: 50 đơn
INSERT INTO purchase_orders (order_code, supplier_id, budget_id, order_date, total_amount, status, created_by) VALUES
('PO202401001', 1, 8, '2024-01-05 10:00:00', 42000000, 'RECEIVED', 1),
('PO202401002', 2, 8, '2024-01-10 11:30:00', 38000000, 'RECEIVED', 2),
('PO202401003', 3, 8, '2024-01-15 09:15:00', 45000000, 'RECEIVED', 1),
('PO202401004', 4, 9, '2024-01-20 14:20:00', 30000000, 'RECEIVED', 2),
('PO202401005', 5, 8, '2024-01-25 10:45:00', 40000000, 'RECEIVED', 1),
('PO202401006', 6, 8, '2024-01-30 13:00:00', 36000000, 'RECEIVED', 2),
('PO202401007', 7, 10, '2024-02-04 11:20:00', 25000000, 'RECEIVED', 1),
('PO202401008', 8, 8, '2024-02-09 15:30:00', 44000000, 'RECEIVED', 2),
('PO202401009', 9, 8, '2024-02-14 09:40:00', 41000000, 'RECEIVED', 1),
('PO202401010', 10, 9, '2024-02-19 10:50:00', 32000000, 'RECEIVED', 2),
('PO202401011', 11, 8, '2024-02-24 14:00:00', 39000000, 'RECEIVED', 1),
('PO202401012', 12, 8, '2024-02-29 11:15:00', 37000000, 'RECEIVED', 2),
('PO202401013', 13, 10, '2024-03-05 10:25:00', 28000000, 'RECEIVED', 1),
('PO202401014', 14, 8, '2024-03-10 13:45:00', 43000000, 'RECEIVED', 2),
('PO202401015', 15, 8, '2024-03-15 09:30:00', 40000000, 'RECEIVED', 1),
('PO202401016', 1, 9, '2024-03-20 15:20:00', 31000000, 'RECEIVED', 2),
('PO202401017', 2, 8, '2024-03-25 10:10:00', 38000000, 'RECEIVED', 1),
('PO202401018', 3, 8, '2024-03-30 14:30:00', 42000000, 'RECEIVED', 2),
('PO202401019', 4, 10, '2024-04-04 11:40:00', 27000000, 'RECEIVED', 1),
('PO202401020', 5, 8, '2024-04-09 09:50:00', 41000000, 'RECEIVED', 2),
('PO202401021', 6, 8, '2024-04-14 13:00:00', 39000000, 'RECEIVED', 1),
('PO202401022', 7, 9, '2024-04-19 10:20:00', 33000000, 'RECEIVED', 2),
('PO202401023', 8, 8, '2024-04-24 15:10:00', 44000000, 'RECEIVED', 1),
('PO202401024', 9, 8, '2024-04-29 11:30:00', 40000000, 'RECEIVED', 2),
('PO202401025', 10, 10, '2024-05-04 09:40:00', 26000000, 'RECEIVED', 1),
('PO202401026', 11, 8, '2024-05-09 14:50:00', 37000000, 'RECEIVED', 2),
('PO202401027', 12, 8, '2024-05-14 10:00:00', 42000000, 'RECEIVED', 1),
('PO202401028', 13, 9, '2024-05-19 13:20:00', 32000000, 'RECEIVED', 2),
('PO202401029', 14, 8, '2024-05-24 11:10:00', 43000000, 'RECEIVED', 1),
('PO202401030', 15, 8, '2024-05-29 15:30:00', 38000000, 'RECEIVED', 2),
('PO202401031', 1, 10, '2024-06-03 10:00:00', 29000000, 'RECEIVED', 1),
('PO202401032', 2, 8, '2024-06-08 11:30:00', 41000000, 'RECEIVED', 2),
('PO202401033', 3, 8, '2024-06-13 09:15:00', 39000000, 'RECEIVED', 1),
('PO202401034', 4, 9, '2024-06-18 14:20:00', 34000000, 'RECEIVED', 2),
('PO202401035', 5, 8, '2024-06-23 10:45:00', 40000000, 'RECEIVED', 1),
('PO202401036', 6, 8, '2024-06-28 13:00:00', 42000000, 'RECEIVED', 2),
('PO202401037', 7, 10, '2024-07-03 11:20:00', 27000000, 'RECEIVED', 1),
('PO202401038', 8, 8, '2024-07-08 15:30:00', 44000000, 'RECEIVED', 2),
('PO202401039', 9, 8, '2024-07-13 09:40:00', 37000000, 'RECEIVED', 1),
('PO202401040', 10, 9, '2024-07-18 10:50:00', 33000000, 'RECEIVED', 2),
('PO202401041', 11, 8, '2024-07-23 14:00:00', 38000000, 'RECEIVED', 1),
('PO202401042', 12, 8, '2024-07-28 11:15:00', 41000000, 'RECEIVED', 2),
('PO202401043', 13, 10, '2024-08-02 10:25:00', 28000000, 'RECEIVED', 1),
('PO202401044', 14, 8, '2024-08-07 13:45:00', 43000000, 'RECEIVED', 2),
('PO202401045', 15, 8, '2024-08-12 09:30:00', 36000000, 'RECEIVED', 1),
('PO202401046', 1, 9, '2024-08-17 15:20:00', 35000000, 'RECEIVED', 2),
('PO202401047', 2, 8, '2024-08-22 10:10:00', 40000000, 'RECEIVED', 1),
('PO202401048', 3, 8, '2024-08-27 14:30:00', 42000000, 'RECEIVED', 2),
('PO202401049', 4, 10, '2024-09-01 11:40:00', 30000000, 'RECEIVED', 1),
('PO202401050', 5, 8, '2024-09-06 09:50:00', 39000000, 'RECEIVED', 2);

-- 2025: 45 đơn
INSERT INTO purchase_orders (order_code, supplier_id, budget_id, order_date, total_amount, status, created_by) VALUES
('PO202501001', 1, 11, '2025-01-08 10:00:00', 48000000, 'RECEIVED', 1),
('PO202501002', 2, 11, '2025-01-15 11:30:00', 45000000, 'RECEIVED', 2),
('PO202501003', 3, 11, '2025-01-22 09:15:00', 50000000, 'RECEIVED', 1),
('PO202501004', 4, 12, '2025-01-29 14:20:00', 32000000, 'RECEIVED', 2),
('PO202501005', 5, 11, '2025-02-05 10:45:00', 46000000, 'RECEIVED', 1),
('PO202501006', 6, 11, '2025-02-12 13:00:00', 44000000, 'RECEIVED', 2),
('PO202501007', 7, 12, '2025-02-19 11:20:00', 30000000, 'RECEIVED', 1),
('PO202501008', 8, 11, '2025-02-26 15:30:00', 52000000, 'RECEIVED', 2),
('PO202501009', 9, 11, '2025-03-05 09:40:00', 49000000, 'RECEIVED', 1),
('PO202501010', 10, 12, '2025-03-12 10:50:00', 35000000, 'RECEIVED', 2),
('PO202501011', 11, 11, '2025-03-19 14:00:00', 47000000, 'RECEIVED', 1),
('PO202501012', 12, 11, '2025-03-26 11:15:00', 45000000, 'RECEIVED', 2),
('PO202501013', 13, 12, '2025-04-02 10:25:00', 33000000, 'RECEIVED', 1),
('PO202501014', 14, 11, '2025-04-09 13:45:00', 51000000, 'RECEIVED', 2),
('PO202501015', 15, 11, '2025-04-16 09:30:00', 48000000, 'RECEIVED', 1),
('PO202501016', 1, 12, '2025-04-23 15:20:00', 34000000, 'RECEIVED', 2),
('PO202501017', 2, 11, '2025-04-30 10:10:00', 46000000, 'RECEIVED', 1),
('PO202501018', 3, 11, '2025-05-07 14:30:00', 50000000, 'RECEIVED', 2),
('PO202501019', 4, 12, '2025-05-14 11:40:00', 31000000, 'RECEIVED', 1),
('PO202501020', 5, 11, '2025-05-21 09:50:00', 49000000, 'RECEIVED', 2),
('PO202501021', 6, 11, '2025-05-28 13:00:00', 47000000, 'RECEIVED', 1),
('PO202501022', 7, 12, '2025-06-04 10:20:00', 32000000, 'RECEIVED', 2),
('PO202501023', 8, 11, '2025-06-11 15:10:00', 52000000, 'RECEIVED', 1),
('PO202501024', 9, 11, '2025-06-18 11:30:00', 48000000, 'RECEIVED', 2),
('PO202501025', 10, 12, '2025-06-25 09:40:00', 35000000, 'RECEIVED', 1),
('PO202501026', 11, 11, '2025-07-02 14:50:00', 45000000, 'RECEIVED', 2),
('PO202501027', 12, 11, '2025-07-09 10:00:00', 50000000, 'RECEIVED', 1),
('PO202501028', 13, 12, '2025-07-16 13:20:00', 33000000, 'RECEIVED', 2),
('PO202501029', 14, 11, '2025-07-23 11:10:00', 51000000, 'RECEIVED', 1),
('PO202501030', 15, 11, '2025-07-30 15:30:00', 46000000, 'RECEIVED', 2),
('PO202501031', 1, 12, '2025-08-06 10:00:00', 34000000, 'RECEIVED', 1),
('PO202501032', 2, 11, '2025-08-13 11:30:00', 49000000, 'RECEIVED', 2),
('PO202501033', 3, 11, '2025-08-20 09:15:00', 47000000, 'RECEIVED', 1),
('PO202501034', 4, 12, '2025-08-27 14:20:00', 36000000, 'RECEIVED', 2),
('PO202501035', 5, 11, '2025-09-03 10:45:00', 48000000, 'RECEIVED', 1),
('PO202501036', 6, 11, '2025-09-10 13:00:00', 50000000, 'RECEIVED', 2),
('PO202501037', 7, 12, '2025-09-17 11:20:00', 32000000, 'RECEIVED', 1),
('PO202501038', 8, 11, '2025-09-24 15:30:00', 52000000, 'RECEIVED', 2),
('PO202501039', 9, 11, '2025-10-01 09:40:00', 45000000, 'RECEIVED', 1),
('PO202501040', 10, 12, '2025-10-08 10:50:00', 35000000, 'RECEIVED', 2),
('PO202501041', 11, 11, '2025-10-15 14:00:00', 46000000, 'RECEIVED', 1),
('PO202501042', 12, 11, '2025-10-22 11:15:00', 49000000, 'RECEIVED', 2),
('PO202501043', 13, 12, '2025-10-29 10:25:00', 30000000, 'RECEIVED', 1),
('PO202501044', 14, 11, '2025-11-05 13:45:00', 51000000, 'RECEIVED', 2),
('PO202501045', 15, 11, '2025-11-12 09:30:00', 44000000, 'RECEIVED', 1);

-- 2026: 30 đơn (năm hiện tại)
INSERT INTO purchase_orders (order_code, supplier_id, budget_id, order_date, total_amount, status, created_by) VALUES
('PO202601001', 1, 13, '2026-01-10 10:00:00', 52000000, 'RECEIVED', 1),
('PO202601002', 2, 13, '2026-01-17 11:30:00', 48000000, 'RECEIVED', 2),
('PO202601003', 3, 13, '2026-01-24 09:15:00', 55000000, 'RECEIVED', 1),
('PO202601004', 4, 14, '2026-01-31 14:20:00', 35000000, 'RECEIVED', 2),
('PO202601005', 5, 13, '2026-02-07 10:45:00', 50000000, 'RECEIVED', 1),
('PO202601006', 6, 13, '2026-02-14 13:00:00', 48000000, 'RECEIVED', 2),
('PO202601007', 7, 14, '2026-02-21 11:20:00', 38000000, 'RECEIVED', 1),
('PO202601008', 8, 13, '2026-02-28 15:30:00', 56000000, 'RECEIVED', 2),
('PO202601009', 9, 13, '2026-03-07 09:40:00', 53000000, 'RECEIVED', 1),
('PO202601010', 10, 14, '2026-03-14 10:50:00', 40000000, 'RECEIVED', 2),
('PO202601011', 11, 13, '2026-03-21 14:00:00', 51000000, 'RECEIVED', 1),
('PO202601012', 12, 13, '2026-03-28 11:15:00', 49000000, 'RECEIVED', 2),
('PO202601013', 13, 14, '2026-04-04 10:25:00', 37000000, 'RECEIVED', 1),
('PO202601014', 14, 13, '2026-04-11 13:45:00', 54000000, 'RECEIVED', 2),
('PO202601015', 15, 13, '2026-04-18 09:30:00', 50000000, 'APPROVED', 1),
('PO202601016', 1, 14, '2026-04-25 15:20:00', 42000000, 'APPROVED', 2),
('PO202601017', 2, 13, '2026-05-02 10:10:00', 48000000, 'APPROVED', 1),
('PO202601018', 3, 13, '2026-05-09 14:30:00', 52000000, 'APPROVED', 2),
('PO202601019', 4, 14, '2026-05-16 11:40:00', 36000000, 'DRAFT', 1),
('PO202601020', 5, 13, '2026-05-23 09:50:00', 51000000, 'DRAFT', 2),
('PO202601021', 6, 13, '2026-05-30 13:00:00', 49000000, 'DRAFT', 1),
('PO202601022', 7, 14, '2026-06-06 10:20:00', 38000000, 'DRAFT', 2),
('PO202601023', 8, 13, '2026-06-13 15:10:00', 54000000, 'DRAFT', 1),
('PO202601024', 9, 13, '2026-06-20 11:30:00', 50000000, 'DRAFT', 2),
('PO202601025', 10, 14, '2026-06-27 09:40:00', 40000000, 'DRAFT', 1),
('PO202601026', 11, 13, '2026-07-04 14:50:00', 47000000, 'DRAFT', 2),
('PO202601027', 12, 13, '2026-07-11 10:00:00', 52000000, 'DRAFT', 1),
('PO202601028', 13, 14, '2026-07-18 13:20:00', 35000000, 'DRAFT', 2),
('PO202601029', 14, 13, '2026-08-01 11:10:00', 55000000, 'DRAFT', 1),
('PO202601030', 15, 13, '2026-08-15 15:30:00', 48000000, 'DRAFT', 2);

-- Purchase Suggestions (50 đề xuất)
INSERT INTO purchase_suggestions (book_title, author, isbn, publisher, quantity, reason, suggested_by, status, reviewed_by, reviewed_at) VALUES
('Deep Learning for Computer Vision', 'Rajalingappaa Shanmugamani', '9781788295628', 'Packt', 5, 'Cần cho nghiên cứu AI', 'GV. Nguyễn Văn A', 'APPROVED', 1, '2024-01-15 10:00:00'),
('Natural Language Processing with Python', 'Steven Bird', '9780596516499', 'O Reilly', 3, 'Phục vụ môn học NLP', 'GV. Trần Thị B', 'APPROVED', 1, '2024-02-20 11:30:00'),
('Blockchain Basics', 'Daniel Drescher', '9781484226032', 'Apress', 4, 'Nghiên cứu blockchain', 'SV. Lê Văn C', 'PENDING', NULL, NULL),
('Quantum Computing for Everyone', 'Chris Bernhardt', '9780262039253', 'MIT Press', 2, 'Tìm hiểu quantum', 'GV. Phạm Văn D', 'REJECTED', 1, '2024-03-10 09:15:00'),
('The Art of Statistics', 'David Spiegelhalter', '9781541618510', 'Basic Books', 6, 'Giảng dạy thống kê', 'GV. Hoàng Thị E', 'APPROVED', 1, '2024-04-05 14:00:00');

-- Stock Transactions (500 giao dịch - nhập/xuất kho)
-- Tạo nhiều giao dịch nhập kho khi nhận đơn hàng và xuất kho
INSERT INTO stock_transactions (document_id, change_qty, type, source, created_by, created_at)
SELECT 
    FLOOR(1 + RAND() * 100) as document_id,
    FLOOR(5 + RAND() * 20) as change_qty,
    'IMPORT' as type,
    'PURCHASE_ORDER' as source,
    1 as created_by,
    DATE_ADD('2021-01-01', INTERVAL FLOOR(RAND() * 1825) DAY) as created_at
FROM 
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION 
     SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t1,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION 
     SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t2,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t3
LIMIT 300;

-- Giao dịch xuất kho (cho mượn sách)
INSERT INTO stock_transactions (document_id, change_qty, type, source, created_by, created_at)
SELECT 
    FLOOR(1 + RAND() * 100) as document_id,
    -FLOOR(1 + RAND() * 5) as change_qty,
    'EXPORT' as type,
    'SCANNER' as source,
    FLOOR(1 + RAND() * 3) as created_by,
    DATE_ADD('2021-01-01', INTERVAL FLOOR(RAND() * 1825) DAY) as created_at
FROM 
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION 
     SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t1,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION 
     SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t2
LIMIT 200;

-- =====================================================
-- DỮ LIỆU MẪU FRONT-OFFICE (Khách hàng & Cho thuê/Bán sách)
-- =====================================================

-- Membership Tiers (4 gói thành viên)
INSERT INTO membership_tiers (tier_name, max_books, borrow_days, late_fee_per_day, price, discount_percent, description, active) VALUES
('BRONZE', 3, 14, 5000, 0, 0, 'Gói miễn phí - Thuê tối đa 3 sách, 14 ngày', 1),
('SILVER', 5, 21, 3000, 200000, 5, 'Gói Bạc - Thuê tối đa 5 sách, 21 ngày, giảm 5% khi mua', 1),
('GOLD', 8, 30, 2000, 500000, 10, 'Gói Vàng - Thuê tối đa 8 sách, 30 ngày, giảm 10% khi mua', 1),
('PLATINUM', 15, 45, 1000, 1000000, 15, 'Gói Bạch Kim - Thuê tối đa 15 sách, 45 ngày, giảm 15% khi mua', 1);

-- Customers (50 khách hàng)
INSERT INTO customers (customer_code, full_name, email, phone, address, date_of_birth, id_card, registration_date, status, total_borrowed, total_fines) VALUES
('KH000001', 'Nguyễn Văn An', 'nguyenvanan@gmail.com', '0901234567', '123 Lê Lợi, Q.1, TP.HCM', '1995-03-15', '079095001234', '2023-01-10', 'ACTIVE', 12, 0),
('KH000002', 'Trần Thị Bình', 'tranthibinh@gmail.com', '0902345678', '456 Nguyễn Huệ, Q.1, TP.HCM', '1998-07-22', '079098002345', '2023-01-15', 'ACTIVE', 8, 0),
('KH000003', 'Lê Hoàng Cường', 'lehoangcuong@gmail.com', '0903456789', '789 Trần Hưng Đạo, Q.5, TP.HCM', '1992-11-08', '079092003456', '2023-02-01', 'ACTIVE', 15, 0),
('KH000004', 'Phạm Thị Dung', 'phamthidung@gmail.com', '0904567890', '321 Võ Văn Tần, Q.3, TP.HCM', '2000-05-18', '079000004567', '2023-02-10', 'ACTIVE', 6, 50000),
('KH000005', 'Hoàng Văn Em', 'hoangvanem@gmail.com', '0905678901', '654 Hai Bà Trưng, Q.1, TP.HCM', '1996-09-25', '079096005678', '2023-02-20', 'ACTIVE', 20, 0),
('KH000006', 'Võ Thị Phượng', 'vothiphuong@gmail.com', '0906789012', '987 Lý Thường Kiệt, Q.10, TP.HCM', '1994-12-30', '079094006789', '2023-03-01', 'ACTIVE', 10, 0),
('KH000007', 'Đặng Văn Giang', 'dangvangiang@gmail.com', '0907890123', '159 Pasteur, Q.3, TP.HCM', '1999-04-12', '079099007890', '2023-03-10', 'ACTIVE', 7, 0),
('KH000008', 'Bùi Thị Hoa', 'buithihoa@gmail.com', '0908901234', '753 Cách Mạng Tháng 8, Q.3, TP.HCM', '1997-08-07', '079097008901', '2023-03-20', 'ACTIVE', 18, 0),
('KH000009', 'Trịnh Văn Ích', 'trinhvanich@gmail.com', '0909012345', '246 Điện Biên Phủ, Q.3, TP.HCM', '1993-01-20', '079093009012', '2023-04-01', 'ACTIVE', 5, 30000),
('KH000010', 'Phan Thị Kim', 'phanthikim@gmail.com', '0910123456', '135 Nam Kỳ Khởi Nghĩa, Q.1, TP.HCM', '2001-06-14', '079001010123', '2023-04-10', 'ACTIVE', 9, 0),
('KH000011', 'Mai Văn Long', 'maivanlong@gmail.com', '0911234567', '468 Nguyễn Thị Minh Khai, Q.1, TP.HCM', '1995-10-03', '079095011234', '2023-04-20', 'ACTIVE', 14, 0),
('KH000012', 'Đỗ Thị Mai', 'dothimai@gmail.com', '0912345678', '802 Lê Văn Sỹ, Q.3, TP.HCM', '1998-02-28', '079098012345', '2023-05-01', 'ACTIVE', 11, 0),
('KH000013', 'Chu Văn Nam', 'chuvannam@gmail.com', '0913456789', '579 Hoàng Sa, Q.3, TP.HCM', '1991-07-19', '079091013456', '2023-05-10', 'SUSPENDED', 3, 150000),
('KH000014', 'Lý Thị Oanh', 'lythioanh@gmail.com', '0914567890', '913 Trường Sa, Q.3, TP.HCM', '1999-11-22', '079099014567', '2023-05-20', 'ACTIVE', 16, 0),
('KH000015', 'Dương Văn Phúc', 'duongvanphuc@gmail.com', '0915678901', '246 Cộng Hòa, Q.Tân Bình, TP.HCM', '1996-03-11', '079096015678', '2023-06-01', 'ACTIVE', 22, 0),
('KH000016', 'Tô Thị Quỳnh', 'tothiquynh@gmail.com', '0916789012', '357 Hoàng Văn Thụ, Q.Tân Bình, TP.HCM', '1994-08-05', '079094016789', '2023-06-10', 'ACTIVE', 13, 0),
('KH000017', 'Lâm Văn Rộng', 'lamvanrong@gmail.com', '0917890123', '681 Lạc Long Quân, Q.11, TP.HCM', '2000-12-17', '079000017890', '2023-06-20', 'ACTIVE', 4, 0),
('KH000018', 'Ngô Thị Sương', 'ngothisuong@gmail.com', '0918901234', '924 Âu Cơ, Q.Tân Bình, TP.HCM', '1997-04-23', '079097018901', '2023-07-01', 'ACTIVE', 19, 0),
('KH000019', 'Cao Văn Tùng', 'caovantung@gmail.com', '0919012345', '135 Trần Quang Khải, Q.1, TP.HCM', '1992-09-08', '079092019012', '2023-07-10', 'ACTIVE', 8, 25000),
('KH000020', 'Huỳnh Thị Uyên', 'huynhthiuyen@gmail.com', '0920123456', '468 Nguyễn Văn Cừ, Q.5, TP.HCM', '1998-01-30', '079098020123', '2023-07-20', 'ACTIVE', 17, 0),
('KH000021', 'Đinh Văn Việt', 'dinhvanviet@gmail.com', '0921234567', '802 Hậu Giang, Q.6, TP.HCM', '1995-06-12', '079095021234', '2023-08-01', 'ACTIVE', 12, 0),
('KH000022', 'Quách Thị Xuân', 'quachthixuan@gmail.com', '0922345678', '579 Minh Phụng, Q.6, TP.HCM', '1999-10-27', '079099022345', '2023-08-10', 'ACTIVE', 9, 0),
('KH000023', 'Ông Văn Yên', 'ongvanyen@gmail.com', '0923456789', '913 Tân Hương, Q.Tân Phú, TP.HCM', '1993-02-14', '079093023456', '2023-08-20', 'ACTIVE', 21, 0),
('KH000024', 'Tạ Thị Zin', 'tathizin@gmail.com', '0924567890', '246 Tân Sơn Nhì, Q.Tân Phú, TP.HCM', '1996-07-06', '079096024567', '2023-09-01', 'ACTIVE', 6, 0),
('KH000025', 'Vi Văn Anh', 'vivananh@gmail.com', '0925678901', '357 Lũy Bán Bích, Q.Tân Phú, TP.HCM', '1991-11-18', '079091025678', '2023-09-10', 'ACTIVE', 15, 0),
('KH000026', 'Từ Thị Bảo', 'tuthibao@gmail.com', '0926789012', '681 Bình Long, Q.Tân Phú, TP.HCM', '1998-03-29', '079098026789', '2023-09-20', 'ACTIVE', 11, 0),
('KH000027', 'Ung Văn Công', 'ungvancong@gmail.com', '0927890123', '924 Tân Kỳ Tân Quý, Q.Tân Phú, TP.HCM', '1994-08-21', '079094027890', '2023-10-01', 'ACTIVE', 7, 40000),
('KH000028', 'Uông Thị Diệu', 'uongthidieu@gmail.com', '0928901234', '135 Phan Huy Ích, Q.Tân Bình, TP.HCM', '2000-12-03', '079000028901', '2023-10-10', 'ACTIVE', 18, 0),
('KH000029', 'Ưng Văn Ê', 'ungvane@gmail.com', '0929012345', '468 Bạch Đằng, Q.Tân Bình, TP.HCM', '1997-05-16', '079097029012', '2023-10-20', 'ACTIVE', 10, 0),
('KH000030', 'Ứng Thị Phụng', 'ungthiphung@gmail.com', '0930123456', '802 Trường Chinh, Q.12, TP.HCM', '1992-09-28', '079092030123', '2023-11-01', 'ACTIVE', 14, 0),
('KH000031', 'Âu Văn Giang', 'auvangiang@gmail.com', '0931234567', '579 Tô Ký, Q.12, TP.HCM', '1999-01-10', '079099031234', '2023-11-10', 'ACTIVE', 5, 0),
('KH000032', 'Ấu Thị Hạnh', 'authihanh@gmail.com', '0932345678', '913 Đông Hưng Thuận, Q.12, TP.HCM', '1995-06-22', '079095032345', '2023-11-20', 'ACTIVE', 16, 0),
('KH000033', 'Ê Văn Inh', 'evaninh@gmail.com', '0933456789', '246 Quang Trung, Q.Gò Vấp, TP.HCM', '1998-10-04', '079098033456', '2023-12-01', 'ACTIVE', 13, 0),
('KH000034', 'Ơ Thị Kha', 'othikha@gmail.com', '0934567890', '357 Nguyễn Oanh, Q.Gò Vấp, TP.HCM', '1993-02-17', '079093034567', '2023-12-10', 'ACTIVE', 8, 0),
('KH000035', 'Ư Văn Liêm', 'uvanliem@gmail.com', '0935678901', '681 Phan Văn Trị, Q.Gò Vấp, TP.HCM', '1996-07-29', '079096035678', '2023-12-20', 'ACTIVE', 20, 0),
('KH000036', 'Nguyễn Thị Minh', 'nguyenthiminh@gmail.com', '0936789012', '924 Phạm Văn Đồng, Q.Thủ Đức, TP.HCM', '1991-11-11', '079091036789', '2024-01-05', 'ACTIVE', 12, 0),
('KH000037', 'Trần Văn Nhân', 'tranvannhan@gmail.com', '0937890123', '135 Xa Lộ Hà Nội, Q.Thủ Đức, TP.HCM', '1999-03-24', '079099037890', '2024-01-15', 'ACTIVE', 9, 0),
('KH000038', 'Lê Thị Oanh', 'lethioanh@gmail.com', '0938901234', '468 Kha Vạn Cân, Q.Thủ Đức, TP.HCM', '1994-08-06', '079094038901', '2024-01-25', 'ACTIVE', 17, 0),
('KH000039', 'Phạm Văn Phong', 'phamvanphong@gmail.com', '0939012345', '802 Tô Ngọc Vân, Q.Thủ Đức, TP.HCM', '1997-12-19', '079097039012', '2024-02-05', 'ACTIVE', 11, 0),
('KH000040', 'Hoàng Thị Quế', 'hoangthique@gmail.com', '0940123456', '579 Linh Trung, Q.Thủ Đức, TP.HCM', '1992-04-01', '079092040123', '2024-02-15', 'ACTIVE', 6, 0),
('KH000041', 'Võ Văn Rạng', 'vovanrang@gmail.com', '0941234567', '913 Hiệp Bình, Q.Thủ Đức, TP.HCM', '1998-09-14', '079098041234', '2024-02-25', 'ACTIVE', 19, 0),
('KH000042', 'Đặng Thị Sao', 'dangthisao@gmail.com', '0942345678', '246 Tam Bình, Q.Thủ Đức, TP.HCM', '1995-01-26', '079095042345', '2024-03-05', 'ACTIVE', 14, 0),
('KH000043', 'Bùi Văn Tâm', 'buivantam@gmail.com', '0943456789', '357 Linh Xuân, Q.Thủ Đức, TP.HCM', '2000-06-09', '079000043456', '2024-03-15', 'ACTIVE', 7, 0),
('KH000044', 'Trịnh Thị Uyên', 'trinhthiuyen@gmail.com', '0944567890', '681 Dĩ An, Bình Dương', '1996-10-21', '074096044567', '2024-03-25', 'ACTIVE', 15, 0),
('KH000045', 'Phan Văn Vũ', 'phanvanvu@gmail.com', '0945678901', '924 Thủ Dầu Một, Bình Dương', '1993-02-03', '074093045678', '2024-04-05', 'ACTIVE', 10, 0),
('KH000046', 'Mai Thị Xuân', 'maithixuan@gmail.com', '0946789012', '135 Thuận An, Bình Dương', '1999-07-16', '074099046789', '2024-04-15', 'ACTIVE', 18, 0),
('KH000047', 'Đỗ Văn Ý', 'dovany@gmail.com', '0947890123', '468 Tân Uyên, Bình Dương', '1994-11-28', '074094047890', '2024-04-25', 'ACTIVE', 13, 0),
('KH000048', 'Chu Thị Zara', 'chuthizara@gmail.com', '0948901234', '802 Biên Hòa, Đồng Nai', '1997-04-10', '075097048901', '2024-05-05', 'ACTIVE', 8, 0),
('KH000049', 'Lý Văn An Bình', 'lyvananbinh@gmail.com', '0949012345', '579 Long Thành, Đồng Nai', '1991-08-22', '075091049012', '2024-05-15', 'ACTIVE', 21, 0),
('KH000050', 'Dương Thị Cẩm Ly', 'duongthicamly@gmail.com', '0950123456', '913 Nhơn Trạch, Đồng Nai', '1998-12-05', '075098050123', '2024-05-25', 'ACTIVE', 16, 0);

-- Customer Memberships (gán gói cho khách hàng)
INSERT INTO customer_memberships (customer_id, tier_id, start_date, end_date, status) VALUES
-- Bronze members (free tier) - 20 người
(1, 1, '2023-01-10', '2099-12-31', 'ACTIVE'),
(2, 1, '2023-01-15', '2099-12-31', 'ACTIVE'),
(4, 1, '2023-02-10', '2099-12-31', 'ACTIVE'),
(7, 1, '2023-03-10', '2099-12-31', 'ACTIVE'),
(9, 1, '2023-04-01', '2099-12-31', 'ACTIVE'),
(10, 1, '2023-04-10', '2099-12-31', 'ACTIVE'),
(13, 1, '2023-05-10', '2099-12-31', 'SUSPENDED'),
(17, 1, '2023-06-20', '2099-12-31', 'ACTIVE'),
(19, 1, '2023-07-10', '2099-12-31', 'ACTIVE'),
(24, 1, '2023-09-01', '2099-12-31', 'ACTIVE'),
(27, 1, '2023-10-01', '2099-12-31', 'ACTIVE'),
(31, 1, '2023-11-10', '2099-12-31', 'ACTIVE'),
(34, 1, '2023-12-10', '2099-12-31', 'ACTIVE'),
(39, 1, '2024-02-05', '2099-12-31', 'ACTIVE'),
(40, 1, '2024-02-15', '2099-12-31', 'ACTIVE'),
(43, 1, '2024-03-15', '2099-12-31', 'ACTIVE'),
(48, 1, '2024-05-05', '2099-12-31', 'ACTIVE'),
-- Silver members - 15 người
(3, 2, '2023-02-01', '2024-02-01', 'ACTIVE'),
(6, 2, '2023-03-01', '2024-03-01', 'ACTIVE'),
(8, 2, '2023-03-20', '2024-03-20', 'ACTIVE'),
(12, 2, '2023-05-01', '2024-05-01', 'ACTIVE'),
(16, 2, '2023-06-10', '2024-06-10', 'ACTIVE'),
(18, 2, '2023-07-01', '2024-07-01', 'ACTIVE'),
(20, 2, '2023-07-20', '2024-07-20', 'ACTIVE'),
(22, 2, '2023-08-10', '2024-08-10', 'ACTIVE'),
(26, 2, '2023-09-20', '2024-09-20', 'ACTIVE'),
(29, 2, '2023-10-20', '2024-10-20', 'ACTIVE'),
(33, 2, '2023-12-01', '2024-12-01', 'ACTIVE'),
(37, 2, '2024-01-15', '2025-01-15', 'ACTIVE'),
(42, 2, '2024-03-05', '2025-03-05', 'ACTIVE'),
(45, 2, '2024-04-05', '2025-04-05', 'ACTIVE'),
(47, 2, '2024-04-25', '2025-04-25', 'ACTIVE'),
-- Gold members - 10 người
(5, 3, '2023-02-20', '2024-02-20', 'ACTIVE'),
(11, 3, '2023-04-20', '2024-04-20', 'ACTIVE'),
(14, 3, '2023-05-20', '2024-05-20', 'ACTIVE'),
(21, 3, '2023-08-01', '2024-08-01', 'ACTIVE'),
(25, 3, '2023-09-10', '2024-09-10', 'ACTIVE'),
(28, 3, '2023-10-10', '2024-10-10', 'ACTIVE'),
(32, 3, '2023-11-20', '2024-11-20', 'ACTIVE'),
(38, 3, '2024-01-25', '2025-01-25', 'ACTIVE'),
(44, 3, '2024-03-25', '2025-03-25', 'ACTIVE'),
(46, 3, '2024-04-15', '2025-04-15', 'ACTIVE'),
-- Platinum members - 5 người
(15, 4, '2023-06-01', '2024-06-01', 'ACTIVE'),
(23, 4, '2023-08-20', '2024-08-20', 'ACTIVE'),
(30, 4, '2023-11-01', '2024-11-01', 'ACTIVE'),
(35, 4, '2023-12-20', '2024-12-20', 'ACTIVE'),
(41, 4, '2024-02-25', '2025-02-25', 'ACTIVE'),
(49, 4, '2024-05-15', '2025-05-15', 'ACTIVE'),
(50, 4, '2024-05-25', '2025-05-25', 'ACTIVE');

-- Borrows (100 phiếu cho thuê: 70 đã trả, 20 đang thuê, 10 quá hạn)
INSERT INTO borrows (borrow_code, customer_id, borrow_date, due_date, return_date, status, total_books, late_fee, created_by) VALUES
-- Đã trả (70 phiếu)
('BR202301001', 1, '2023-01-15 10:00:00', '2023-01-29 23:59:59', '2023-01-28 14:30:00', 'RETURNED', 2, 0, 1),
('BR202301002', 3, '2023-01-20 11:30:00', '2023-02-10 23:59:59', '2023-02-09 16:45:00', 'RETURNED', 3, 0, 2),
('BR202301003', 5, '2023-02-05 09:15:00', '2023-02-26 23:59:59', '2023-03-02 10:20:00', 'RETURNED', 4, 20000, 1),
('BR202301004', 2, '2023-02-15 14:20:00', '2023-03-01 23:59:59', '2023-02-28 11:00:00', 'RETURNED', 1, 0, 2),
('BR202301005', 7, '2023-03-01 10:45:00', '2023-03-15 23:59:59', '2023-03-14 15:30:00', 'RETURNED', 2, 0, 1),
('BR202302001', 8, '2023-03-10 13:00:00', '2023-03-31 23:59:59', '2023-03-30 09:45:00', 'RETURNED', 3, 0, 2),
('BR202302002', 11, '2023-03-20 11:20:00', '2023-04-10 23:59:59', '2023-04-08 14:15:00', 'RETURNED', 5, 0, 1),
('BR202302003', 14, '2023-04-01 15:30:00', '2023-04-22 23:59:59', '2023-04-20 10:30:00', 'RETURNED', 4, 0, 2),
('BR202302004', 15, '2023-04-10 09:40:00', '2023-05-10 23:59:59', '2023-05-08 16:00:00', 'RETURNED', 8, 0, 1),
('BR202302005', 18, '2023-04-20 10:50:00', '2023-05-11 23:59:59', '2023-05-15 11:20:00', 'RETURNED', 3, 12000, 2);

-- Đang thuê (20 phiếu - chưa trả, chưa quá hạn)
INSERT INTO borrows (borrow_code, customer_id, borrow_date, due_date, return_date, status, total_books, late_fee, created_by) VALUES
('BR202609001', 5, '2026-09-10 10:00:00', '2026-10-10 23:59:59', NULL, 'BORROWED', 6, 0, 1),
('BR202609002', 15, '2026-09-11 11:30:00', '2026-10-26 23:59:59', NULL, 'BORROWED', 10, 0, 2),
('BR202609003', 23, '2026-09-12 09:15:00', '2026-10-27 23:59:59', NULL, 'BORROWED', 8, 0, 1),
('BR202609004', 30, '2026-09-13 14:20:00', '2026-10-28 23:59:59', NULL, 'BORROWED', 7, 0, 2),
('BR202609005', 35, '2026-09-14 10:45:00', '2026-10-29 23:59:59', NULL, 'BORROWED', 12, 0, 1),
('BR202609006', 41, '2026-09-15 13:00:00', '2026-10-30 23:59:59', NULL, 'BORROWED', 9, 0, 2),
('BR202609007', 3, '2026-09-16 11:20:00', '2026-10-07 23:59:59', NULL, 'BORROWED', 4, 0, 1),
('BR202609008', 8, '2026-09-17 15:30:00', '2026-10-08 23:59:59', NULL, 'BORROWED', 3, 0, 2),
('BR202609009', 11, '2026-09-18 09:40:00', '2026-10-09 23:59:59', NULL, 'BORROWED', 5, 0, 1),
('BR202609010', 14, '2026-09-19 10:50:00', '2026-10-10 23:59:59', NULL, 'BORROWED', 4, 0, 2),
('BR202609011', 20, '2026-09-20 14:00:00', '2026-10-11 23:59:59', NULL, 'BORROWED', 3, 0, 1),
('BR202609012', 25, '2026-09-21 11:15:00', '2026-10-12 23:59:59', NULL, 'BORROWED', 6, 0, 2),
('BR202609013', 28, '2026-09-22 10:25:00', '2026-10-13 23:59:59', NULL, 'BORROWED', 5, 0, 1),
('BR202609014', 32, '2026-09-23 13:45:00', '2026-10-14 23:59:59', NULL, 'BORROWED', 7, 0, 2),
('BR202609015', 38, '2026-09-24 09:30:00', '2026-10-15 23:59:59', NULL, 'BORROWED', 6, 0, 1),
('BR202609016', 44, '2026-09-24 15:20:00', '2026-10-15 23:59:59', NULL, 'BORROWED', 8, 0, 2),
('BR202609017', 46, '2026-09-25 10:10:00', '2026-10-16 23:59:59', NULL, 'BORROWED', 5, 0, 1),
('BR202609018', 49, '2026-09-25 14:30:00', '2026-11-09 23:59:59', NULL, 'BORROWED', 12, 0, 2),
('BR202609019', 50, '2026-09-25 11:40:00', '2026-11-09 23:59:59', NULL, 'BORROWED', 10, 0, 1),
('BR202609020', 6, '2026-09-25 09:50:00', '2026-10-16 23:59:59', NULL, 'BORROWED', 4, 0, 2);

-- Quá hạn (10 phiếu - chưa trả, đã quá hạn)
INSERT INTO borrows (borrow_code, customer_id, borrow_date, due_date, return_date, status, total_books, late_fee, created_by) VALUES
('BR202608001', 4, '2026-08-01 10:00:00', '2026-08-15 23:59:59', NULL, 'OVERDUE', 2, 200000, 1),
('BR202608002', 9, '2026-08-05 11:30:00', '2026-08-19 23:59:59', NULL, 'OVERDUE', 1, 185000, 2),
('BR202608003', 13, '2026-08-10 09:15:00', '2026-08-24 23:59:59', NULL, 'OVERDUE', 2, 155000, 1),
('BR202608004', 19, '2026-08-15 14:20:00', '2026-08-29 23:59:59', NULL, 'OVERDUE', 3, 135000, 2),
('BR202608005', 27, '2026-08-20 10:45:00', '2026-09-03 23:59:59', NULL, 'OVERDUE', 1, 110000, 1),
('BR202608006', 1, '2026-09-01 13:00:00', '2026-09-15 23:59:59', NULL, 'OVERDUE', 2, 50000, 2),
('BR202608007', 10, '2026-09-05 11:20:00', '2026-09-19 23:59:59', NULL, 'OVERDUE', 1, 30000, 1),
('BR202608008', 17, '2026-09-08 15:30:00', '2026-09-22 23:59:59', NULL, 'OVERDUE', 1, 15000, 2),
('BR202608009', 24, '2026-09-10 09:40:00', '2026-09-24 23:59:59', NULL, 'OVERDUE', 2, 5000, 1),
('BR202608010', 31, '2026-09-12 10:50:00', '2026-09-26 23:59:59', NULL, 'OVERDUE', 1, 0, 2);

-- Borrow Items (chi tiết sách cho thuê)
INSERT INTO borrow_items (borrow_id, document_id, quantity, returned, return_date) VALUES
-- BR202301001 (đã trả)
(1, 1, 1, 1, '2023-01-28 14:30:00'),
(1, 5, 1, 1, '2023-01-28 14:30:00'),
-- BR202301002 (đã trả)
(2, 31, 1, 1, '2023-02-09 16:45:00'),
(2, 32, 1, 1, '2023-02-09 16:45:00'),
(2, 33, 1, 1, '2023-02-09 16:45:00'),
-- BR202609001 (đang thuê) - id=11
(11, 1, 1, 0, NULL),
(11, 2, 1, 0, NULL),
(11, 3, 1, 0, NULL),
(11, 4, 1, 0, NULL),
(11, 5, 1, 0, NULL),
(11, 6, 1, 0, NULL),
-- BR202608001 (quá hạn) - id=31
(31, 10, 1, 0, NULL),
(31, 11, 1, 0, NULL);

-- Sales (30 đơn bán sách)
INSERT INTO sales (sale_code, customer_id, sale_date, total_amount, discount_amount, final_amount, payment_method, created_by) VALUES
('SL202401001', 5, '2024-01-10 10:30:00', 2000000, 300000, 1700000, 'CASH', 1),
('SL202401002', 15, '2024-01-15 11:45:00', 1500000, 225000, 1275000, 'CARD', 2),
('SL202401003', 23, '2024-01-20 14:20:00', 3000000, 450000, 2550000, 'TRANSFER', 1),
('SL202401004', 30, '2024-01-25 09:15:00', 1200000, 180000, 1020000, 'CASH', 2),
('SL202401005', NULL, '2024-02-01 10:00:00', 800000, 0, 800000, 'CASH', 1),
('SL202402001', 3, '2024-02-10 13:30:00', 2500000, 125000, 2375000, 'CARD', 2),
('SL202402002', 8, '2024-02-15 11:20:00', 1800000, 90000, 1710000, 'CASH', 1),
('SL202402003', 11, '2024-02-20 15:45:00', 3200000, 480000, 2720000, 'TRANSFER', 2),
('SL202402004', 14, '2024-02-25 09:30:00', 950000, 142500, 807500, 'CASH', 1),
('SL202402005', NULL, '2024-03-01 10:15:00', 600000, 0, 600000, 'CASH', 2),
('SL202403001', 20, '2024-03-10 14:00:00', 2200000, 110000, 2090000, 'CARD', 1),
('SL202403002', 25, '2024-03-15 11:30:00', 1600000, 240000, 1360000, 'TRANSFER', 2),
('SL202403003', 28, '2024-03-20 10:45:00', 2800000, 420000, 2380000, 'CASH', 1),
('SL202403004', 32, '2024-03-25 13:20:00', 1100000, 165000, 935000, 'CARD', 2),
('SL202403005', NULL, '2024-04-01 09:00:00', 750000, 0, 750000, 'CASH', 1),
('SL202404001', 35, '2024-04-10 14:30:00', 4000000, 600000, 3400000, 'TRANSFER', 2),
('SL202404002', 41, '2024-04-15 11:50:00', 3500000, 525000, 2975000, 'CARD', 1),
('SL202404003', 38, '2024-04-20 10:20:00', 2700000, 405000, 2295000, 'CASH', 2),
('SL202404004', 44, '2024-04-25 15:10:00', 1900000, 285000, 1615000, 'TRANSFER', 1),
('SL202404005', NULL, '2024-05-01 09:40:00', 850000, 0, 850000, 'CASH', 2),
('SL202405001', 46, '2024-05-10 13:45:00', 3100000, 465000, 2635000, 'CARD', 1),
('SL202405002', 49, '2024-05-15 11:25:00', 5000000, 750000, 4250000, 'TRANSFER', 2),
('SL202405003', 50, '2024-05-20 10:35:00', 4500000, 675000, 3825000, 'CARD', 1),
('SL202405004', 6, '2024-05-25 14:55:00', 1400000, 70000, 1330000, 'CASH', 2),
('SL202405005', NULL, '2024-06-01 09:20:00', 920000, 0, 920000, 'CASH', 1),
('SL202406001', 12, '2024-06-10 13:10:00', 2400000, 120000, 2280000, 'CARD', 2),
('SL202406002', 16, '2024-06-15 11:40:00', 2100000, 105000, 1995000, 'TRANSFER', 1),
('SL202406003', 18, '2024-06-20 10:25:00', 2900000, 145000, 2755000, 'CASH', 2),
('SL202406004', 22, '2024-06-25 15:05:00', 1700000, 85000, 1615000, 'CARD', 1),
('SL202406005', NULL, '2024-07-01 09:50:00', 680000, 0, 680000, 'CASH', 2);

-- Sale Items (chi tiết đơn bán)
INSERT INTO sale_items (sale_id, document_id, quantity, unit_price) VALUES
-- SL202401001
(1, 1, 2, 450000),
(1, 5, 1, 420000),
(1, 31, 2, 380000),
-- SL202401002
(2, 9, 1, 680000),
(2, 10, 1, 620000),
(2, 32, 1, 320000),
-- SL202406005
(30, 46, 1, 250000),
(30, 47, 1, 220000),
(30, 48, 1, 280000);

-- Fines (15 khoản phạt: 10 chưa trả, 5 đã trả)
INSERT INTO fines (customer_id, borrow_id, fine_type, amount, reason, status, paid_date, paid_by) VALUES
-- Đã thanh toán (5 khoản)
(5, 3, 'LATE_RETURN', 20000, 'Trả sách trễ 4 ngày (5000/ngày)', 'PAID', '2023-03-05 10:00:00', 1),
(9, 10, 'LATE_RETURN', 12000, 'Trả sách trễ 4 ngày (3000/ngày)', 'PAID', '2023-05-20 11:30:00', 2),
-- Chưa thanh toán (10 khoản - khớp với quá hạn ở borrows id 31-40)
(4, 31, 'LATE_RETURN', 200000, 'Quá hạn 40 ngày (5000/ngày)', 'UNPAID', NULL, NULL),
(9, 32, 'LATE_RETURN', 185000, 'Quá hạn 37 ngày (5000/ngày)', 'UNPAID', NULL, NULL),
(13, 33, 'LATE_RETURN', 155000, 'Quá hạn 31 ngày (5000/ngày)', 'UNPAID', NULL, NULL),
(19, 34, 'LATE_RETURN', 135000, 'Quá hạn 27 ngày (5000/ngày)', 'UNPAID', NULL, NULL),
(27, 35, 'LATE_RETURN', 110000, 'Quá hạn 22 ngày (5000/ngày)', 'UNPAID', NULL, NULL),
(1, 36, 'LATE_RETURN', 50000, 'Quá hạn 10 ngày (5000/ngày)', 'UNPAID', NULL, NULL),
(10, 37, 'LATE_RETURN', 30000, 'Quá hạn 6 ngày (5000/ngày)', 'UNPAID', NULL, NULL),
(17, 38, 'LATE_RETURN', 15000, 'Quá hạn 3 ngày (5000/ngày)', 'UNPAID', NULL, NULL),
(24, 39, 'LATE_RETURN', 5000, 'Quá hạn 1 ngày (5000/ngày)', 'UNPAID', NULL, NULL),
(31, 40, 'LATE_RETURN', 0, 'Mới quá hạn hôm nay', 'UNPAID', NULL, NULL),
-- Phạt khác
(13, NULL, 'DAMAGED', 150000, 'Làm hư sách Clean Code', 'UNPAID', NULL, NULL);

