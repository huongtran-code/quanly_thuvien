package com.library.view;

import com.library.controller.AuthController;
import com.library.controller.BudgetController;
import com.library.model.Budget;
import com.library.util.AppConstants;
import com.library.util.CurrencyUtil;
import com.library.util.Icons;
import com.library.view.components.FormPanel;
import com.library.view.components.StyledButton;
import com.library.view.components.StyledTable;
import com.library.view.components.UiFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel quản lý ngân sách với progress bar hiển thị % đã chi
 */
public class BudgetPanel extends JPanel {

    private final BudgetController controller = new BudgetController();
    private StyledTable table;
    private DefaultTableModel tableModel;
    private JPanel summaryPanel;

    public BudgetPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(AppConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Top section: Summary Cards + Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(0, 16));
        topPanel.setOpaque(false);
        
        summaryPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        summaryPanel.setOpaque(false);
        
        topPanel.add(summaryPanel, BorderLayout.NORTH);
        topPanel.add(createToolbar(), BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel createToolbar() {
        java.util.List<JComponent> buttons = new java.util.ArrayList<>();

        if (AuthController.getInstance().isAdmin()) {
            StyledButton addBtn = StyledButton.success("Thêm ngân sách");
            addBtn.setIcon(Icons.button("plus"));
            addBtn.addActionListener(e -> showBudgetDialog(null));

            StyledButton editBtn = new StyledButton("Sửa");
            editBtn.setIcon(Icons.button("edit"));
            editBtn.addActionListener(e -> editSelected());

            StyledButton closeBtn = StyledButton.warning("Đóng NS");
            closeBtn.setIcon(Icons.button("lock"));
            closeBtn.addActionListener(e -> closeBudget());

            StyledButton reopenBtn = StyledButton.success("Mở NS");
            reopenBtn.setIcon(Icons.button("unlock"));
            reopenBtn.addActionListener(e -> reopenBudget());

            buttons.add(addBtn);
            buttons.add(editBtn);
            buttons.add(closeBtn);
            buttons.add(reopenBtn);
        }

        StyledButton refreshBtn = UiFactory.secondaryButton("Làm mới");
        refreshBtn.setIcon(Icons.button("refresh"));
        refreshBtn.addActionListener(e -> loadData());
        buttons.add(refreshBtn);

        return UiFactory.toolbar("Quản lý Ngân sách", Icons.title("wallet"),
                buttons.toArray(new JComponent[0]));
    }

    private JPanel createTablePanel() {
        String[] columns = {"ID", "Năm", "Tên ngân sách", "Tổng ngân sách", "Đã chi", "Còn lại", "Tiến độ", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);

        // Progress bar renderer cho cột "Tiến độ"
        table.getColumnModel().getColumn(6).setCellRenderer(new ProgressBarRenderer());
        // Status renderer
        table.getColumnModel().getColumn(7).setCellRenderer(new StyledTable.StatusRenderer());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppConstants.BG_DARK);
        panel.add(table.wrapInScrollPane(), BorderLayout.CENTER);

        // Summary cards moved to topPanel in constructor
        return panel;
    }

    private void updateSummary(List<Budget> budgets) {
        summaryPanel.removeAll();

        double totalBudget = budgets.stream().mapToDouble(Budget::getTotalAmount).sum();
        double totalSpent = budgets.stream().mapToDouble(Budget::getSpentAmount).sum();
        double totalRemaining = budgets.stream().mapToDouble(Budget::getRemainingAmount).sum();

        summaryPanel.add(createCard("Tổng ngân sách", "briefcase", CurrencyUtil.format(totalBudget), AppConstants.PRIMARY));
        summaryPanel.add(createCard("Đã chi", "upload", CurrencyUtil.format(totalSpent), AppConstants.WARNING));
        summaryPanel.add(createCard("Còn lại", "wallet", CurrencyUtil.format(totalRemaining), AppConstants.ACCENT));

        summaryPanel.revalidate();
        summaryPanel.repaint();
    }

