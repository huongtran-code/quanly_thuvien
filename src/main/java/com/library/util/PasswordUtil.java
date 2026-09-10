package com.library.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích mã hóa và xác thực mật khẩu BCrypt
 */
public final class PasswordUtil {

    private static final int LOG_ROUNDS = 10;

    private PasswordUtil() {}

    /**
     * Hash mật khẩu với BCrypt
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Kiểm tra mật khẩu với hash đã lưu
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
