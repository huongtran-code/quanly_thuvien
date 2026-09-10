package com.library.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility: Sinh BCrypt hash cho mật khẩu - chạy 1 lần để lấy hash cho schema.sql
 * Chạy: mvn -q compile exec:java -Dexec.mainClass="com.library.util.PasswordHashGen"
 */
public class PasswordHashGen {
    public static void main(String[] args) {
        String password = "admin123";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
    }
}