    private JPanel createCard(String title, String iconName, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppConstants.BG_CARD);
        // Remove heavy borders and use flat soft styling
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppConstants.FONT_SMALL);
        titleLabel.setForeground(AppConstants.TEXT_MUTED);
        titleLabel.setIcon(Icons.get(iconName, 14, color));
        titleLabel.setIconTextGap(6);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(AppConstants.FONT_SUBTITLE);
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);

        return card;
    }

    private void loadData() {
        List<Budget> budgets = controller.getAllBudgets();
        tableModel.setRowCount(0);
        for (Budget b : budgets) {
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getFiscalYear(),
                    b.getBudgetName(),
                    CurrencyUtil.format(b.getTotalAmount()),
                    CurrencyUtil.format(b.getSpentAmount()),
                    CurrencyUtil.format(b.getRemainingAmount()),
                    b.getSpentPercentage(), // double for progress bar
                    b.getStatus()
            });
        }
        updateSummary(budgets);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiFactory.showWarning(this, "Vui lòng chọn ngân sách!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Budget budget = controller.getBudgetById(id);
        if (budget != null) showBudgetDialog(budget);
    }

    private void closeBudget() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiFactory.showWarning(this, "Vui lòng chọn ngân sách!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 7);
        if ("CLOSED".equals(status)) {
            UiFactory.showInfo(this, "Ngân sách đã được đóng!");
            return;
        }
        String remaining = (String) tableModel.getValueAt(row, 5);
        if (UiFactory.confirmWarning(this,
                "Đóng ngân sách sẽ cắt phần ngân sách còn lại (" + remaining + ").\n"
                + "Không thể tạo đơn mua mới từ ngân sách này.\nBạn có chắc?",
                "Xác nhận đóng ngân sách")) {
            String error = controller.closeBudget(id);
            if (error != null) {
                UiFactory.showError(this, error);
            } else {
                UiFactory.showInfo(this, "Đã đóng ngân sách và cắt phần còn lại!");
                loadData();
            }
        }
    }

    private void reopenBudget() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiFactory.showWarning(this, "Vui lòng chọn ngân sách!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 7);
        if ("ACTIVE".equals(status)) {
            UiFactory.showInfo(this, "Ngân sách đang hoạt động rồi!");
            return;
        }
        String input = JOptionPane.showInputDialog(this,
                "Nhập tổng ngân sách mới (phải >= số đã chi):",
                "Mở lại ngân sách", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                double newTotal = Double.parseDouble(input.trim().replaceAll("[^0-9]", ""));
                String error = controller.reopenBudget(id, newTotal);
                if (error != null) {
                    UiFactory.showError(this, error);
                } else {
                    UiFactory.showInfo(this, "Đã mở lại ngân sách!");
                    loadData();
                }
            } catch (NumberFormatException ex) {
                UiFactory.showError(this, "Số tiền không hợp lệ!");
            }
        }
    }

    private void showBudgetDialog(Budget budget) {
        boolean isNew = (budget == null);
        JDialog dialog = UiFactory.modalDialog(this,
                isNew ? "Thêm ngân sách" : "Sửa ngân sách", 450, 320);

        FormPanel form = new FormPanel(130);
        JSpinner yearSpinner = form.addSpinner("Năm tài chính", 2000, 2100,
                isNew ? java.time.LocalDate.now().getYear() : budget.getFiscalYear());
        JTextField nameField = form.addTextField("Tên ngân sách", isNew ? "" : budget.getBudgetName());
        JTextField amountField = form.addTextField("Tổng ngân sách (₫)",
                isNew ? "0" : String.valueOf((long) budget.getTotalAmount()));

        StyledButton saveBtn = StyledButton.success("Lưu");
        saveBtn.setIcon(Icons.button("save"));
        StyledButton cancelBtn = UiFactory.secondaryButton("Hủy");

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                UiFactory.showError(dialog, "Tên ngân sách không được để trống!");
                nameField.requestFocus();
                return;
            }
            Budget b = isNew ? new Budget() : budget;
            b.setFiscalYear((int) yearSpinner.getValue());
            b.setBudgetName(name);
            try {
                b.setTotalAmount(Double.parseDouble(amountField.getText().trim().replaceAll("[^0-9]", "")));
            } catch (NumberFormatException ex) {
                UiFactory.showError(dialog, "Số tiền không hợp lệ!");
                amountField.requestFocus();
                return;
            }

            String error = controller.saveBudget(b);
            if (error != null) {
                UiFactory.showError(dialog, error);
            } else {
                dialog.dispose();
                loadData();
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setLayout(new BorderLayout());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(UiFactory.dialogButtons(cancelBtn, saveBtn), BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(saveBtn);
        dialog.setVisible(true);
    }

    /**
     * Custom renderer: progress bar cho cột tiến độ
     */
    private static class ProgressBarRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            double percent = value instanceof Number ? ((Number) value).doubleValue() : 0;
            JPanel panel = new JPanel(new BorderLayout(4, 0));
            panel.setBackground(isSelected ? AppConstants.PRIMARY.darker() :
                    (row % 2 == 0 ? AppConstants.BG_CARD : AppConstants.BG_ROW_ALT));
            panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

            // Progress bar
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue(Math.min((int) percent, 100));
            bar.setStringPainted(true);
            bar.setString(String.format("%.1f%%", percent));
            bar.setFont(AppConstants.FONT_SMALL);
            bar.setPreferredSize(new Dimension(80, 18));

            // Color based on percentage
            if (percent >= 90) {
                bar.setForeground(AppConstants.DANGER);
            } else if (percent >= 70) {
                bar.setForeground(AppConstants.WARNING);
            } else {
                bar.setForeground(AppConstants.ACCENT);
            }
            bar.setBackground(AppConstants.BG_INPUT);

            panel.add(bar, BorderLayout.CENTER);
            return panel;
        }
    }
}
