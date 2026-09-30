package com.library.controller;

import com.library.dao.UserDAO;
import com.library.model.User;
import com.library.util.PasswordUtil;

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

    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        
        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            return null;
        }
        
        // Check if account is locked or inactive
        if ("LOCKED".equals(user.getStatus())) {
            return null;
        }
        if ("INACTIVE".equals(user.getStatus())) {
            return null;
        }
        
        // Verify password
        if (PasswordUtil.verify(password, user.getPasswordHash())) {
            // Update last login
            userDAO.updateLastLogin(user.getId());
            
            // Log activity
            userDAO.logActivity(user.getId(), "LOGIN", null, null, "Đăng nhập thành công");
            
            this.currentUser = user;
            return user;
        } else {
            // Increment failed login attempts
            userDAO.incrementFailedLoginAttempts(user.getId());
            
            // Lock account if too many failed attempts
            if (user.getFailedLoginAttempts() >= 4) { // Lock after 5 failed attempts
                userDAO.lockAccount(user.getId(), "Khóa tự động do đăng nhập sai quá 5 lần");
            }
            
            return null;
        }
    }

    public void logout() {
        if (currentUser != null) {
            userDAO.logActivity(currentUser.getId(), "LOGOUT", null, null, "Đăng xuất");
        }
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
