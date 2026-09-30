package com.library.controller;

import com.library.dao.UserDAO;
import com.library.model.ActivityLog;
import com.library.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class UserController {
    
    private static UserController instance;
    private final UserDAO userDAO;
    
    private UserController() {
        this.userDAO = new UserDAO();
    }
    
    public static synchronized UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }
    
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
    
    public List<User> getUsersByRole(String role) {
        return userDAO.findByRole(role);
    }
    
    public List<User> getUsersByStatus(String status) {
        return userDAO.findByStatus(status);
    }
    
    public List<User> searchUsers(String keyword) {
        return userDAO.search(keyword);
    }
    
    public User getUserById(int id) {
        return userDAO.findById(id);
    }
    
    public boolean createUser(String username, String password, String fullName, String email, String phone, String role) {
        // Validate
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // Check username unique
        if (userDAO.findByUsername(username) != null) {
            return false;
        }
        
        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(hashPassword(password));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus("ACTIVE");
        user.setUpdatedBy(AuthController.getInstance().getCurrentUser().getId());
        
        boolean success = userDAO.insert(user);
        if (success) {
            logActivity("CREATE_USER", "User", user.getId(), "Tạo user: " + username);
        }
        return success;
    }
    
    public boolean updateUser(int userId, String fullName, String email, String phone, String role, String notes) {
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }
        
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setNotes(notes);
        user.setUpdatedBy(AuthController.getInstance().getCurrentUser().getId());
        
        boolean success = userDAO.update(user);
        if (success) {
            logActivity("UPDATE_USER", "User", userId, "Cập nhật user: " + user.getUsername());
        }
        return success;
    }
    
    public boolean changePassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return false;
        }
        
        String hash = hashPassword(newPassword);
        Integer updatedBy = AuthController.getInstance().getCurrentUser().getId();
        boolean success = userDAO.updatePassword(userId, hash, updatedBy);
        
        if (success) {
            User user = userDAO.findById(userId);
            logActivity("CHANGE_PASSWORD", "User", userId, "Đổi mật khẩu: " + user.getUsername());
        }
        return success;
    }
    
    public boolean activateUser(int userId) {
        Integer updatedBy = AuthController.getInstance().getCurrentUser().getId();
        boolean success = userDAO.updateStatus(userId, "ACTIVE", null, updatedBy);
        
        if (success) {
            User user = userDAO.findById(userId);
            logActivity("ACTIVATE_USER", "User", userId, "Kích hoạt user: " + user.getUsername());
        }
        return success;
    }
    
    public boolean deactivateUser(int userId, String reason) {
        Integer updatedBy = AuthController.getInstance().getCurrentUser().getId();
        boolean success = userDAO.updateStatus(userId, "INACTIVE", reason, updatedBy);
        
        if (success) {
            User user = userDAO.findById(userId);
            logActivity("DEACTIVATE_USER", "User", userId, "Vô hiệu hóa user: " + user.getUsername() + " - " + reason);
        }
        return success;
    }
    
    public boolean lockUser(int userId, String reason) {
        boolean success = userDAO.lockAccount(userId, reason);
        
        if (success) {
            User user = userDAO.findById(userId);
            logActivity("LOCK_USER", "User", userId, "Khóa user: " + user.getUsername() + " - " + reason);
        }
        return success;
    }
    
    public List<ActivityLog> getUserActivityLogs(int userId, int limit) {
        return userDAO.getActivityLogs(userId, limit);
    }
    
    public List<ActivityLog> getAllActivityLogs(int limit) {
        return userDAO.getAllActivityLogs(limit);
    }
    
    public void logActivity(String action, String targetEntity, Integer targetId, String details) {
        try {
            int userId = AuthController.getInstance().getCurrentUser().getId();
            userDAO.logActivity(userId, action, targetEntity, targetId, details);
        } catch (Exception e) {
            // Ignore logging errors
        }
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
