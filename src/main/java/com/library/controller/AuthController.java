package com.library.controller;

import com.library.dao.UserDAO;
import com.library.model.User;
import com.library.util.PasswordUtil;

/**
 * Controller: Xác thực và quản lý phiên đăng nhập
 */
public class AuthController {

    private static AuthController instance;
    private final UserDAO userDAO = new UserDAO();
    private User currentUser;

    private AuthController() {}

    public static synchronized AuthController getInstance() {
        if (instance == null) {
            instance = new AuthController();
        }
        return instance;
    }

    /**
     * Đăng nhập
     * @return User nếu thành công, null nếu thất bại
     */
    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        User user = userDAO.findByUsername(username.trim());
        if (user != null && PasswordUtil.verify(password, user.getPasswordHash())) {
            this.currentUser = user;
            return user;
        }
        return null;
    }

    /**
     * Đăng xuất
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Lấy user đang đăng nhập
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Kiểm tra đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Kiểm tra có phải admin không
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
