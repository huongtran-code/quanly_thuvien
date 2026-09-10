package com.library.view.components;

import com.library.controller.AuthController;
import com.library.model.User;
import com.library.util.AppConstants;
import com.library.util.Icons;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thanh trạng thái cuối màn hình
 */
public class StatusBar extends JPanel {

    private final JLabel userLabel;
    private final JLabel roleLabel;
    private final JLabel timeLabel;
    private final JLabel dbLabel;

    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_DARK);
        setPreferredSize(new Dimension(0, 30));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppConstants.BORDER));

        // Left panel
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        leftPanel.setOpaque(false);

        userLabel = createLabel("Chưa đăng nhập");
        userLabel.setIcon(Icons.get("user", 12));
        roleLabel = createLabel("");
        dbLabel = createLabel("MySQL");
        dbLabel.setIcon(Icons.get("database", 12));

        leftPanel.add(userLabel);
        leftPanel.add(createSeparator());
        leftPanel.add(roleLabel);
        leftPanel.add(createSeparator());
        leftPanel.add(dbLabel);

        // Right panel
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        rightPanel.setOpaque(false);

        timeLabel = createLabel("");
        timeLabel.setIcon(Icons.get("clock", 12));
        rightPanel.add(timeLabel);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        // Timer cập nhật giờ
        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();
        updateTime();
    }

    /**
     * Cập nhật thông tin user
     */
    public void updateUser() {
        User user = AuthController.getInstance().getCurrentUser();
        if (user != null) {
            userLabel.setText(user.getFullName());
            String roleText = user.isAdmin() ? "ADMIN" : "THỦ THƯ";
            Color roleColor = user.isAdmin() ? AppConstants.WARNING : AppConstants.ACCENT;
            roleLabel.setText(roleText);
            roleLabel.setForeground(roleColor);
        } else {
            userLabel.setText("Chưa đăng nhập");
            roleLabel.setText("");
        }
    }

    public void setDbStatus(boolean connected) {
        dbLabel.setText(connected ? "MySQL ✓" : "MySQL ✗");
        dbLabel.setIcon(Icons.get("database", 12, connected ? AppConstants.ACCENT : AppConstants.DANGER));
        dbLabel.setForeground(connected ? AppConstants.ACCENT : AppConstants.DANGER);
    }

    private void updateTime() {
        timeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")));
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppConstants.FONT_SMALL);
        label.setForeground(AppConstants.TEXT_SECONDARY);
        return label;
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setForeground(AppConstants.BORDER);
        sep.setPreferredSize(new Dimension(1, 16));
        return sep;
    }
}
