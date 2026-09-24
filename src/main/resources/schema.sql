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
    role ENUM('ADMIN', 'LIBRARIAN') NOT NULL DEFAULT 'LIBRARIAN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
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
-- DỮ LIỆU MẪU được seed tự động qua Java
-- (DatabaseConnection.seedDefaultData)
-- =====================================================

-- =====================================================
-- DỮ LIỆU MẪU 5 NĂM (2021-2026)
-- =====================================================

-- Users
INSERT INTO users (username, password_hash, full_name, role) VALUES
('admin', '$2a$10$XqNwI7XqG0Q8PqGkPQXQ6OZJzZJZJ0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z', 'Quản trị viên', 'ADMIN'),
('librarian1', '$2a$10$XqNwI7XqG0Q8PqGkPQXQ6OZJzZJZJ0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z', 'Nguyễn Văn A', 'LIBRARIAN'),
('librarian2', '$2a$10$XqNwI7XqG0Q8PqGkPQXQ6OZJzZJZJ0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z', 'Trần Thị B', 'LIBRARIAN'),
('librarian3', '$2a$10$XqNwI7XqG0Q8PqGkPQXQ6OZJzZJZJ0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z', 'Lê Văn C', 'LIBRARIAN');

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

