#!/bin/bash
# =====================================================
# Script cài đặt môi trường cho dự án
# Quản lý Mua sắm Tài liệu Thư viện
# =====================================================

set -e

echo "======================================"
echo " SETUP MÔI TRƯỜNG DỰ ÁN THƯ VIỆN"
echo "======================================"

# 1. Cài Homebrew (nếu chưa có)
if ! command -v brew &> /dev/null; then
    echo ""
    echo "[1/4] Cài đặt Homebrew..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    
    # Thêm vào PATH (Apple Silicon)
    if [ -f "/opt/homebrew/bin/brew" ]; then
        echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
        eval "$(/opt/homebrew/bin/brew shellenv)"
    fi
else
    echo "[1/4] ✓ Homebrew đã có"
fi

# 2. Cài Java 17
if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -q "17"; then
    echo ""
    echo "[2/4] Cài đặt Java 17 (OpenJDK)..."
    brew install openjdk@17
    
    # Symlink để macOS nhận ra
    sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    
    # Thêm vào PATH
    echo 'export PATH="$(brew --prefix)/opt/openjdk@17/bin:$PATH"' >> ~/.zprofile
    export PATH="$(brew --prefix)/opt/openjdk@17/bin:$PATH"
    export JAVA_HOME="$(brew --prefix)/opt/openjdk@17"
else
    echo "[2/4] ✓ Java 17 đã có"
fi

# 3. Cài Maven
if ! command -v mvn &> /dev/null; then
    echo ""
    echo "[3/4] Cài đặt Maven..."
    brew install maven
else
    echo "[3/4] ✓ Maven đã có"
fi

# 4. Cài MySQL
if ! command -v mysql &> /dev/null; then
    echo ""
    echo "[4/4] Cài đặt MySQL..."
    brew install mysql
    brew services start mysql
    
    echo ""
    echo "⚠  MySQL đã cài xong. Mật khẩu root mặc định là rỗng."
    echo "   Để đặt mật khẩu root = 'root', chạy:"
    echo "   mysql -u root -e \"ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';\""
    echo ""
else
    echo "[4/4] ✓ MySQL đã có"
    # Đảm bảo MySQL đang chạy
    brew services start mysql 2>/dev/null || true
fi

echo ""
echo "======================================"
echo " KIỂM TRA MÔI TRƯỜNG"
echo "======================================"
echo "Java:  $(java -version 2>&1 | head -1)"
echo "Maven: $(mvn -version 2>&1 | head -1)"
echo "MySQL: $(mysql --version 2>&1)"
echo ""

echo "======================================"
echo " SETUP XONG! CHẠY ỨNG DỤNG"
echo "======================================"
echo ""
echo "  cd \"$(dirname "$0")\""
echo "  mvn clean compile exec:java"
echo ""
echo "  Đăng nhập: admin / admin123"
echo ""
