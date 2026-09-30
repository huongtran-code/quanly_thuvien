package com.library.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích mã hóa và xác thực mật khẩu SHA-256
 */
public final class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Hash mật khẩu với SHA-256
     */
    public static String hash(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Kiểm tra mật khẩu với hash đã lưu
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        try {
            return hash(plainPassword).equals(hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
