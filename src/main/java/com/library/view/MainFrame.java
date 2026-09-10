package com.library.view;

import com.library.controller.AuthController;
import com.library.util.AppConstants;
import com.library.util.Icons;
import com.library.view.components.StatusBar;
import com.library.view.components.UiFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Cửa sổ chính với Navigation Sidebar và CardLayout (SaaS Dashboard Style)
 */
public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel sidebar;
    private ButtonGroup sidebarGroup;
    private StatusBar statusBar;
    private ScannerPanel scannerPanel;

    public MainFrame() {
        initUI();
    }

    private void initUI() {
        setTitle(AppConstants.APP_NAME);
        setSize(AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 600));

        // Main layout
        setLayout(new BorderLayout());

        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Sidebar Setup
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppConstants.BG_DARK.darker());
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));

        sidebarGroup = new ButtonGroup();

        // Content Area (Cards)
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(AppConstants.BG_DARK);

        // Add panels
        DocumentPanel documentPanel = new DocumentPanel();
        CustomerPurchasePanel customerPanel = new CustomerPurchasePanel();
        scannerPanel = new ScannerPanel(() -> {
            documentPanel.refreshData();
            customerPanel.loadData();
        });

        // Add items to sidebar and content
        addSidebarItem("Tài liệu", "document", documentPanel, "Quản lý tài liệu và danh mục", true);
        addSidebarItem("Nhà cung cấp", "building", new SupplierPanel(), "Quản lý nhà cung cấp", false);
        addSidebarItem("Ngân sách", "wallet", new BudgetPanel(), "Quản lý ngân sách mua sắm", false);
        addSidebarItem("Đơn mua", "package", new PurchaseOrderPanel(), "Quản lý đơn mua hàng", false);
        addSidebarItem("Đề xuất mua", "edit", new SuggestionPanel(), "Đề xuất mua sách từ GV/SV", false);
        addSidebarItem("Báo cáo", "chart", new ReportPanel(), "Báo cáo thống kê chi tiêu", false);
        addSidebarItem("Sách đã mua", "inbox", customerPanel, "Danh sách sách đã xuất kho cho khách", false);
        addSidebarItem("Quét mã", "scan", scannerPanel, "Kết nối điện thoại quét mã vạch", false);
        addSidebarItem("Trợ lý AI", "bot", new AIAssistantPanel(), "Trợ lý AI tư vấn nghiệp vụ", false);

        sidebar.add(Box.createVerticalGlue()); // Push buttons to top

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Status bar
        statusBar = new StatusBar();
        statusBar.updateUser();
        statusBar.setDbStatus(true);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void addSidebarItem(String title, String iconName, JComponent panel, String tooltip, boolean selected) {
        String cardName = title;
        contentPanel.add(panel, cardName);

        JToggleButton btn = new JToggleButton("  " + title);
        btn.setIcon(Icons.get(iconName, 18, AppConstants.TEXT_MUTED));
        btn.setSelectedIcon(Icons.get(iconName, 18, AppConstants.PRIMARY_LIGHT));
        btn.setToolTipText(tooltip);
        btn.setFont(AppConstants.FONT_BODY);
        btn.setForeground(AppConstants.TEXT_MUTED);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(AppConstants.BG_DARK.darker());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(180, 44));

        btn.addItemListener(e -> {
            if (btn.isSelected()) {
                btn.setForeground(AppConstants.TEXT_PRIMARY);
                btn.setBackground(new Color(79, 70, 229, 60)); // Primary tint for active
                cardLayout.show(contentPanel, cardName);
            } else {
                btn.setForeground(AppConstants.TEXT_MUTED);
                btn.setBackground(AppConstants.BG_DARK.darker());
            }
        });

        // Add a subtle hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!btn.isSelected()) btn.setBackground(AppConstants.BG_CARD);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.isSelected()) btn.setBackground(AppConstants.BG_DARK.darker());
            }
        });

        if (selected) {
            btn.setSelected(true);
        }

        sidebarGroup.add(btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(6));
    }

    @Override
    public void dispose() {
        if (scannerPanel != null) {
            scannerPanel.stopServer();
        }
        super.dispose();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppConstants.BG_DARK.darker());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstants.BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        // Left: Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(Icons.get("book", 26, AppConstants.PRIMARY_LIGHT));
        JLabel titleLabel = UiFactory.panelTitle(AppConstants.APP_NAME);

        leftPanel.add(iconLabel);
        leftPanel.add(titleLabel);

        // Right: User info + Logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        String username = AuthController.getInstance().getCurrentUser().getFullName();
        JLabel userLabel = UiFactory.fieldLabel(username);
        userLabel.setIcon(Icons.get("user", 14));
        userLabel.setIconTextGap(6);

        JButton logoutBtn = new JButton("Đăng xuất", Icons.get("logout", 12, AppConstants.DANGER));
        logoutBtn.setFont(AppConstants.FONT_SMALL);
        logoutBtn.setForeground(AppConstants.DANGER);
        logoutBtn.setBackground(AppConstants.BG_DARK);
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConstants.DANGER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> doLogout());

        rightPanel.add(userLabel);
        rightPanel.add(logoutBtn);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private void doLogout() {
        if (UiFactory.confirm(this, "Bạn có chắc muốn đăng xuất?", "Xác nhận")) {
            AuthController.getInstance().logout();
            dispose();
            // Show login again
            SwingUtilities.invokeLater(() -> {
                LoginView loginView = new LoginView(() -> {
                    MainFrame mainFrame = new MainFrame();
                    mainFrame.setVisible(true);
                });
                loginView.setVisible(true);
            });
        }
    }
}
