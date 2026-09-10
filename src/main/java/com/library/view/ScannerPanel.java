package com.library.view;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.library.controller.AuthController;
import com.library.controller.DocumentController;
import com.library.controller.StockController;
import com.library.controller.StockController.ScanResult;
import com.library.model.Category;
import com.library.model.Document;
import com.library.model.StockTransaction;
import com.library.model.User;
import com.library.scanner.ScannerServer;
import com.library.util.AppConstants;
import com.library.util.Icons;
import com.library.view.components.FormPanel;
import com.library.view.components.StyledButton;
import com.library.view.components.StyledTable;
import com.library.view.components.UiFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel kết nối điện thoại quét mã vạch để nhập/xuất kho.
 */
public class ScannerPanel extends JPanel {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final StockController stockController = new StockController();
    private final DocumentController documentController = new DocumentController();
    private final Runnable onStockChanged;

    private ScannerServer server;
    private StyledButton toggleBtn;
    private JLabel statusLabel;
    private JLabel qrLabel;
    private JTextArea urlArea;
    private DefaultTableModel activityModel;
    private StyledTable activityTable;
    private DefaultTableModel historyModel;

    public ScannerPanel(Runnable onStockChanged) {
        this.onStockChanged = onStockChanged;

        setLayout(new BorderLayout(10, 10));
        setBackground(AppConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createToolbar(), BorderLayout.NORTH);
        add(createConnectionCard(), BorderLayout.WEST);
        add(createTablesPanel(), BorderLayout.CENTER);

        loadHistory();
    }

    // ── UI ──

    private JPanel createToolbar() {
        toggleBtn = StyledButton.success("Bật server");
        toggleBtn.setIcon(Icons.button("play"));
        toggleBtn.setPreferredSize(new Dimension(140, 32));
        toggleBtn.addActionListener(e -> toggleServer());

        StyledButton refreshBtn = UiFactory.secondaryButton("Làm mới lịch sử");
        refreshBtn.setIcon(Icons.button("refresh"));
        refreshBtn.addActionListener(e -> loadHistory());

        return UiFactory.toolbar("Quét mã vạch từ điện thoại", Icons.title("scan"),
                refreshBtn, toggleBtn);
    }

