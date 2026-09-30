package com.library.view;

import com.library.controller.ReportController;
import com.library.model.Budget;
import com.library.util.AppConstants;
import com.library.util.CurrencyUtil;
import com.library.util.Icons;
import com.library.view.components.StyledButton;
import com.library.view.components.StyledTable;
import com.library.view.components.UiFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

/**
 * Panel báo cáo thống kê với biểu đồ bar chart và bảng tổng hợp
 */
public class ReportPanel extends JPanel {

    private final ReportController controller = new ReportController();
    private JSpinner yearSpinner;
    private JPanel chartPanel;
    private JPanel statsPanel;
    private String currentChart = "monthly"; // monthly, supplier, category, customers, borrows, sales, fines

    public ReportPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(AppConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createToolbar(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadReport();
    }

    private JPanel createToolbar() {
        yearSpinner = new JSpinner(new SpinnerNumberModel(
                controller.getCurrentYear(), 2020, 2100, 1));
        yearSpinner.setFont(AppConstants.FONT_BODY);
        yearSpinner.setPreferredSize(new Dimension(80, 30));
        yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "#"));
        yearSpinner.addChangeListener(e -> loadReport());

        // Back-office reports
        StyledButton monthlyBtn = new StyledButton("Chi tiêu tháng");
        monthlyBtn.setIcon(Icons.button("calendar"));
        monthlyBtn.setPreferredSize(new Dimension(140, 30));
        monthlyBtn.addActionListener(e -> { currentChart = "monthly"; loadReport(); });

        StyledButton supplierBtn = StyledButton.success("Theo NCC");
        supplierBtn.setIcon(Icons.button("building"));
        supplierBtn.setPreferredSize(new Dimension(120, 30));
        supplierBtn.addActionListener(e -> { currentChart = "supplier"; loadReport(); });

        StyledButton categoryBtn = StyledButton.warning("Theo danh mục");
        categoryBtn.setIcon(Icons.button("folder"));
        categoryBtn.setPreferredSize(new Dimension(150, 30));
        categoryBtn.addActionListener(e -> { currentChart = "category"; loadReport(); });

        // Front-office reports
        StyledButton customersBtn = new StyledButton("Khách hàng", new Color(139, 92, 246), new Color(124, 58, 237));
        customersBtn.setIcon(Icons.button("users"));
        customersBtn.setPreferredSize(new Dimension(140, 30));
        customersBtn.addActionListener(e -> { currentChart = "customers"; loadReport(); });

        StyledButton borrowsBtn = new StyledButton("Mượn sách", new Color(236, 72, 153), new Color(219, 39, 119));
        borrowsBtn.setIcon(Icons.button("book-open"));
        borrowsBtn.setPreferredSize(new Dimension(130, 30));
        borrowsBtn.addActionListener(e -> { currentChart = "borrows"; loadReport(); });

        StyledButton salesBtn = new StyledButton("Doanh thu", new Color(34, 197, 94), new Color(22, 163, 74));
        salesBtn.setIcon(Icons.button("shopping-cart"));
        salesBtn.setPreferredSize(new Dimension(130, 30));
        salesBtn.addActionListener(e -> { currentChart = "sales"; loadReport(); });

        StyledButton finesBtn = new StyledButton("Phạt", new Color(249, 115, 22), new Color(234, 88, 12));
        finesBtn.setIcon(Icons.button("alert-circle"));
        finesBtn.setPreferredSize(new Dimension(100, 30));
        finesBtn.addActionListener(e -> { currentChart = "fines"; loadReport(); });

        StyledButton aiBtn = new StyledButton("✨ AI Phân tích", AppConstants.PRIMARY, AppConstants.PRIMARY_DARK);
        aiBtn.setPreferredSize(new Dimension(135, 30));
        aiBtn.addActionListener(e -> showAIAnalysisDialog((int) yearSpinner.getValue()));

