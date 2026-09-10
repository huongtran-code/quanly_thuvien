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

