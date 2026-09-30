package com.library.view;

import com.library.controller.UserController;
import com.library.model.ActivityLog;
import com.library.model.User;
import com.library.util.AppConstants;
import com.library.util.Icons;
import com.library.view.components.StyledButton;
import com.library.view.components.StyledTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserPanel extends JPanel {

    private StyledTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> roleFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private final UserController userController;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public UserPanel() {
        this.userController = UserController.getInstance();
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(AppConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Quản lý nhân sự");
        titleLabel.setFont(AppConstants.FONT_TITLE);
        titleLabel.setForeground(AppConstants.TEXT_PRIMARY);
        titleLabel.setIcon(Icons.get("users", 24, AppConstants.PRIMARY));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        
        StyledButton addBtn = new StyledButton("Thêm nhân viên");
        addBtn.setIcon(Icons.get("user-plus", 16, Color.WHITE));
        addBtn.addActionListener(e -> showAddUserDialog());
        
        StyledButton editBtn = new StyledButton("Sửa");
        editBtn.setIcon(Icons.get("edit", 16, Color.WHITE));
        editBtn.setBackground(new Color(59, 130, 246));
        editBtn.addActionListener(e -> editSelectedUser());
        
        StyledButton deleteBtn = new StyledButton("Ngừng hoạt động");
        deleteBtn.setIcon(Icons.get("user-x", 16, Color.WHITE));
        deleteBtn.setBackground(new Color(239, 68, 68));
        deleteBtn.addActionListener(e -> deactivateSelectedUser());
        
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(btnPanel, BorderLayout.EAST);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setFont(AppConstants.FONT_BODY);
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm...");
        searchField.addActionListener(e -> loadData());

        StyledButton searchBtn = new StyledButton("Tìm");
        searchBtn.setIcon(Icons.get("search", 14, Color.WHITE));
        searchBtn.addActionListener(e -> loadData());

        roleFilterCombo = new JComboBox<>(new String[]{"Tất cả", "ADMIN", "LIBRARIAN"});
        roleFilterCombo.setFont(AppConstants.FONT_BODY);
        roleFilterCombo.addActionListener(e -> loadData());

        statusFilterCombo = new JComboBox<>(new String[]{"Tất cả", "ACTIVE", "INACTIVE", "LOCKED"});
        statusFilterCombo.setFont(AppConstants.FONT_BODY);
        statusFilterCombo.addActionListener(e -> loadData());

        toolbar.add(new JLabel("Tìm kiếm:"));
        toolbar.add(searchField);
        toolbar.add(searchBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("Vai trò:"));
        toolbar.add(roleFilterCombo);
        toolbar.add(new JLabel("Trạng thái:"));
        toolbar.add(statusFilterCombo);

        // Table
        String[] columns = {"ID", "Username", "Họ tên", "Email", "Vai trò", "Trạng thái", "Đăng nhập cuối"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new StyledTable(tableModel);

        // Context menu
        JPopupMenu contextMenu = new JPopupMenu();
        
        JMenuItem editItem = new JMenuItem("Sửa thông tin", Icons.get("edit", 14, AppConstants.TEXT_PRIMARY));
        editItem.addActionListener(e -> editSelectedUser());
        
        JMenuItem changePasswordItem = new JMenuItem("Đổi mật khẩu", Icons.get("key", 14, AppConstants.TEXT_PRIMARY));
        changePasswordItem.addActionListener(e -> changePasswordForSelectedUser());
        
        JMenuItem viewLogsItem = new JMenuItem("Xem lịch sử", Icons.get("clock", 14, AppConstants.TEXT_PRIMARY));
        viewLogsItem.addActionListener(e -> viewUserLogs());
        
        JMenuItem activateItem = new JMenuItem("Kích hoạt", Icons.get("check-circle", 14, new Color(34, 197, 94)));
        activateItem.addActionListener(e -> activateSelectedUser());
        
        JMenuItem deactivateItem = new JMenuItem("Vô hiệu hóa", Icons.get("x-circle", 14, new Color(239, 68, 68)));
        deactivateItem.addActionListener(e -> deactivateSelectedUser());
        
        contextMenu.add(editItem);
        contextMenu.add(changePasswordItem);
        contextMenu.addSeparator();
        contextMenu.add(viewLogsItem);
        contextMenu.addSeparator();
        contextMenu.add(activateItem);
        contextMenu.add(deactivateItem);
        
        table.setComponentPopupMenu(contextMenu);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedUser();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(toolbar, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        
        List<User> users;
        String searchText = searchField.getText().trim();
        String roleFilter = (String) roleFilterCombo.getSelectedItem();
        String statusFilter = (String) statusFilterCombo.getSelectedItem();
        
        if (!searchText.isEmpty()) {
            users = userController.searchUsers(searchText);
        } else if (!"Tất cả".equals(roleFilter)) {
            users = userController.getUsersByRole(roleFilter);
        } else if (!"Tất cả".equals(statusFilter)) {
            users = userController.getUsersByStatus(statusFilter);
        } else {
            users = userController.getAllUsers();
        }
        
        for (User user : users) {
            String lastLogin = user.getLastLogin() != null ? user.getLastLogin().format(formatter) : "Chưa đăng nhập";
            tableModel.addRow(new Object[]{
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail() != null ? user.getEmail() : "",
                user.getRole(),
                user.getStatus(),
                lastLogin
            });
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm nhân viên", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"LIBRARIAN", "ADMIN"});

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Mật khẩu:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Họ tên:"));
        formPanel.add(fullNameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Số điện thoại:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Vai trò:"));
        formPanel.add(roleCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        StyledButton saveBtn = new StyledButton("Lưu");
        StyledButton cancelBtn = new StyledButton("Hủy");
        cancelBtn.setBackground(AppConstants.TEXT_MUTED);

        saveBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu phải có ít nhất 6 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = userController.createUser(username, password, fullName, email, phone, role);
            if (success) {
                JOptionPane.showMessageDialog(dialog, "Thêm nhân viên thành công!");
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Thêm nhân viên thất bại! Username có thể đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa!");
            return;
        }

        int userId = (int) tableModel.getValueAt(row, 0);
        User user = userController.getUserById(userId);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa thông tin nhân viên", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField fullNameField = new JTextField(user.getFullName());
        JTextField emailField = new JTextField(user.getEmail() != null ? user.getEmail() : "");
        JTextField phoneField = new JTextField(user.getPhone() != null ? user.getPhone() : "");
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"LIBRARIAN", "ADMIN"});
        roleCombo.setSelectedItem(user.getRole());
        JTextArea notesArea = new JTextArea(user.getNotes() != null ? user.getNotes() : "", 3, 20);
        notesArea.setLineWrap(true);

        formPanel.add(new JLabel("Username:"));
        formPanel.add(new JLabel(user.getUsername()));
        formPanel.add(new JLabel("Họ tên:"));
        formPanel.add(fullNameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Số điện thoại:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Vai trò:"));
        formPanel.add(roleCombo);
        formPanel.add(new JLabel("Ghi chú:"));
        formPanel.add(new JScrollPane(notesArea));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        StyledButton saveBtn = new StyledButton("Lưu");
        StyledButton cancelBtn = new StyledButton("Hủy");
        cancelBtn.setBackground(AppConstants.TEXT_MUTED);

        saveBtn.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();
            String notes = notesArea.getText().trim();

            if (fullName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập họ tên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = userController.updateUser(userId, fullName, email, phone, role, notes);
            if (success) {
                JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!");
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void changePasswordForSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!");
            return;
        }

        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Mật khẩu mới:"));
        panel.add(passwordField);
        panel.add(new JLabel("Xác nhận:"));
        panel.add(confirmField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Đổi mật khẩu cho " + username, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 6 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = userController.changePassword(userId, password);
            if (success) {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void activateSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!");
            return;
        }

        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Kích hoạt tài khoản " + username + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = userController.activateUser(userId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Kích hoạt tài khoản thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Kích hoạt thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deactivateSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!");
            return;
        }

        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        String reason = JOptionPane.showInputDialog(this, "Lý do vô hiệu hóa tài khoản " + username + ":");
        if (reason != null && !reason.trim().isEmpty()) {
            boolean success = userController.deactivateUser(userId, reason);
            if (success) {
                JOptionPane.showMessageDialog(this, "Vô hiệu hóa tài khoản thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Vô hiệu hóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewUserLogs() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!");
            return;
        }

        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        List<ActivityLog> logs = userController.getUserActivityLogs(userId, 100);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Lịch sử hoạt động: " + username, true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);

        String[] columns = {"Thời gian", "Hành động", "Đối tượng", "Chi tiết"};
        DefaultTableModel logModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (ActivityLog log : logs) {
            String target = log.getTargetEntity() != null ? log.getTargetEntity() + " #" + log.getTargetId() : "";
            logModel.addRow(new Object[]{
                log.getCreatedAt().format(formatter),
                log.getAction(),
                target,
                log.getDetails() != null ? log.getDetails() : ""
            });
        }

        StyledTable logTable = new StyledTable(logModel);
        JScrollPane scrollPane = new JScrollPane(logTable);

        dialog.add(scrollPane);
        dialog.setVisible(true);
    }
}