        StyledButton refreshBtn = UiFactory.secondaryButton("Cập nhật");
        refreshBtn.setIcon(Icons.button("refresh"));
        refreshBtn.setPreferredSize(new Dimension(115, 30));
        refreshBtn.addActionListener(e -> loadReport());

        JPanel toolbar1 = UiFactory.toolbar("Báo cáo Thống kê", Icons.title("chart"),
                UiFactory.fieldLabel("Năm:"), yearSpinner,
                monthlyBtn, supplierBtn, categoryBtn);
        
        JPanel toolbar2 = UiFactory.toolbar("",
                customersBtn, borrowsBtn, salesBtn, finesBtn,
                (JComponent) Box.createHorizontalStrut(20),
                aiBtn, refreshBtn);
        
        JPanel combined = new JPanel();
        combined.setLayout(new BoxLayout(combined, BoxLayout.Y_AXIS));
        combined.setBackground(AppConstants.BG_DARK);
        combined.add(toolbar1);
        combined.add(Box.createVerticalStrut(5));
        combined.add(toolbar2);
        
        return combined;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 10));
        main.setBackground(AppConstants.BG_DARK);

        // Chart area
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(AppConstants.BG_CARD);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConstants.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        chartPanel.setPreferredSize(new Dimension(0, 350));

        // Stats area
        statsPanel = new JPanel(new BorderLayout(10, 0));
        statsPanel.setBackground(AppConstants.BG_DARK);

        main.add(chartPanel, BorderLayout.CENTER);
        main.add(statsPanel, BorderLayout.SOUTH);

        return main;
    }

    private void loadReport() {
        int year = (int) yearSpinner.getValue();

        // Update chart
        chartPanel.removeAll();
        switch (currentChart) {
            case "monthly" -> chartPanel.add(createMonthlyChart(year), BorderLayout.CENTER);
            case "supplier" -> chartPanel.add(createBarChart(controller.getSpendingBySupplier(year), "Chi tiêu theo Nhà cung cấp - " + year), BorderLayout.CENTER);
            case "category" -> chartPanel.add(createBarChart(controller.getSpendingByCategory(year), "Chi tiêu theo Danh mục - " + year), BorderLayout.CENTER);
            case "customers" -> chartPanel.add(createCustomerStatsPanel(), BorderLayout.CENTER);
            case "borrows" -> chartPanel.add(createBorrowStatsPanel(), BorderLayout.CENTER);
            case "sales" -> chartPanel.add(createSalesChart(year), BorderLayout.CENTER);
            case "fines" -> chartPanel.add(createFineStatsPanel(year), BorderLayout.CENTER);
        }
        chartPanel.revalidate();
        chartPanel.repaint();

        // Update stats
        statsPanel.removeAll();
        if (currentChart.equals("monthly") || currentChart.equals("supplier") || currentChart.equals("category")) {
            statsPanel.add(createOrderStatusPanel(), BorderLayout.WEST);
            statsPanel.add(createBudgetSummaryTable(), BorderLayout.CENTER);
        }
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    /**
     * Biểu đồ cột chi tiêu theo tháng (Java 2D)
     */
    private JPanel createMonthlyChart(int year) {
        Map<Integer, Double> data = controller.getMonthlySpending(year);

        return new JPanel() {
            {
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int padding = 60;
                int chartW = w - padding * 2;
                int chartH = h - padding * 2;

                // Title
                g2.setFont(AppConstants.FONT_HEADING);
                g2.setColor(AppConstants.TEXT_PRIMARY);
                String titleText = "Chi tiêu theo Tháng - " + year;
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(titleText, (w - fm.stringWidth(titleText)) / 2, 25);

                // Find max value
                double maxVal = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
                if (maxVal <= 0) maxVal = 1;

                // Draw axes
                g2.setColor(AppConstants.BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(padding, padding, padding, h - padding);
                g2.drawLine(padding, h - padding, w - padding, h - padding);

                // Draw bars
                int barWidth = chartW / 14;
                String[] months = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};

                for (int i = 0; i < 12; i++) {
                    double value = data.getOrDefault(i + 1, 0.0);
                    int barH = (int) ((value / maxVal) * (chartH - 20));
                    int x = padding + (i + 1) * (chartW / 13) - barWidth / 2;
                    int y = h - padding - barH;

                    // Gradient bar
                    if (value > 0) {
                        GradientPaint gp = new GradientPaint(x, y, AppConstants.PRIMARY,
                                x, h - padding, AppConstants.PRIMARY_DARK);
                        g2.setPaint(gp);
                        g2.fill(new RoundRectangle2D.Float(x, y, barWidth, barH, 4, 4));

                        // Value on top
                        g2.setColor(AppConstants.TEXT_PRIMARY);
                        g2.setFont(AppConstants.FONT_SMALL);
                        String valStr = CurrencyUtil.formatNumber(value);
                        FontMetrics fm2 = g2.getFontMetrics();
                        g2.drawString(valStr, x + (barWidth - fm2.stringWidth(valStr)) / 2, y - 5);
                    }

                    // Month label
                    g2.setColor(AppConstants.TEXT_SECONDARY);
                    g2.setFont(AppConstants.FONT_SMALL);
                    FontMetrics fm3 = g2.getFontMetrics();
                    g2.drawString(months[i], x + (barWidth - fm3.stringWidth(months[i])) / 2, h - padding + 16);
                }

                // Y-axis labels
                g2.setColor(AppConstants.TEXT_MUTED);
                g2.setFont(AppConstants.FONT_SMALL);
                for (int i = 0; i <= 4; i++) {
                    double val = maxVal * i / 4;
                    int y = h - padding - (int) ((val / maxVal) * (chartH - 20));
                    String label = CurrencyUtil.formatNumber(val);
                    g2.drawString(label, 5, y + 4);
                    // Grid line
                    g2.setColor(new Color(71, 85, 105, 50));
                    g2.drawLine(padding + 1, y, w - padding, y);
                    g2.setColor(AppConstants.TEXT_MUTED);
                }

                // No data message
                if (data.isEmpty()) {
                    g2.setFont(AppConstants.FONT_BODY);
                    g2.setColor(AppConstants.TEXT_MUTED);
                    String msg = "Chưa có dữ liệu chi tiêu cho năm " + year;
                    g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, h / 2);
                }
            }
        };
    }

    /**
     * Biểu đồ cột ngang cho dữ liệu key-value
     */
    private JPanel createBarChart(Map<String, Double> data, String title) {
        return new JPanel() {
            {
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int padding = 40;

                // Title
                g2.setFont(AppConstants.FONT_HEADING);
                g2.setColor(AppConstants.TEXT_PRIMARY);
                FontMetrics fmTitle = g2.getFontMetrics();
                g2.drawString(title, (w - fmTitle.stringWidth(title)) / 2, 25);

                if (data.isEmpty()) {
                    g2.setFont(AppConstants.FONT_BODY);
                    g2.setColor(AppConstants.TEXT_MUTED);
                    String msg = "Chưa có dữ liệu";
                    g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, h / 2);
                    return;
                }

                // Horizontal bar chart
                double maxVal = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
                int barHeight = Math.min(35, (h - padding * 2 - 30) / data.size() - 5);
                int labelWidth = 180;
                int chartWidth = w - labelWidth - padding * 2;

                Color[] colors = {AppConstants.PRIMARY, AppConstants.ACCENT, AppConstants.WARNING,
                        new Color(168, 85, 247), new Color(236, 72, 153), new Color(6, 182, 212)};

                int i = 0;
                for (Map.Entry<String, Double> entry : data.entrySet()) {
                    int y = padding + 30 + i * (barHeight + 8);
                    int barW = (int) ((entry.getValue() / maxVal) * chartWidth);

                    // Label
                    g2.setFont(AppConstants.FONT_SMALL);
                    g2.setColor(AppConstants.TEXT_SECONDARY);
                    String label = entry.getKey();
                    if (label.length() > 25) label = label.substring(0, 22) + "...";
                    g2.drawString(label, padding, y + barHeight / 2 + 4);

                    // Bar
                    Color color = colors[i % colors.length];
                    GradientPaint gp = new GradientPaint(labelWidth, y, color,
                            labelWidth + barW, y, color.brighter());
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(labelWidth, y, barW, barHeight, 6, 6));

                    // Value
                    g2.setColor(AppConstants.TEXT_PRIMARY);
                    g2.drawString(CurrencyUtil.format(entry.getValue()),
                            labelWidth + barW + 8, y + barHeight / 2 + 4);

                    i++;
                }
            }
        };
    }

    /**
     * Panel thống kê số đơn theo trạng thái
     */
    private JPanel createOrderStatusPanel() {
        Map<String, Integer> statusCounts = controller.getOrderCountByStatus();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppConstants.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConstants.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        panel.setPreferredSize(new Dimension(220, 180));

        JLabel title = new JLabel("Tổng quan đơn mua");
        title.setFont(AppConstants.FONT_HEADING);
        title.setForeground(AppConstants.TEXT_PRIMARY);
        title.setIcon(Icons.get("list", 15, AppConstants.PRIMARY_LIGHT));
        title.setIconTextGap(7);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));

        String[][] items = {
                {"Nháp", "DRAFT", "●"},
                {"Đã duyệt", "APPROVED", "●"},
                {"Đã nhận", "RECEIVED", "●"},
                {"Đã hủy", "CANCELLED", "●"}
        };

        for (String[] item : items) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(200, 24));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            Color color = AppConstants.getOrderStatusColor(item[1]);
            JLabel label = new JLabel(item[2] + " " + item[0]);
            label.setFont(AppConstants.FONT_BODY);
            label.setForeground(color);

            int count = statusCounts.getOrDefault(item[1], 0);
            JLabel countLabel = new JLabel(String.valueOf(count));
            countLabel.setFont(AppConstants.FONT_HEADING);
            countLabel.setForeground(AppConstants.TEXT_PRIMARY);

            row.add(label, BorderLayout.WEST);
            row.add(countLabel, BorderLayout.EAST);
            panel.add(row);
            panel.add(Box.createVerticalStrut(4));
        }

        // Total
        panel.add(Box.createVerticalStrut(4));
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setMaximumSize(new Dimension(200, 24));
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppConstants.BORDER));

        JLabel totalLabel = new JLabel("Tổng cộng");
        totalLabel.setFont(AppConstants.FONT_HEADING);
        totalLabel.setForeground(AppConstants.TEXT_PRIMARY);

        int total = statusCounts.values().stream().mapToInt(Integer::intValue).sum();
        JLabel totalCount = new JLabel(String.valueOf(total));
        totalCount.setFont(AppConstants.FONT_HEADING);
        totalCount.setForeground(AppConstants.PRIMARY_LIGHT);

        totalRow.add(totalLabel, BorderLayout.WEST);
        totalRow.add(totalCount, BorderLayout.EAST);
        panel.add(totalRow);

        return panel;
    }

    /**
     * Bảng tổng hợp ngân sách
     */
    private JPanel createBudgetSummaryTable() {
        List<Budget> budgets = controller.getBudgetSummary();

        String[] cols = {"Tên ngân sách", "Năm", "Tổng NS", "Đã chi", "Còn lại", "% Chi", "T.Thái"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Budget b : budgets) {
            model.addRow(new Object[]{
                    b.getBudgetName(),
                    b.getFiscalYear(),
                    CurrencyUtil.format(b.getTotalAmount()),
                    CurrencyUtil.format(b.getSpentAmount()),
                    CurrencyUtil.format(b.getRemainingAmount()),
                    String.format("%.1f%%", b.getSpentPercentage()),
                    b.getStatus()
            });
        }

        StyledTable table = new StyledTable(model);
        table.getColumnModel().getColumn(6).setCellRenderer(new StyledTable.StatusRenderer());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppConstants.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel title = new JLabel("Tổng hợp ngân sách");
        title.setFont(AppConstants.FONT_HEADING);
        title.setForeground(AppConstants.TEXT_PRIMARY);
        title.setIcon(Icons.get("briefcase", 15, AppConstants.PRIMARY_LIGHT));
        title.setIconTextGap(7);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        panel.add(title, BorderLayout.NORTH);
        panel.add(table.wrapInScrollPane(), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 180));

        return panel;
    }

    /**
     * Mở hộp thoại phân tích chi tiêu & ngân sách thông minh bằng AI
     */
    private void showAIAnalysisDialog(int year) {
        JDialog dialog = UiFactory.modalDialog(this, "AI Phân tích Chiến lược Chi tiêu & Ngân sách - Năm " + year, 700, 560);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(AppConstants.BG_DARK);

        // Chuẩn bị dữ liệu tổng hợp
        StringBuilder sb = new StringBuilder();
        sb.append("- Năm tài chính: ").append(year).append("\n");

        Map<Integer, Double> monthly = controller.getMonthlySpending(year);
        double totalYearSpending = 0;
        sb.append("- Chi tiêu theo 12 tháng:\n");
        for (int m = 1; m <= 12; m++) {
            double amount = monthly.getOrDefault(m, 0.0);
            if (amount > 0) {
                sb.append(String.format("  + Tháng %d: %s\n", m, CurrencyUtil.format(amount)));
                totalYearSpending += amount;
            }
        }
        sb.append("- Tổng chi tiêu trong năm: ").append(CurrencyUtil.format(totalYearSpending)).append("\n");

        Map<String, Double> bySupplier = controller.getSpendingBySupplier(year);
        if (!bySupplier.isEmpty()) {
            sb.append("- Chi tiêu theo Nhà cung cấp:\n");
            bySupplier.forEach((k, v) -> sb.append(String.format("  + %s: %s\n", k, CurrencyUtil.format(v))));
        }

        Map<String, Double> byCategory = controller.getSpendingByCategory(year);
        if (!byCategory.isEmpty()) {
            sb.append("- Chi tiêu theo Danh mục sách:\n");
            byCategory.forEach((k, v) -> sb.append(String.format("  + %s: %s\n", k, CurrencyUtil.format(v))));
        }

        List<Budget> budgets = controller.getBudgetSummary();
        if (!budgets.isEmpty()) {
            sb.append("- Tình hình các nguồn Ngân sách:\n");
            for (Budget b : budgets) {
                sb.append(String.format("  + %s (Năm %d): Tổng %s, Đã chi %s, Còn lại %s (%.1f%%)\n",
                        b.getBudgetName(), b.getFiscalYear(),
                        CurrencyUtil.format(b.getTotalAmount()),
                        CurrencyUtil.format(b.getSpentAmount()),
                        CurrencyUtil.format(b.getRemainingAmount()),
                        b.getSpentPercentage()));
            }
        }

        String dataSummary = sb.toString();

        // Header info
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppConstants.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstants.BORDER),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        JLabel lblTitle = new JLabel("Báo cáo đánh giá AI tự động", Icons.get("bot", 20, AppConstants.PRIMARY_LIGHT), JLabel.LEFT);
        lblTitle.setFont(AppConstants.FONT_HEADING);
        lblTitle.setForeground(AppConstants.TEXT_PRIMARY);

        JLabel lblStatus = new JLabel("● Đang phân tích...");
        lblStatus.setFont(AppConstants.FONT_SMALL);
        lblStatus.setForeground(AppConstants.WARNING);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblStatus, BorderLayout.EAST);

        // Content viewer
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setBackground(AppConstants.BG_DARK);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setFont(AppConstants.FONT_BODY);
        editorPane.setText("<html><body style='color:#94a3b8; font-family:sans-serif; font-size:13px; padding:16px;'>"
                + "<i>🤖 Đang gửi số liệu sang mô hình AI để xử lý phân tích và dự báo... Vui lòng đợi trong giây lát...</i></body></html>");

        JScrollPane scroll = new JScrollPane(editorPane);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppConstants.BG_DARK);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setOpaque(false);

        StyledButton btnCopy = new StyledButton("Sao chép", AppConstants.BG_CARD_HOVER, AppConstants.BORDER);
        btnCopy.setPreferredSize(new Dimension(110, 32));
        btnCopy.setEnabled(false);

        final String[] rawResultHolder = new String[1];
        btnCopy.addActionListener(e -> {
            if (rawResultHolder[0] != null) {
                java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(rawResultHolder[0]);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                UiFactory.showInfo(dialog, "Đã sao chép nội dung phân tích vào clipboard!");
            }
        });

        StyledButton btnClose = new StyledButton("Đóng", AppConstants.BG_CARD_HOVER, AppConstants.BORDER);
        btnClose.setPreferredSize(new Dimension(80, 32));
        btnClose.addActionListener(e -> dialog.dispose());

        actions.add(btnCopy);
        actions.add(btnClose);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);

        // Worker calling AIService
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return com.library.service.AIService.analyzeBudgetAndReport(dataSummary);
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    rawResultHolder[0] = result;
                    lblStatus.setText("● Hoàn tất");
                    lblStatus.setForeground(AppConstants.ACCENT);
                    btnCopy.setEnabled(true);

                    String formattedHtml = result
                            .replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")
                            .replaceAll("\\*(.*?)\\*", "<i>$1</i>")
                            .replace("\n", "<br/>");

                    editorPane.setText("<html><body style='color:#f8fafc; font-family:sans-serif; font-size:12px; padding:16px; line-height:1.5;'>"
                            + formattedHtml + "</body></html>");
                    editorPane.setCaretPosition(0);
                } catch (Exception ex) {
                    String err = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    lblStatus.setText("● Lỗi phân tích");
                    lblStatus.setForeground(AppConstants.DANGER);
                    editorPane.setText("<html><body style='color:#f87171; font-family:sans-serif; font-size:13px; padding:16px;'>"
                            + "<b>Không thể phân tích dữ liệu:</b><br/>" + err + "</body></html>");
                }
            }
        };
        worker.execute();

        dialog.setVisible(true);
    }
    
    // ==================== FRONT-OFFICE REPORT PANELS ====================
    
    /**
     * Panel thống kê khách hàng
     */
    private JPanel createCustomerStatsPanel() {
        Map<String, Integer> statusCounts = controller.getCustomerCountByStatus();
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppConstants.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Title
        JLabel title = new JLabel("Thống Kê Khách Hàng");
        title.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 24));
        title.setForeground(AppConstants.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        
        // Stats cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(800, 150));
        
        cardsPanel.add(createStatCard("Hoạt động", statusCounts.getOrDefault("ACTIVE", 0), 
                                     AppConstants.ACCENT, "users"));
        cardsPanel.add(createStatCard("Tạm ngưng", statusCounts.getOrDefault("SUSPENDED", 0), 
                                     AppConstants.WARNING, "alert-circle"));
        cardsPanel.add(createStatCard("Không hoạt động", statusCounts.getOrDefault("INACTIVE", 0), 
                                     AppConstants.TEXT_MUTED, "user-x"));
        
        panel.add(cardsPanel);
        panel.add(Box.createVerticalStrut(20));
        
        int total = statusCounts.values().stream().mapToInt(Integer::intValue).sum();
        JLabel totalLabel = new JLabel("Tổng cộng: " + total + " khách hàng");
        totalLabel.setFont(AppConstants.FONT_HEADING);
        totalLabel.setForeground(AppConstants.TEXT_SECONDARY);
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(totalLabel);
        
        return panel;
    }
    
    /**
     * Panel thống kê mượn sách
     */
    private JPanel createBorrowStatsPanel() {
        Map<String, Integer> statusCounts = controller.getBorrowCountByStatus();
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppConstants.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Title
        JLabel title = new JLabel("Thống Kê Mượn/Trả Sách");
        title.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 24));
        title.setForeground(AppConstants.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        
        // Stats cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(800, 150));
        
        cardsPanel.add(createStatCard("Đang mượn", statusCounts.getOrDefault("BORROWED", 0), 
                                     AppConstants.PRIMARY, "book-open"));
        cardsPanel.add(createStatCard("Đã trả", statusCounts.getOrDefault("RETURNED", 0), 
                                     AppConstants.ACCENT, "check-circle"));
        cardsPanel.add(createStatCard("Quá hạn", statusCounts.getOrDefault("OVERDUE", 0), 
                                     AppConstants.DANGER, "alert-triangle"));
        
        panel.add(cardsPanel);
        panel.add(Box.createVerticalStrut(20));
        
        int total = statusCounts.values().stream().mapToInt(Integer::intValue).sum();
        JLabel totalLabel = new JLabel("Tổng cộng: " + total + " phiếu mượn");
        totalLabel.setFont(AppConstants.FONT_HEADING);
        totalLabel.setForeground(AppConstants.TEXT_SECONDARY);
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(totalLabel);
        
        return panel;
    }
    
    /**
     * Biểu đồ doanh thu bán sách theo tháng
     */
    private JPanel createSalesChart(int year) {
        Map<Integer, Double> data = controller.getMonthlySalesRevenue(year);
        
        return new JPanel() {
            {
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int padding = 60;
                int chartW = w - padding * 2;
                int chartH = h - padding * 2;

                // Title
                g2.setFont(AppConstants.FONT_HEADING);
                g2.setColor(AppConstants.TEXT_PRIMARY);
                String titleText = "Doanh Thu Bán Sách - " + year;
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(titleText, (w - fm.stringWidth(titleText)) / 2, 25);

                // Find max value
                double maxVal = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
                if (maxVal <= 0) maxVal = 1;

                // Draw axes
                g2.setColor(AppConstants.BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(padding, padding, padding, h - padding);
                g2.drawLine(padding, h - padding, w - padding, h - padding);

                // Draw bars
                int barWidth = chartW / 14;
                String[] months = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};

                for (int i = 0; i < 12; i++) {
                    double value = data.getOrDefault(i + 1, 0.0);
                    int barH = (int) ((value / maxVal) * (chartH - 20));
                    int x = padding + (i + 1) * (chartW / 13) - barWidth / 2;
                    int y = h - padding - barH;

                    // Gradient bar (green for sales)
                    if (value > 0) {
                        GradientPaint gp = new GradientPaint(x, y, new Color(34, 197, 94),
                                x, h - padding, new Color(22, 163, 74));
                        g2.setPaint(gp);
                        g2.fill(new RoundRectangle2D.Float(x, y, barWidth, barH, 4, 4));

                        // Value on top
                        g2.setColor(AppConstants.TEXT_PRIMARY);
                        g2.setFont(AppConstants.FONT_SMALL);
                        String valStr = CurrencyUtil.formatNumber(value);
                        FontMetrics fm2 = g2.getFontMetrics();
                        g2.drawString(valStr, x + (barWidth - fm2.stringWidth(valStr)) / 2, y - 5);
                    }

                    // Month label
                    g2.setColor(AppConstants.TEXT_SECONDARY);
                    g2.setFont(AppConstants.FONT_SMALL);
                    FontMetrics fm3 = g2.getFontMetrics();
                    g2.drawString(months[i], x + (barWidth - fm3.stringWidth(months[i])) / 2, h - padding + 16);
                }

                // Y-axis labels
                g2.setColor(AppConstants.TEXT_MUTED);
                g2.setFont(AppConstants.FONT_SMALL);
                for (int i = 0; i <= 4; i++) {
                    double val = maxVal * i / 4;
                    int y = h - padding - (int) ((val / maxVal) * (chartH - 20));
                    String label = CurrencyUtil.formatNumber(val);
                    g2.drawString(label, 5, y + 4);
                    // Grid line
                    g2.setColor(new Color(71, 85, 105, 50));
                    g2.drawLine(padding + 1, y, w - padding, y);
                    g2.setColor(AppConstants.TEXT_MUTED);
                }

                // Total revenue
                double total = controller.getTotalSalesRevenue(year);
                g2.setFont(AppConstants.FONT_HEADING);
                g2.setColor(new Color(34, 197, 94));
                String totalStr = "Tổng: " + CurrencyUtil.format(total);
                g2.drawString(totalStr, w - padding - g2.getFontMetrics().stringWidth(totalStr), 25);

                // No data message
                if (data.isEmpty()) {
                    g2.setFont(AppConstants.FONT_BODY);
                    g2.setColor(AppConstants.TEXT_MUTED);
                    String msg = "Chưa có dữ liệu bán sách cho năm " + year;
                    g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, h / 2);
                }
            }
        };
    }
    
    /**
     * Panel thống kê phạt
     */
    private JPanel createFineStatsPanel(int year) {
        Map<String, Integer> statusCounts = controller.getFineCountByStatus();
        double unpaid = controller.getTotalUnpaidFines();
        double paid = controller.getTotalPaidFines(year);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppConstants.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Title
        JLabel title = new JLabel("Thống Kê Phạt - " + year);
        title.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 24));
        title.setForeground(AppConstants.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        
        // Stats cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(800, 150));
        
        cardsPanel.add(createStatCard("Chưa thanh toán", statusCounts.getOrDefault("UNPAID", 0), 
                                     AppConstants.DANGER, "alert-circle"));
        cardsPanel.add(createStatCard("Đã thanh toán", statusCounts.getOrDefault("PAID", 0), 
                                     AppConstants.ACCENT, "check-circle"));
        cardsPanel.add(createStatCard("Miễn phạt", statusCounts.getOrDefault("WAIVED", 0), 
                                     AppConstants.WARNING, "x-circle"));
        
        panel.add(cardsPanel);
        panel.add(Box.createVerticalStrut(30));
        
        // Financial summary
        JPanel financePanel = new JPanel(new GridLayout(1, 2, 40, 0));
        financePanel.setOpaque(false);
        financePanel.setMaximumSize(new Dimension(600, 100));
        
        JPanel unpaidPanel = createFinanceCard("Nợ phạt", unpaid, AppConstants.DANGER);
        JPanel paidPanel = createFinanceCard("Đã thu (" + year + ")", paid, AppConstants.ACCENT);
        
        financePanel.add(unpaidPanel);
        financePanel.add(paidPanel);
        
        panel.add(financePanel);
        
        return panel;
    }
    
    /**
     * Tạo stat card
     */
    private JPanel createStatCard(String label, int count, Color color, String iconName) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppConstants.BG_DARK);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        
        JLabel icon = new JLabel(Icons.get(iconName, 32, color));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(icon);
        card.add(Box.createVerticalStrut(10));
        
        JLabel countLabel = new JLabel(String.valueOf(count));
        countLabel.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 32));
        countLabel.setForeground(color);
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(countLabel);
        card.add(Box.createVerticalStrut(5));
        
        JLabel labelText = new JLabel(label);
        labelText.setFont(AppConstants.FONT_BODY);
        labelText.setForeground(AppConstants.TEXT_SECONDARY);
        labelText.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(labelText);
        
        return card;
    }
    
    /**
     * Tạo finance card
     */
    private JPanel createFinanceCard(String label, double amount, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppConstants.BG_DARK);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        
        JLabel labelText = new JLabel(label);
        labelText.setFont(AppConstants.FONT_BODY);
        labelText.setForeground(AppConstants.TEXT_SECONDARY);
        labelText.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(labelText);
        card.add(Box.createVerticalStrut(8));
        
        JLabel amountLabel = new JLabel(CurrencyUtil.format(amount));
        amountLabel.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 24));
        amountLabel.setForeground(color);
        amountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(amountLabel);
        
        return card;
    }
}

