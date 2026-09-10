package com.library.view;

import com.library.controller.AuthController;
import com.library.controller.BudgetController;
import com.library.controller.DocumentController;
import com.library.controller.PurchaseOrderController;
import com.library.controller.SupplierController;
import com.library.model.*;
import com.library.util.AppConstants;
import com.library.util.CurrencyUtil;
import com.library.util.Icons;
import com.library.view.components.StyledButton;
import com.library.view.components.StyledTable;
import com.library.view.components.UiFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel quản lý đơn mua hàng
 */
public class PurchaseOrderPanel extends JPanel {

    private final PurchaseOrderController orderController = new PurchaseOrderController();
    private final SupplierController supplierController = new SupplierController();
    private final BudgetController budgetController = new BudgetController();
    private final DocumentController documentController = new DocumentController();

    private StyledTable table;
    private DefaultTableModel tableModel;

    /** Column index for checkbox */
    private static final int COL_CHECK = 0;

    public PurchaseOrderPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(AppConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel createToolbar() {
        java.util.List<JComponent> buttons = new ArrayList<>();

        StyledButton createBtn = StyledButton.success("Tạo đơn mua");
        createBtn.setIcon(Icons.button("plus"));
        createBtn.addActionListener(e -> showCreateOrderDialog());

        StyledButton viewBtn = new StyledButton("Chi tiết");
        viewBtn.setIcon(Icons.button("eye"));
        viewBtn.addActionListener(e -> viewOrderDetails());

        buttons.add(createBtn);
        buttons.add(viewBtn);

        if (AuthController.getInstance().isAdmin()) {
            // Single approve
            StyledButton approveBtn = new StyledButton("Duyệt");
            approveBtn.setIcon(Icons.button("check"));
            approveBtn.setColors(AppConstants.ACCENT, AppConstants.ACCENT_DARK);
            approveBtn.addActionListener(e -> changeStatus("approve"));

            StyledButton receiveBtn = StyledButton.success("Nhận hàng");
            receiveBtn.setIcon(Icons.button("inbox"));
            receiveBtn.addActionListener(e -> changeStatus("receive"));

            StyledButton cancelBtn = StyledButton.danger("Hủy đơn");
            cancelBtn.setIcon(Icons.button("x"));
            cancelBtn.addActionListener(e -> changeStatus("cancel"));

            // Bulk actions
            StyledButton bulkApproveBtn = new StyledButton("Duyệt đã chọn");
            bulkApproveBtn.setIcon(Icons.button("check"));
            bulkApproveBtn.setColors(new Color(6, 182, 212), new Color(8, 145, 178));
            bulkApproveBtn.addActionListener(e -> bulkAction("approve"));

            StyledButton bulkReceiveBtn = new StyledButton("Nhận đã chọn");
            bulkReceiveBtn.setIcon(Icons.button("inbox"));
            bulkReceiveBtn.setColors(new Color(34, 197, 94), new Color(22, 163, 74));
            bulkReceiveBtn.addActionListener(e -> bulkAction("receive"));

            buttons.add(approveBtn);
            buttons.add(receiveBtn);
            buttons.add(cancelBtn);
            buttons.add((JComponent) Box.createHorizontalStrut(8));
            buttons.add(bulkApproveBtn);
            buttons.add(bulkReceiveBtn);
        }

        StyledButton exportBtn = UiFactory.secondaryButton("Xuất CSV");
        exportBtn.setIcon(Icons.button("download"));
        exportBtn.addActionListener(e -> {
            if (com.library.util.CsvExporter.exportToCSV(this, tableModel, "don_mua_hang")) {
                UiFactory.showInfo(this, "Đã xuất file CSV thành công!");
            }
        });
        buttons.add(exportBtn);

        StyledButton refreshBtn = UiFactory.secondaryButton("");
        refreshBtn.setIcon(Icons.button("refresh"));
        refreshBtn.setPreferredSize(new Dimension(50, 32));
        refreshBtn.setToolTipText("Làm mới danh sách");
        refreshBtn.addActionListener(e -> loadData());
        buttons.add(refreshBtn);

        return UiFactory.toolbar("Quản lý Đơn mua hàng", Icons.title("package"),
                buttons.toArray(new JComponent[0]));
    }

    private JPanel createTablePanel() {
        String[] columns = {"✓", "ID", "Mã đơn", "Nhà cung cấp", "Ngân sách", "Ngày đặt", "Tổng tiền", "Trạng thái", "Người tạo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == COL_CHECK; // chỉ checkbox được sửa
            }
            @Override
            public Class<?> getColumnClass(int column) {
                return column == COL_CHECK ? Boolean.class : Object.class;
            }
        };
        table = new StyledTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Checkbox column
        table.getColumnModel().getColumn(COL_CHECK).setPreferredWidth(35);
        table.getColumnModel().getColumn(COL_CHECK).setMaxWidth(35);

        // Select All header checkbox
        JTableHeader header = table.getTableHeader();
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = header.columnAtPoint(e.getPoint());
                if (col == COL_CHECK) {
                    toggleSelectAll();
                }
            }
        });

        table.getColumnModel().getColumn(1).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setCellRenderer(new StyledTable.StatusRenderer());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppConstants.BG_DARK);
        panel.add(table.wrapInScrollPane(), BorderLayout.CENTER);
        return panel;
    }

    private void toggleSelectAll() {
        boolean allChecked = true;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (!Boolean.TRUE.equals(tableModel.getValueAt(i, COL_CHECK))) {
                allChecked = false;
                break;
            }
        }
        // Đảo: nếu tất cả đang checked → uncheck hết, ngược lại check hết
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(!allChecked, i, COL_CHECK);
        }
    }

    private void loadData() {
        List<PurchaseOrder> orders = orderController.getAllOrders();
        tableModel.setRowCount(0);
        for (PurchaseOrder po : orders) {
            tableModel.addRow(new Object[]{
                    Boolean.FALSE, // checkbox
                    po.getId(),
                    po.getOrderCode(),
                    po.getSupplierName(),
                    po.getBudgetName(),
                    po.getOrderDate() != null ? po.getOrderDate().toLocalDate().toString() : "",
                    CurrencyUtil.format(po.getTotalAmount()),
                    po.getStatus(),
                    po.getCreatedByName()
            });
        }
    }

    /** Thay đổi trạng thái 1 đơn (chọn bằng row) */
    private void changeStatus(String action) {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiFactory.showWarning(this, "Vui lòng chọn đơn mua!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 1);
        String code = (String) tableModel.getValueAt(row, 2);

        String message = switch (action) {
            case "approve" -> "Duyệt đơn " + code + "?\nNgân sách sẽ bị trừ tương ứng.";
            case "receive" -> "Xác nhận nhận hàng cho đơn " + code + "?\nTồn kho sẽ được cập nhật.";
            case "cancel" -> "Hủy đơn " + code + "?\nNgân sách sẽ được hoàn lại (nếu đã duyệt).";
            default -> "";
        };

        if (UiFactory.confirm(this, message, "Xác nhận")) {
            // Nếu nhận hàng → hỏi thông tin hóa đơn NCC
            if ("receive".equals(action)) {
                showInvoiceDialog(id, code);
            }

            String error = switch (action) {
                case "approve" -> orderController.approveOrder(id);
                case "receive" -> orderController.receiveOrder(id);
                case "cancel" -> orderController.cancelOrder(id);
                default -> "Hành động không hợp lệ";
            };
            if (error != null) {
                UiFactory.showError(this, error);
            } else {
                loadData();
                UiFactory.showInfo(this, "Thao tác thành công!");
            }
        }
    }

    /** Duyệt/nhận hàng loạt các đơn đã tick checkbox */
    private void bulkAction(String action) {
        // Thu thập các đơn đã chọn
        java.util.List<int[]> selected = new ArrayList<>(); // [id, rowIndex]
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, COL_CHECK))) {
                int id = (int) tableModel.getValueAt(i, 1);
                selected.add(new int[]{id, i});
            }
        }

        if (selected.isEmpty()) {
            UiFactory.showWarning(this, "Chưa chọn đơn nào!\nTick vào cột ✓ hoặc bấm tiêu đề ✓ để chọn tất cả.");
            return;
        }

        String actionName = "approve".equals(action) ? "duyệt" : "nhận hàng";
        String msg = "Bạn muốn " + actionName + " " + selected.size() + " đơn đã chọn?";
        if ("approve".equals(action)) {
            msg += "\nNgân sách sẽ bị trừ tương ứng cho từng đơn.";
        } else {
            msg += "\nTồn kho sẽ được cập nhật cho từng đơn.";
        }

        if (!UiFactory.confirm(this, msg, "Xác nhận " + actionName + " hàng loạt")) {
            return;
        }

        int success = 0;
        int fail = 0;
        StringBuilder errors = new StringBuilder();

        for (int[] pair : selected) {
            int id = pair[0];
            String code = (String) tableModel.getValueAt(pair[1], 2);
            String error = "approve".equals(action)
                    ? orderController.approveOrder(id)
                    : orderController.receiveOrder(id);

            if (error == null) {
                success++;
            } else {
                fail++;
                errors.append("• ").append(code).append(": ").append(error).append("\n");
            }
        }

        loadData();

        if (fail == 0) {
            UiFactory.showInfo(this, "✅ Đã " + actionName + " thành công " + success + " đơn!");
        } else {
            String report = "Thành công: " + success + " đơn\nThất bại: " + fail + " đơn\n\nChi tiết lỗi:\n" + errors;
            UiFactory.showWarning(this, report);
        }
    }

    private void viewOrderDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiFactory.showWarning(this, "Vui lòng chọn đơn mua!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 1);
        PurchaseOrder order = orderController.getOrderById(id);
        if (order == null) return;

        JDialog dialog = UiFactory.modalDialog(this,
                "Chi tiết đơn mua: " + order.getOrderCode(), 700, 500);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(AppConstants.BG_CARD);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Order info
        JPanel info = new JPanel(new GridLayout(0, 2, 10, 8));
        info.setBackground(AppConstants.BG_CARD);
        addInfoRow(info, "Mã đơn:", order.getOrderCode());
        addInfoRow(info, "Nhà cung cấp:", order.getSupplierName());
        addInfoRow(info, "Ngân sách:", order.getBudgetName());
        addInfoRow(info, "Ngày đặt:", order.getOrderDate() != null ? order.getOrderDate().toString() : "");
        addInfoRow(info, "Tổng tiền:", CurrencyUtil.format(order.getTotalAmount()));
        addInfoRow(info, "Trạng thái:", order.getStatus());
        addInfoRow(info, "Người tạo:", order.getCreatedByName());
        addInfoRow(info, "Ghi chú:", order.getNotes() != null ? order.getNotes() : "");

        // Items table
        String[] cols = {"Tài liệu", "Số lượng", "Đơn giá", "Thành tiền"};
        DefaultTableModel itemModel = new DefaultTableModel(cols, 0);
        for (PurchaseOrderItem item : order.getItems()) {
            itemModel.addRow(new Object[]{
                    item.getDocumentTitle(),
                    item.getQuantity(),
                    CurrencyUtil.format(item.getUnitPrice()),
                    CurrencyUtil.format(item.getSubtotal())
            });
        }
        StyledTable itemTable = new StyledTable(itemModel);

        content.add(info, BorderLayout.NORTH);
        content.add(itemTable.wrapInScrollPane(), BorderLayout.CENTER);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppConstants.FONT_BODY);
        lbl.setForeground(AppConstants.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(AppConstants.FONT_BODY);
        val.setForeground(AppConstants.TEXT_PRIMARY);

        panel.add(lbl);
        panel.add(val);
    }

    private void showCreateOrderDialog() {
        JDialog dialog = UiFactory.modalDialog(this, "Tạo đơn mua hàng mới", 850, 650);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(AppConstants.BG_CARD);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Top: Order info
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        topPanel.setBackground(AppConstants.BG_CARD);

        List<Supplier> suppliers = supplierController.getActiveSuppliers();
        JComboBox<Supplier> supplierCombo = new JComboBox<>(suppliers.toArray(new Supplier[0]));
        supplierCombo.setFont(AppConstants.FONT_BODY);

        List<Budget> budgets = budgetController.getActiveBudgets();
        JComboBox<Budget> budgetCombo = new JComboBox<>(budgets.toArray(new Budget[0]));
        budgetCombo.setFont(AppConstants.FONT_BODY);

        JPanel supPanel = new JPanel(new BorderLayout(5, 0));
        supPanel.setOpaque(false);
        supPanel.add(UiFactory.fieldLabel("Nhà cung cấp:"), BorderLayout.WEST);
        supPanel.add(supplierCombo, BorderLayout.CENTER);

        JPanel budPanel = new JPanel(new BorderLayout(5, 0));
        budPanel.setOpaque(false);
        budPanel.add(UiFactory.fieldLabel("Ngân sách:"), BorderLayout.WEST);
        budPanel.add(budgetCombo, BorderLayout.CENTER);

        topPanel.add(supPanel);
        topPanel.add(budPanel);

        // Middle: Items
        String[] cols = {"ID", "Tài liệu", "Đơn giá", "Số lượng", "Thành tiền"};
        DefaultTableModel itemModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        StyledTable itemTable = new StyledTable(itemModel);
        List<PurchaseOrderItem> orderItems = new ArrayList<>();

        // Add item controls — two methods: scan ISBN or select from dropdown
        JPanel addItemPanel = new JPanel();
        addItemPanel.setLayout(new BoxLayout(addItemPanel, BoxLayout.Y_AXIS));
        addItemPanel.setBackground(AppConstants.BG_CARD);

        // Row 1: ISBN scan
        JPanel scanRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        scanRow.setBackground(AppConstants.BG_CARD);

        JTextField isbnField = UiFactory.textField(220, "Quét hoặc nhập mã ISBN...");
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        qtySpinner.setPreferredSize(new Dimension(80, 30));

        JLabel totalLabel = new JLabel("Tổng: 0 ₫");
        totalLabel.setFont(AppConstants.FONT_HEADING);
        totalLabel.setForeground(AppConstants.PRIMARY_LIGHT);

        // Lambda to update total
        Runnable updateTotal = () -> {
            double total = orderItems.stream().mapToDouble(PurchaseOrderItem::getSubtotal).sum();
            totalLabel.setText("Tổng: " + CurrencyUtil.format(total));
        };

        // Lambda to add document to order (with duplicate check)
        java.util.function.Consumer<Document> addDocToOrder = (doc) -> {
            if (doc == null) return;

            // ── Tra trùng: kiểm tra đơn mua đang chờ ──
            com.library.dao.PurchaseOrderDAO poDAO = new com.library.dao.PurchaseOrderDAO();
            List<PurchaseOrder> pendingOrders = poDAO.findPendingOrdersForDocument(doc.getId());
            if (!pendingOrders.isEmpty()) {
                StringBuilder warn = new StringBuilder();
                warn.append("⚠ Tài liệu \"").append(doc.getTitle()).append("\" đang có trong:\n");
                for (PurchaseOrder po : pendingOrders) {
                    warn.append("  • ").append(po.getOrderCode()).append(" (").append(po.getStatus()).append(")\n");
                }
                warn.append("\nBạn vẫn muốn thêm?");
                if (!UiFactory.confirm(dialog, warn.toString(), "Cảnh báo trùng")) {
                    return;
                }
            }

            int qty = (int) qtySpinner.getValue();

            PurchaseOrderItem item = new PurchaseOrderItem(doc.getId(), qty, doc.getUnitPrice());
            item.setDocumentTitle(doc.getTitle());
            orderItems.add(item);

            itemModel.addRow(new Object[]{
                    doc.getId(),
                    doc.getTitle(),
                    CurrencyUtil.format(doc.getUnitPrice()),
                    qty,
                    CurrencyUtil.format(item.getSubtotal())
            });

            updateTotal.run();
        };

        StyledButton scanBtn = new StyledButton("Quét thêm");
        scanBtn.setIcon(Icons.button("scan"));
        scanBtn.setPreferredSize(new Dimension(120, 30));
        scanBtn.setColors(AppConstants.WARNING, new Color(217, 119, 6));
        scanBtn.addActionListener(e -> {
            String isbn = isbnField.getText().trim();
            if (isbn.isEmpty()) {
                UiFactory.showWarning(dialog, "Vui lòng nhập mã ISBN!");
                return;
            }
            Document doc = documentController.findByIsbn(isbn);
            if (doc == null) {
                UiFactory.showError(dialog, "Không tìm thấy tài liệu với ISBN: " + isbn);
                return;
            }
            addDocToOrder.accept(doc);
            isbnField.setText("");
            isbnField.requestFocus();
        });

        // Enter key on ISBN field triggers scan
        isbnField.addActionListener(e -> scanBtn.doClick());

        scanRow.add(UiFactory.fieldLabel("📷 ISBN:"));
        scanRow.add(isbnField);
        scanRow.add(UiFactory.fieldLabel("SL:"));
        scanRow.add(qtySpinner);
        scanRow.add(scanBtn);

        // Row 2: Dropdown select (fallback)
        JPanel dropdownRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        dropdownRow.setBackground(AppConstants.BG_CARD);

        List<Document> allDocs = documentController.getAllDocuments();
        JComboBox<Document> docCombo = new JComboBox<>(allDocs.toArray(new Document[0]));
        docCombo.setFont(AppConstants.FONT_BODY);
        docCombo.setPreferredSize(new Dimension(300, 30));

        StyledButton addItemBtn = StyledButton.success("Thêm");
        addItemBtn.setIcon(Icons.button("plus"));
        addItemBtn.setPreferredSize(new Dimension(90, 30));
        addItemBtn.addActionListener(e -> {
            Document selectedDoc = (Document) docCombo.getSelectedItem();
            addDocToOrder.accept(selectedDoc);
        });

        StyledButton removeItemBtn = StyledButton.danger("Xóa dòng");
        removeItemBtn.setIcon(Icons.button("trash"));
        removeItemBtn.setPreferredSize(new Dimension(110, 30));
        removeItemBtn.addActionListener(e -> {
            int row = itemTable.getSelectedRow();
            if (row < 0) {
                UiFactory.showWarning(dialog, "Vui lòng chọn dòng cần xóa!");
                return;
            }
            orderItems.remove(row);
            itemModel.removeRow(row);
            updateTotal.run();
        });

        dropdownRow.add(UiFactory.fieldLabel("Hoặc chọn:"));
        dropdownRow.add(docCombo);
        dropdownRow.add(addItemBtn);
        dropdownRow.add(removeItemBtn);
        dropdownRow.add(Box.createHorizontalStrut(20));
        dropdownRow.add(totalLabel);

        addItemPanel.add(scanRow);
        addItemPanel.add(dropdownRow);

        // Notes
        JPanel notesPanel = new JPanel(new BorderLayout(5, 0));
        notesPanel.setBackground(AppConstants.BG_CARD);
        notesPanel.add(UiFactory.fieldLabel("Ghi chú:"), BorderLayout.WEST);
        JTextField notesField = new JTextField();
        UiFactory.styleField(notesField);
        notesPanel.add(notesField, BorderLayout.CENTER);

        // Bottom buttons
        StyledButton saveBtn = StyledButton.success("Tạo đơn");
        saveBtn.setIcon(Icons.button("save"));
        StyledButton cancelBtn = UiFactory.secondaryButton("Hủy");

        saveBtn.addActionListener(e -> {
            Supplier selectedSupplier = (Supplier) supplierCombo.getSelectedItem();
            Budget selectedBudget = (Budget) budgetCombo.getSelectedItem();
            if (selectedSupplier == null || selectedBudget == null) {
                UiFactory.showError(dialog, "Vui lòng chọn nhà cung cấp và ngân sách!");
                return;
            }
            if (orderItems.isEmpty()) {
                UiFactory.showError(dialog, "Vui lòng thêm ít nhất 1 tài liệu!");
                return;
            }

            PurchaseOrder order = new PurchaseOrder();
            order.setSupplierId(selectedSupplier.getId());
            order.setBudgetId(selectedBudget.getId());
            order.setItems(new ArrayList<>(orderItems));
            order.setNotes(notesField.getText().trim());
            order.setCreatedBy(AuthController.getInstance().getCurrentUser().getId());

            String error = orderController.createOrder(order);
            if (error != null) {
                UiFactory.showError(dialog, error);
            } else {
                dialog.dispose();
                loadData();
                UiFactory.showInfo(this, "Tạo đơn mua thành công!\nMã đơn: " + order.getOrderCode());
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        // Assemble
        JPanel topWrapper = new JPanel(new BorderLayout(0, 10));
        topWrapper.setOpaque(false);
        topWrapper.add(topPanel, BorderLayout.NORTH);
        topWrapper.add(addItemPanel, BorderLayout.CENTER);
        topWrapper.add(notesPanel, BorderLayout.SOUTH);

        content.add(topWrapper, BorderLayout.NORTH);
        content.add(itemTable.wrapInScrollPane(), BorderLayout.CENTER);
        content.add(UiFactory.dialogButtons(cancelBtn, saveBtn), BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    /**
     * Dialog nhập thông tin hóa đơn NCC khi nhận hàng
     */
    private void showInvoiceDialog(int orderId, String orderCode) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 8));
        panel.setBackground(AppConstants.BG_CARD);

        panel.add(UiFactory.fieldLabel("Số hóa đơn NCC:"));
        JTextField invoiceField = UiFactory.textField(200, "Nhập số hóa đơn...");
        panel.add(invoiceField);

        panel.add(UiFactory.fieldLabel("Ngày hóa đơn (dd/MM/yyyy):"));
        JTextField dateField = UiFactory.textField(200, java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        panel.add(dateField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Hóa đơn NCC cho đơn " + orderCode, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String invoiceNum = invoiceField.getText().trim();
            if (!invoiceNum.isEmpty()) {
                java.time.LocalDateTime invoiceDate = null;
                try {
                    java.time.LocalDate date = java.time.LocalDate.parse(dateField.getText().trim(),
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    invoiceDate = date.atStartOfDay();
                } catch (Exception ignored) {}

                new com.library.dao.PurchaseOrderDAO().updateInvoice(orderId, invoiceNum, invoiceDate);
            }
        }
    }
}
