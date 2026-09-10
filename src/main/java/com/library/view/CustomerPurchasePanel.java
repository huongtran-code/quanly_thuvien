package com.library.view;

import com.library.controller.StockController;
import com.library.model.StockTransaction;
import com.library.util.AppConstants;
import com.library.util.Icons;
import com.library.view.components.StyledButton;
import com.library.view.components.StyledTable;
import com.library.view.components.UiFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel hiển thị sách đã xuất kho (đã mua) cho khách hàng xem.
 * Thiết kế premium với thẻ thống kê, bảng rõ ràng, phù hợp quầy phục vụ.
 */
public class CustomerPurchasePanel extends JPanel {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final StockController stockController = new StockController();
    private DefaultTableModel tableModel;
    private StyledTable table;

    // Stat cards
    private JLabel statTotalQty;
    private JLabel statTotalTx;
    private JLabel statToday;
    private JLabel lastUpdateLabel;

    public CustomerPurchasePanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(AppConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createToolbar(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadData();
    }

    // ── Toolbar ──

    private JPanel createToolbar() {
        StyledButton exportBtn = UiFactory.secondaryButton("Xuất CSV");
        exportBtn.setIcon(Icons.button("download"));
        exportBtn.addActionListener(e -> {
            if (com.library.util.CsvExporter.exportToCSV(this, tableModel, "sach_da_mua")) {
                com.library.view.components.UiFactory.showInfo(this, "Đã xuất file CSV thành công!");
            }
        });

        StyledButton refreshBtn = UiFactory.secondaryButton("Làm mới");
        refreshBtn.setIcon(Icons.button("refresh"));
        refreshBtn.addActionListener(e -> loadData());

        return UiFactory.toolbar("Sách đã mua / Xuất kho", Icons.title("inbox"), exportBtn, refreshBtn);
    }

    // ── Main content ──

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setOpaque(false);

        main.add(createStatsRow(), BorderLayout.NORTH);
        main.add(createTableCard(), BorderLayout.CENTER);
        main.add(createFooter(), BorderLayout.SOUTH);

        return main;
    }

    // ── Stat cards row ──

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 100));

        // Card 1: Tổng sách đã xuất
        statTotalQty = new JLabel("0");
        JPanel card1 = createStatCard(
                "Tổng sách đã xuất",
                statTotalQty,
                new Color(99, 102, 241),   // Indigo
                new Color(129, 140, 248),
                "package"
        );

        // Card 2: Số giao dịch
        statTotalTx = new JLabel("0");
        JPanel card2 = createStatCard(
                "Số giao dịch xuất",
                statTotalTx,
                new Color(16, 185, 129),   // Emerald
                new Color(52, 211, 153),
                "list"
        );

        // Card 3: Hôm nay
        statToday = new JLabel("0");
        JPanel card3 = createStatCard(
                "Xuất hôm nay",
                statToday,
                new Color(245, 158, 11),   // Amber
                new Color(251, 191, 36),
                "calendar"
        );

        row.add(card1);
        row.add(card2);
        row.add(card3);
        return row;
    }

    /**
     * Tạo thẻ thống kê với gradient background, icon, số lớn
     */
    private JPanel createStatCard(String title, JLabel valueLabel,
                                   Color colorFrom, Color colorTo, String iconName) {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Rounded gradient background
                GradientPaint gp = new GradientPaint(
                        0, 0, colorFrom,
                        getWidth(), getHeight(), colorTo.darker()
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

                // Subtle glass highlight on top half
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() / 2f, 16, 16));

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // Left: Icon
        JLabel icon = new JLabel(Icons.get(iconName, 30, new Color(255, 255, 255, 180)));
        card.add(icon, BorderLayout.WEST);

        // Center: Title + Value
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppConstants.FONT_SMALL.deriveFont(Font.PLAIN, 12f));
        titleLabel.setForeground(new Color(255, 255, 255, 180));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(valueLabel);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // ── Table card ──

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(AppConstants.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConstants.BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel tableTitle = new JLabel("  Lịch sử xuất kho gần đây");
        tableTitle.setFont(AppConstants.FONT_HEADING.deriveFont(15f));
        tableTitle.setForeground(AppConstants.TEXT_PRIMARY);
        tableTitle.setIcon(Icons.get("clock", 16, AppConstants.PRIMARY_LIGHT));
        tableTitle.setIconTextGap(8);
        header.add(tableTitle, BorderLayout.WEST);

        card.add(header, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "STT", "Thời gian", "Tên sách", "Số lượng", "Nguồn", "Người thực hiện"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new StyledTable(tableModel);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(350);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);

        // Center align STT and SL
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setBackground(AppConstants.BG_CARD);
        centerRenderer.setForeground(AppConstants.TEXT_PRIMARY);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        // Qty column with accent color
        DefaultTableCellRenderer qtyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(AppConstants.FONT_HEADING.deriveFont(14f));
                if (!isSelected) {
                    setForeground(AppConstants.ACCENT);
                    setBackground(row % 2 == 0 ? AppConstants.BG_CARD : AppConstants.BG_ROW_ALT);
                }
                return this;
            }
        };
        table.getColumnModel().getColumn(3).setCellRenderer(qtyRenderer);

        // Larger font for readability
        table.setFont(AppConstants.FONT_BODY.deriveFont(13.5f));
        table.setRowHeight(42);

        card.add(table.wrapInScrollPane(), BorderLayout.CENTER);
        return card;
    }

    // ── Footer ──

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConstants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        footer.setPreferredSize(new Dimension(0, 42));

        // Left: hint
        JLabel hint = new JLabel("Dữ liệu tự cập nhật khi xuất kho qua quét mã vạch");
        hint.setFont(AppConstants.FONT_SMALL);
        hint.setForeground(AppConstants.TEXT_MUTED);
        hint.setIcon(Icons.get("scan", 12, AppConstants.TEXT_MUTED));
        hint.setIconTextGap(6);
        footer.add(hint, BorderLayout.WEST);

        // Right: last update time
        lastUpdateLabel = new JLabel();
        lastUpdateLabel.setFont(AppConstants.FONT_SMALL);
        lastUpdateLabel.setForeground(AppConstants.TEXT_MUTED);
        footer.add(lastUpdateLabel, BorderLayout.EAST);

        return footer;
    }

    // ── Data ──

    public void loadData() {
        List<StockTransaction> exports = stockController.getRecentExports(200);
        tableModel.setRowCount(0);

        int totalQty = 0;
        int todayQty = 0;
        LocalDate today = LocalDate.now();
        int stt = 1;

        for (StockTransaction tx : exports) {
            int qty = Math.abs(tx.getChangeQty());
            totalQty += qty;

            if (tx.getCreatedAt() != null && tx.getCreatedAt().toLocalDate().equals(today)) {
                todayQty += qty;
            }

            tableModel.addRow(new Object[]{
                    stt++,
                    tx.getCreatedAt() != null ? tx.getCreatedAt().format(DATETIME_FMT) : "",
                    tx.getDocumentTitle(),
                    qty,
                    tx.getSource(),
                    tx.getCreatedByName() != null ? tx.getCreatedByName() : ""
            });
        }

        // Update stats
        statTotalQty.setText(String.valueOf(totalQty));
        statTotalTx.setText(String.valueOf(exports.size()));
        statToday.setText(String.valueOf(todayQty));
        lastUpdateLabel.setText("Cập nhật: " + LocalDateTime.now().format(TIME_FMT));
    }
}
