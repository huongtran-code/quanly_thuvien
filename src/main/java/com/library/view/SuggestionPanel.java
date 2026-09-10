package com.library.view;

import com.library.controller.AuthController;
import com.library.controller.SuggestionController;
import com.library.model.PurchaseSuggestion;
import com.library.util.AppConstants;
import com.library.util.CsvExporter;
import com.library.util.Icons;
import com.library.view.components.FormPanel;
import com.library.view.components.StyledButton;
import com.library.view.components.StyledTable;
import com.library.view.components.UiFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel quản lý đề xuất mua sách từ GV/SV.
 * - Mọi người đều có thể tạo đề xuất.
 * - Chỉ ADMIN duyệt/từ chối đề xuất.
 */
public class SuggestionPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SuggestionController controller = new SuggestionController();
    private StyledTable table;
    private DefaultTableModel tableModel;

    public SuggestionPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(AppConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel createToolbar() {
        List<JComponent> buttons = new ArrayList<>();

        StyledButton createBtn = StyledButton.success("Tạo đề xuất");
        createBtn.setIcon(Icons.button("plus"));
        createBtn.addActionListener(e -> showCreateDialog());
        buttons.add(createBtn);

        if (AuthController.getInstance().isAdmin()) {
            StyledButton approveBtn = new StyledButton("Duyệt");
            approveBtn.setIcon(Icons.button("check"));
            approveBtn.setColors(AppConstants.ACCENT, AppConstants.ACCENT_DARK);
            approveBtn.addActionListener(e -> reviewSelected("APPROVED"));

            StyledButton rejectBtn = StyledButton.danger("Từ chối");
            rejectBtn.setIcon(Icons.button("x"));
            rejectBtn.addActionListener(e -> reviewSelected("REJECTED"));

            StyledButton deleteBtn = StyledButton.warning("Xóa");
            deleteBtn.setIcon(Icons.button("trash"));
            deleteBtn.addActionListener(e -> deleteSelected());

            buttons.add(approveBtn);
            buttons.add(rejectBtn);
            buttons.add(deleteBtn);
        }

        StyledButton exportBtn = UiFactory.secondaryButton("Xuất CSV");
        exportBtn.setIcon(Icons.button("download"));
        exportBtn.addActionListener(e -> {
            if (CsvExporter.exportToCSV(this, tableModel, "de_xuat_mua_sach")) {
                UiFactory.showInfo(this, "Đã xuất file CSV thành công!");
            }
        });
        buttons.add(exportBtn);

        StyledButton refreshBtn = UiFactory.secondaryButton("");
        refreshBtn.setIcon(Icons.button("refresh"));
        refreshBtn.setPreferredSize(new Dimension(50, 32));
        refreshBtn.addActionListener(e -> loadData());
        buttons.add(refreshBtn);

        return UiFactory.toolbar("Đề xuất Mua sách", Icons.title("edit"),
                buttons.toArray(new JComponent[0]));
    }

    private JPanel createTablePanel() {
        String[] columns = {
                "ID", "Tên sách", "Tác giả", "ISBN", "NXB", "SL",
                "Lý do", "Người đề xuất", "Trạng thái", "Ngày tạo", "Ghi chú duyệt"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(35);
        table.getColumnModel().getColumn(5).setMaxWidth(50);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);
        table.getColumnModel().getColumn(8).setCellRenderer(new StyledTable.StatusRenderer());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppConstants.BG_DARK);
        panel.add(table.wrapInScrollPane(), BorderLayout.CENTER);
        return panel;
    }

    private void loadData() {
        List<PurchaseSuggestion> list = controller.getAll();
        tableModel.setRowCount(0);
        for (PurchaseSuggestion s : list) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getBookTitle(),
                    s.getAuthor(),
                    s.getIsbn(),
                    s.getPublisher(),
                    s.getQuantity(),
                    s.getReason(),
                    s.getSuggestedBy(),
                    s.getStatus(),
                    s.getCreatedAt() != null ? s.getCreatedAt().format(DATE_FMT) : "",
                    s.getReviewNote() != null ? s.getReviewNote() : ""
            });
        }
    }

    private void showCreateDialog() {
        JDialog dialog = UiFactory.modalDialog(this, "Tạo đề xuất mua sách", 500, 450);

        FormPanel form = new FormPanel();
        final JButton[] aiBtnHolder = new JButton[1];
        JTextField titleField = form.addTextFieldWithButton(
                "Tên sách *",
                "",
                "🪄 AI Gợi ý",
                null,
                btn -> {
                    aiBtnHolder[0] = btn;
                    btn.setToolTipText("AI tự động tìm tác giả, nhà xuất bản, ISBN và gợi ý lý do đề xuất");
                }
        );

        JTextField authorField = form.addTextField("Tác giả");
        JTextField isbnField = form.addTextField("ISBN");
        JTextField publisherField = form.addTextField("NXB");
        JSpinner qtySpinner = form.addSpinner("Số lượng", 1, 999, 1);
        JTextArea reasonArea = form.addTextArea("Lý do đề xuất", 3);
        JTextField suggestedByField = form.addTextField("Người đề xuất *");

        // Nếu đã đăng nhập, auto-fill tên
        suggestedByField.setText(AuthController.getInstance().getCurrentUser().getFullName());

        if (aiBtnHolder[0] != null) {
            aiBtnHolder[0].addActionListener(e -> {
                String query = titleField.getText().trim();
                if (query.isEmpty()) {
                    UiFactory.showWarning(dialog, "Vui lòng nhập tên sách trước khi yêu cầu AI gợi ý!");
                    titleField.requestFocus();
                    return;
                }
                aiBtnHolder[0].setEnabled(false);
                aiBtnHolder[0].setText("⏳ Đang tra cứu...");

                SwingWorker<com.library.model.BookMetadataSuggestion, Void> worker = new SwingWorker<>() {
                    @Override
                    protected com.library.model.BookMetadataSuggestion doInBackground() throws Exception {
                        return com.library.service.AIService.suggestBookInfo(query);
                    }

                    @Override
                    protected void done() {
                        try {
                            com.library.model.BookMetadataSuggestion sugg = get();
                            if (sugg != null) {
                                if (sugg.getTitle() != null && !sugg.getTitle().isEmpty()) {
                                    titleField.setText(sugg.getTitle());
                                }
                                if (sugg.getAuthor() != null && !sugg.getAuthor().isEmpty()) {
                                    authorField.setText(sugg.getAuthor());
                                }
                                if (sugg.getPublisher() != null && !sugg.getPublisher().isEmpty()) {
                                    publisherField.setText(sugg.getPublisher());
                                }
                                if (sugg.getIsbn() != null && !sugg.getIsbn().isEmpty()) {
                                    isbnField.setText(sugg.getIsbn());
                                }
                                if (reasonArea.getText().trim().isEmpty() && sugg.getDescription() != null && !sugg.getDescription().isEmpty()) {
                                    reasonArea.setText("Đề xuất phục vụ học tập & nghiên cứu: " + sugg.getDescription());
                                }
                                UiFactory.showInfo(dialog, "Đã tự động hoàn thiện thông tin sách từ AI!");
                            }
                        } catch (Exception ex) {
                            String err = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                            UiFactory.showError(dialog, "Lỗi khi gọi AI: " + err);
                        } finally {
                            aiBtnHolder[0].setEnabled(true);
                            aiBtnHolder[0].setText("🪄 AI Gợi ý");
                        }
                    }
                };
                worker.execute();
            });
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        StyledButton saveBtn = StyledButton.success("Gửi đề xuất");
        saveBtn.addActionListener(e -> {
            PurchaseSuggestion s = new PurchaseSuggestion();
            s.setBookTitle(titleField.getText().trim());
            s.setAuthor(authorField.getText().trim());
            s.setIsbn(isbnField.getText().trim());
            s.setPublisher(publisherField.getText().trim());
            s.setQuantity((int) qtySpinner.getValue());
            s.setReason(reasonArea.getText().trim());
            s.setSuggestedBy(suggestedByField.getText().trim());

            String error = controller.saveSuggestion(s);
            if (error != null) {
                UiFactory.showError(dialog, error);
            } else {
                dialog.dispose();
                loadData();
                UiFactory.showInfo(this, "Đã gửi đề xuất thành công!");
            }
        });

        StyledButton cancelBtn = UiFactory.secondaryButton("Hủy");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(AppConstants.BG_DARK);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        content.add(form, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private void reviewSelected(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiFactory.showWarning(this, "Vui lòng chọn đề xuất!");
            return;
        }

        String currentStatus = (String) tableModel.getValueAt(row, 8);
        if (!"PENDING".equals(currentStatus)) {
            UiFactory.showWarning(this, "Đề xuất này đã được xử lý rồi!");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);
        String action = "APPROVED".equals(newStatus) ? "duyệt" : "từ chối";

        String note = JOptionPane.showInputDialog(this,
                action.substring(0, 1).toUpperCase() + action.substring(1) + " đề xuất: \"" + title + "\"\nGhi chú (tùy chọn):",
                "Xác nhận " + action, JOptionPane.QUESTION_MESSAGE);

        if (note == null) return; // cancelled

        int userId = AuthController.getInstance().getCurrentUser().getId();
        String error = "APPROVED".equals(newStatus)
                ? controller.approve(id, note, userId)
                : controller.reject(id, note, userId);

        if (error != null) {
            UiFactory.showError(this, error);
        } else {
            loadData();
            UiFactory.showInfo(this, "Đã " + action + " đề xuất thành công!");
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiFactory.showWarning(this, "Vui lòng chọn đề xuất cần xóa!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);

        if (UiFactory.confirm(this, "Xóa đề xuất: \"" + title + "\"?", "Xác nhận xóa")) {
            String error = controller.delete(id);
            if (error != null) {
                UiFactory.showError(this, error);
            } else {
                loadData();
            }
        }
    }
}
