package com.library.view;

import com.library.controller.AuthController;
import com.library.model.User;
import com.library.util.AppConstants;
import com.library.util.Icons;
import com.library.view.components.StyledButton;
import com.library.view.components.UiFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Màn hình đăng nhập với gradient background và glassmorphism card
 */
public class LoginView extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private StyledButton loginButton;
    private Timer shakeTimer;
    private final Runnable onLoginSuccess;

    public LoginView(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        initUI();
    }

    private void initUI() {
        setTitle(AppConstants.APP_NAME + " - Đăng nhập");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 23, 42),
                        getWidth(), getHeight(), new Color(30, 27, 75));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());

        // Login card
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Glassmorphism background
                g2.setColor(new Color(30, 41, 59, 220));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                // Border
                g2.setColor(new Color(99, 102, 241, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 20, 20));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(380, 420));

        // Icon / Title
        JLabel iconLabel = new JLabel(Icons.get("book", 48, AppConstants.PRIMARY_LIGHT));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Đăng nhập hệ thống");
        titleLabel.setFont(AppConstants.FONT_TITLE);
        titleLabel.setForeground(AppConstants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Quản lý mua sắm tài liệu thư viện");
        subtitleLabel.setFont(AppConstants.FONT_SMALL);
        subtitleLabel.setForeground(AppConstants.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        JLabel userLabel = new JLabel("Tên đăng nhập");
        userLabel.setFont(AppConstants.FONT_BODY);
        userLabel.setForeground(AppConstants.TEXT_SECONDARY);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        usernameField = createStyledTextField();

        // Password field
        JLabel passLabel = new JLabel("Mật khẩu");
        passLabel.setFont(AppConstants.FONT_BODY);
        passLabel.setForeground(AppConstants.TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        passwordField = new JPasswordField();
        styleField(passwordField);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppConstants.FONT_SMALL);
        errorLabel.setForeground(AppConstants.DANGER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login button
        loginButton = new StyledButton("Đăng nhập");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> doLogin());

        // Assemble card
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(30));
        card.add(userLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(loginButton);

        // Hint
        JLabel hintLabel = new JLabel("Mặc định: admin / admin123");
        hintLabel.setFont(AppConstants.FONT_SMALL);
        hintLabel.setForeground(AppConstants.TEXT_MUTED);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(16));
        card.add(hintLabel);

        mainPanel.add(card);
        setContentPane(mainPanel);

        // Enter ở bất kỳ đâu trong form đều kích hoạt đăng nhập
        getRootPane().setDefaultButton(loginButton);

        // Tự focus ô tên đăng nhập khi mở cửa sổ
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                usernameField.requestFocusInWindow();
            }
        });
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập!");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu!");
            passwordField.requestFocus();
            return;
        }

        // Khóa nút trong lúc xác thực, chạy truy vấn ngoài EDT để không treo UI
        setLoading(true);
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() {
                return AuthController.getInstance().login(username, password);
            }

            @Override
            protected void done() {
                setLoading(false);
                User user;
                try {
                    user = get();
                } catch (Exception ex) {
                    showError("Lỗi kết nối: " + ex.getMessage());
                    return;
                }
                if (user != null) {
                    dispose();
                    onLoginSuccess.run();
                } else {
                    showError("Sai tên đăng nhập hoặc mật khẩu!");
                    passwordField.setText("");
                    passwordField.requestFocus();
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "Đang đăng nhập..." : "Đăng nhập");
        usernameField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
    }

    private void showError(String message) {
        errorLabel.setIcon(Icons.get("alert-triangle", 13, AppConstants.DANGER));
        errorLabel.setIconTextGap(6);
        errorLabel.setText(message);
        // Shake animation (không chồng lặp khi gọi liên tiếp)
        if (shakeTimer != null && shakeTimer.isRunning()) {
            return;
        }
        Point original = getLocation();
        final int[] count = {0};
        shakeTimer = new Timer(30, null);
        shakeTimer.addActionListener(e -> {
            count[0]++;
            if (count[0] >= 8) {
                shakeTimer.stop();
                setLocation(original);
            } else {
                int dx = (count[0] % 2 == 0) ? 5 : -5;
                setLocation(original.x + dx, original.y);
            }
        });
        shakeTimer.start();
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    private void styleField(JTextField field) {
        UiFactory.styleField(field);
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setPreferredSize(new Dimension(300, 38));
    }
}