    private JPanel createConnectionCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppConstants.BG_CARD);
        card.setPreferredSize(new Dimension(270, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConstants.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel title = new JLabel("Kết nối điện thoại");
        title.setFont(AppConstants.FONT_HEADING);
        title.setForeground(AppConstants.TEXT_PRIMARY);
        title.setIcon(Icons.get("smartphone", 16, AppConstants.PRIMARY_LIGHT));
        title.setIconTextGap(7);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("● Server đang tắt");
        statusLabel.setFont(AppConstants.FONT_SMALL);
        statusLabel.setForeground(AppConstants.TEXT_MUTED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        qrLabel = new JLabel();
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
        qrLabel.setPreferredSize(new Dimension(220, 220));
        qrLabel.setMaximumSize(new Dimension(220, 220));

        urlArea = new JTextArea();
        urlArea.setEditable(false);
        urlArea.setLineWrap(true);
        urlArea.setFont(AppConstants.FONT_MONO.deriveFont(11f));
        urlArea.setBackground(AppConstants.BG_CARD);
        urlArea.setForeground(AppConstants.PRIMARY_LIGHT);
        urlArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        urlArea.setMaximumSize(new Dimension(240, 60));
        urlArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea hint = new JTextArea("""
                1. Bấm "Bật server"
                2. Điện thoại cùng Wi-Fi, quét QR
                3. Chấp nhận cảnh báo chứng chỉ (lần đầu)
                4. Chọn Nhập/Xuất rồi quét mã sách""");
        hint.setEditable(false);
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        hint.setFont(AppConstants.FONT_SMALL);
        hint.setBackground(AppConstants.BG_CARD);
        hint.setForeground(AppConstants.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setMaximumSize(new Dimension(240, 90));

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(qrLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(urlArea);
        card.add(Box.createVerticalStrut(12));
        card.add(hint);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel createTablesPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 12));
        panel.setBackground(AppConstants.BG_DARK);

        // ── Hoạt động phiên này ──
        JPanel activityPanel = new JPanel(new BorderLayout(0, 6));
        activityPanel.setBackground(AppConstants.BG_DARK);

        JPanel activityHeader = new JPanel(new BorderLayout());
        activityHeader.setOpaque(false);
        JLabel activityTitle = new JLabel("Hoạt động phiên này");
        activityTitle.setFont(AppConstants.FONT_HEADING);
        activityTitle.setForeground(AppConstants.TEXT_PRIMARY);
        activityHeader.add(activityTitle, BorderLayout.WEST);

        StyledButton createDocBtn = StyledButton.warning("Tạo tài liệu từ mã chưa có");
        createDocBtn.setIcon(Icons.button("plus"));
        createDocBtn.setPreferredSize(new Dimension(230, 28));
        createDocBtn.addActionListener(e -> createDocumentFromSelected());
        activityHeader.add(createDocBtn, BorderLayout.EAST);

        String[] activityCols = {"Giờ", "Mã vạch", "Chế độ", "SL", "Kết quả"};
        activityModel = new DefaultTableModel(activityCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        activityTable = new StyledTable(activityModel);
        activityTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        activityTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        activityTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        activityTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        activityTable.getColumnModel().getColumn(4).setPreferredWidth(380);

        activityPanel.add(activityHeader, BorderLayout.NORTH);
        activityPanel.add(activityTable.wrapInScrollPane(), BorderLayout.CENTER);

        // ── Lịch sử nhập/xuất ──
        JPanel historyPanel = new JPanel(new BorderLayout(0, 6));
        historyPanel.setBackground(AppConstants.BG_DARK);

        JLabel historyTitle = new JLabel("Lịch sử nhập/xuất kho");
        historyTitle.setFont(AppConstants.FONT_HEADING);
        historyTitle.setForeground(AppConstants.TEXT_PRIMARY);

        String[] historyCols = {"Thời gian", "Tài liệu", "Loại", "SL", "Nguồn", "Người thực hiện"};
        historyModel = new DefaultTableModel(historyCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        StyledTable historyTable = new StyledTable(historyModel);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(280);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(40);

        historyPanel.add(historyTitle, BorderLayout.NORTH);
        historyPanel.add(historyTable.wrapInScrollPane(), BorderLayout.CENTER);

        panel.add(activityPanel);
        panel.add(historyPanel);
        return panel;
    }

    // ── Server lifecycle ──

    private void toggleServer() {
        if (server != null && server.isRunning()) {
            stopServer();
            return;
        }
        try {
            User user = AuthController.getInstance().getCurrentUser();
            Integer userId = user != null ? user.getId() : null;
            server = new ScannerServer(userId, this::onScanEvent);
            String url = server.start();

            statusLabel.setText("● Server đang chạy — cổng " + ScannerServer.PORT);
            statusLabel.setForeground(AppConstants.ACCENT);
            urlArea.setText(url);
            qrLabel.setIcon(new ImageIcon(generateQr(url, 220)));
            toggleBtn.setText("Tắt server");
            toggleBtn.setIcon(Icons.button("stop"));
            toggleBtn.setColors(AppConstants.DANGER, AppConstants.DANGER_DARK);
        } catch (Exception ex) {
            UiFactory.showError(this, "Không khởi động được server!\n" + ex.getMessage()
                    + "\n(Cổng " + ScannerServer.PORT + " có thể đang bị chiếm)");
            server = null;
        }
    }

    /** Dừng server (được MainFrame gọi khi đóng cửa sổ / đăng xuất) */
    public void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
        statusLabel.setText("● Server đang tắt");
        statusLabel.setForeground(AppConstants.TEXT_MUTED);
        qrLabel.setIcon(null);
        urlArea.setText("");
        toggleBtn.setText("Bật server");
        toggleBtn.setIcon(Icons.button("play"));
        toggleBtn.setColors(AppConstants.ACCENT, AppConstants.ACCENT_DARK);
    }

    /** Callback từ ScannerServer (chạy ngoài EDT) */
    private void onScanEvent(String barcode, boolean isImport, int qty, ScanResult result) {
        SwingUtilities.invokeLater(() -> {
            String status = (result.isSuccess() ? "✓ " : "✗ ") + result.message();
            activityModel.insertRow(0, new Object[]{
                    LocalDateTime.now().format(TIME_FMT),
                    barcode,
                    isImport ? "Nhập" : "Xuất",
                    qty,
                    status
            });
            if (result.isSuccess()) {
                loadHistory();
                if (onStockChanged != null) onStockChanged.run();
            }
        });
    }

    // ── Data ──

    private void loadHistory() {
        List<StockTransaction> txs = stockController.getRecentTransactions(100);
        historyModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        for (StockTransaction tx : txs) {
            boolean isImport = StockTransaction.TYPE_IMPORT.equals(tx.getType());
            historyModel.addRow(new Object[]{
                    tx.getCreatedAt() != null ? tx.getCreatedAt().format(fmt) : "",
                    tx.getDocumentTitle(),
                    isImport ? "Nhập kho" : "Xuất kho",
                    Math.abs(tx.getChangeQty()),
                    tx.getSource(),
                    tx.getCreatedByName() != null ? tx.getCreatedByName() : ""
            });
        }
    }

    /** Tạo tài liệu mới với ISBN lấy từ dòng hoạt động được chọn */
    private void createDocumentFromSelected() {
        int row = activityTable.getSelectedRow();
        if (row < 0) {
            UiFactory.showWarning(this, "Hãy chọn một dòng quét trong bảng hoạt động!");
            return;
        }
        String barcode = String.valueOf(activityModel.getValueAt(row, 1));

        JDialog dialog = UiFactory.modalDialog(this, "Tạo tài liệu mới từ mã vạch", 500, 480);

        FormPanel form = new FormPanel(100);
        JTextField titleField = form.addTextField("Tên tài liệu");
        JTextField authorField = form.addTextField("Tác giả");
        JTextField isbnField = form.addTextField("ISBN", barcode);

        List<Category> categories = documentController.getAllCategories();
        String[] catNames = categories.stream().map(Category::getName).toArray(String[]::new);
        JComboBox<String> catCombo = form.addComboBox("Danh mục", catNames);

        JTextField publisherField = form.addTextField("Nhà xuất bản");
        JSpinner yearSpinner = form.addSpinner("Năm XB", 1900, 2100, LocalDateTime.now().getYear());
        JTextField priceField = form.addTextField("Đơn giá (₫)", "0");
        JSpinner stockSpinner = form.addSpinner("Tồn kho", 0, 100000, 0);

        StyledButton saveBtn = StyledButton.success("Lưu");
        saveBtn.setIcon(Icons.button("save"));
        StyledButton cancelBtn = UiFactory.secondaryButton("Hủy");

        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                UiFactory.showError(dialog, "Tên tài liệu không được để trống!");
                titleField.requestFocus();
                return;
            }
            Document d = new Document();
            d.setTitle(title);
            d.setAuthor(authorField.getText().trim());
            d.setIsbn(isbnField.getText().trim());
            if (catCombo.getSelectedIndex() >= 0) {
                d.setCategoryId(categories.get(catCombo.getSelectedIndex()).getId());
            }
            d.setPublisher(publisherField.getText().trim());
            d.setPublishYear((int) yearSpinner.getValue());
            try {
                d.setUnitPrice(Double.parseDouble(priceField.getText().trim().replaceAll("[^0-9]", "")));
            } catch (NumberFormatException ex) {
                UiFactory.showError(dialog, "Đơn giá không hợp lệ!");
                return;
            }
            d.setStockQuantity((int) stockSpinner.getValue());

            String error = documentController.saveDocument(d);
            if (error != null) {
                UiFactory.showError(dialog, error);
            } else {
                dialog.dispose();
                if (onStockChanged != null) onStockChanged.run();
                UiFactory.showInfo(this, "Đã tạo tài liệu \"" + title + "\". Quét lại mã để nhập/xuất kho.");
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setLayout(new BorderLayout());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(UiFactory.dialogButtons(cancelBtn, saveBtn), BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(saveBtn);
        dialog.setVisible(true);
    }

    // ── QR ──

    private BufferedImage generateQr(String content, int size) throws Exception {
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size);
        // Module tối trên nền sáng để dễ quét trong theme dark
        return MatrixToImageWriter.toBufferedImage(matrix,
                new MatrixToImageConfig(0xFF0F172A, 0xFFF8FAFC));
    }
}
