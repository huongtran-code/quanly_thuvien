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
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Left panel - Illustration/Branding
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, AppConstants.PRIMARY,
                    getWidth(), getHeight(), AppConstants.PRIMARY_LIGHT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Decorative circles
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(-50, -50, 300, 300);
                g2.fillOval(getWidth() - 200, getHeight() - 200, 300, 300);
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillOval(100, getHeight() - 150, 200, 200);
            }
        };
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(400, 600));
        
        JPanel brandingBox = new JPanel();
        brandingBox.setLayout(new BoxLayout(brandingBox, BoxLayout.Y_AXIS));
        brandingBox.setOpaque(false);
        
        JLabel brandIcon = new JLabel(Icons.get("book-open", 80, Color.WHITE));
        brandIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel brandTitle = new JLabel("Hệ Thống Thư Viện");
        brandTitle.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 32));
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel brandSubtitle = new JLabel("Quản lý mua sắm tài liệu hiện đại");
        brandSubtitle.setFont(new Font(AppConstants.FONT_FAMILY, Font.PLAIN, 16));
        brandSubtitle.setForeground(new Color(255, 255, 255, 200));
        brandSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        brandingBox.add(brandIcon);
        brandingBox.add(Box.createVerticalStrut(24));
        brandingBox.add(brandTitle);
        brandingBox.add(Box.createVerticalStrut(12));
        brandingBox.add(brandSubtitle);
        
        leftPanel.add(brandingBox);

        // Right panel - Login form
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());
        
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        formContainer.setMaximumSize(new Dimension(400, 500));
        
        // Welcome text
        JLabel welcomeLabel = new JLabel("Chào mừng trở lại!");
        welcomeLabel.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(30, 41, 59));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Đăng nhập để tiếp tục");
        subtitleLabel.setFont(new Font(AppConstants.FONT_FAMILY, Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Username field with icon
        JLabel userLabel = new JLabel("Tên đăng nhập");
        userLabel.setFont(new Font(AppConstants.FONT_FAMILY, Font.PLAIN, 13));
        userLabel.setForeground(new Color(51, 65, 85));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel userFieldPanel = createIconInputField("user", false);
        usernameField = (JTextField) userFieldPanel.getComponent(1);
        
        // Password field with toggle
        JLabel passLabel = new JLabel("Mật khẩu");
        passLabel.setFont(new Font(AppConstants.FONT_FAMILY, Font.PLAIN, 13));
        passLabel.setForeground(new Color(51, 65, 85));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel passFieldPanel = createIconInputField("lock", true);
        passwordField = (JPasswordField) passFieldPanel.getComponent(1);
        
        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font(AppConstants.FONT_FAMILY, Font.PLAIN, 12));
        errorLabel.setForeground(new Color(239, 68, 68));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Login button
        loginButton = new StyledButton("Đăng nhập");
        loginButton.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 15));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> doLogin());
        
        // Hint
        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hintPanel.setOpaque(false);
        hintPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        hintPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel hintIcon = new JLabel(Icons.get("info", 14, new Color(100, 116, 139)));
        JLabel hintText = new JLabel(" Mặc định: admin / admin123");
        hintText.setFont(new Font(AppConstants.FONT_FAMILY, Font.PLAIN, 12));
        hintText.setForeground(new Color(100, 116, 139));
        
        hintPanel.add(hintIcon);
        hintPanel.add(hintText);
        
        // Assemble form
        formContainer.add(welcomeLabel);
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(subtitleLabel);
        formContainer.add(Box.createVerticalStrut(40));
        formContainer.add(userLabel);
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(userFieldPanel);
        formContainer.add(Box.createVerticalStrut(24));
        formContainer.add(passLabel);
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(passFieldPanel);
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(errorLabel);
        formContainer.add(Box.createVerticalStrut(24));
        formContainer.add(loginButton);
        formContainer.add(Box.createVerticalStrut(20));
        formContainer.add(hintPanel);
        
        rightPanel.add(formContainer);

        // Add panels to main
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
        getRootPane().setDefaultButton(loginButton);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                usernameField.requestFocusInWindow();
            }
        });
    }
    
    private JPanel createIconInputField(String iconName, boolean isPassword) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel icon = new JLabel(Icons.get(iconName, 18, new Color(100, 116, 139)));
        icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        panel.add(icon, BorderLayout.WEST);
        
        JTextField field;
        if (isPassword) {
            field = new JPasswordField();
            ((JPasswordField) field).setEchoChar('•');
        } else {
            field = new JTextField();
        }
        
        field.setFont(new Font(AppConstants.FONT_FAMILY, Font.PLAIN, 14));
        field.setForeground(new Color(30, 41, 59));
        field.setCaretColor(AppConstants.PRIMARY);
        field.setBackground(new Color(248, 250, 252));
        field.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.add(field, BorderLayout.CENTER);
        
        if (isPassword) {
            JButton toggleBtn = new JButton(Icons.get("eye", 18, new Color(148, 163, 184)));
            toggleBtn.setContentAreaFilled(false);
            toggleBtn.setBorderPainted(false);
            toggleBtn.setFocusPainted(false);
            toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            toggleBtn.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            toggleBtn.addActionListener(e -> {
                JPasswordField pf = (JPasswordField) field;
                if (pf.getEchoChar() == '\u0000') {
                    pf.setEchoChar('•');
                    toggleBtn.setIcon(Icons.get("eye", 18, new Color(148, 163, 184)));
                } else {
                    pf.setEchoChar('\u0000');
                    toggleBtn.setIcon(Icons.get("eye-off", 18, AppConstants.PRIMARY));
                }
            });
            panel.add(toggleBtn, BorderLayout.EAST);
        }
        
        return panel;
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
        errorLabel.setIcon(Icons.get("alert-circle", 14, new Color(248, 113, 113)));
        errorLabel.setIconTextGap(6);
        errorLabel.setText(message);
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
}
