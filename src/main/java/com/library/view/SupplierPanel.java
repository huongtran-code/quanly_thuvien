package com.library.view;

import com.library.controller.SupplierController;
import com.library.model.Supplier;
import com.library.util.AppConstants;
import com.library.util.Icons;
import com.library.view.components.FormPanel;
import com.library.view.components.StyledButton;
import com.library.view.components.StyledTable;
import com.library.view.components.UiFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel quản lý nhà cung cấp
 */
public class SupplierPanel extends JPanel {

    private final SupplierController controller = new SupplierController();
    private StyledTable table;
    private DefaultTableModel tableModel;

    public SupplierPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(AppConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel createToolbar() {
        StyledButton addBtn = StyledButton.success("Thêm mới");
        addBtn.setIcon(Icons.button("plus"));
        addBtn.addActionListener(e -> showSupplierDialog(null));

        StyledButton editBtn = new StyledButton("Sửa");
        editBtn.setIcon(Icons.button("edit"));
        editBtn.addActionListener(e -> editSelected());

        StyledButton toggleBtn = StyledButton.warning("Bật/Tắt");
        toggleBtn.setIcon(Icons.button("power"));
        toggleBtn.addActionListener(e -> toggleSelected());

        StyledButton refreshBtn = UiFactory.secondaryButton("Làm mới");
        refreshBtn.setIcon(Icons.button("refresh"));
        refreshBtn.addActionListener(e -> loadData());

        return UiFactory.toolbar("Quản lý Nhà cung cấp", Icons.title("building"),
                refreshBtn, addBtn, editBtn, toggleBtn);
    }

    private JPanel createTablePanel() {
        String[] columns = {"ID", "Tên nhà cung cấp", "Người liên hệ", "Điện thoại", "Email", "Địa chỉ", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setCellRenderer(new StyledTable.StatusRenderer());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppConstants.BG_DARK);
        panel.add(table.wrapInScrollPane(), BorderLayout.CENTER);
        return panel;
    }

    private void loadData() {
        List<Supplier> suppliers = controller.getAllSuppliers();
        tableModel.setRowCount(0);
        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    s.getContactPerson(),
                    s.getPhone(),
                    s.getEmail(),
                    s.getAddress(),
                    s.isActive() ? "ACTIVE" : "CLOSED"
            });
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiFactory.showWarning(this, "Vui lòng chọn nhà cung cấp!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        // Find supplier from list
        List<Supplier> all = controller.getAllSuppliers();
        Supplier sup = all.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
        if (sup != null) showSupplierDialog(sup);
    }

    private void toggleSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiFactory.showWarning(this, "Vui lòng chọn nhà cung cấp!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 6);
        boolean newActive = !"ACTIVE".equals(status);
        String action = newActive ? "kích hoạt" : "vô hiệu hóa";
        if (UiFactory.confirm(this, "Bạn có chắc muốn " + action + " nhà cung cấp này?", "Xác nhận")) {
            String error = controller.toggleActive(id, newActive);
            if (error != null) {
                UiFactory.showError(this, error);
            } else {
                loadData();
            }
        }
    }

    private void showSupplierDialog(Supplier sup) {
        boolean isNew = (sup == null);
        JDialog dialog = UiFactory.modalDialog(this,
                isNew ? "Thêm nhà cung cấp" : "Sửa nhà cung cấp", 500, 400);

        FormPanel form = new FormPanel(120);
        JTextField nameField = form.addTextField("Tên NCC", isNew ? "" : sup.getName());
        JTextField contactField = form.addTextField("Người liên hệ", isNew ? "" : sup.getContactPerson());
        JTextField phoneField = form.addTextField("Điện thoại", isNew ? "" : sup.getPhone());
        JTextField emailField = form.addTextField("Email", isNew ? "" : sup.getEmail());
        JTextArea addressArea = form.addTextArea("Địa chỉ", 3);
        if (!isNew) addressArea.setText(sup.getAddress());

        StyledButton saveBtn = StyledButton.success("Lưu");
        saveBtn.setIcon(Icons.button("save"));
        StyledButton cancelBtn = UiFactory.secondaryButton("Hủy");

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                UiFactory.showError(dialog, "Tên nhà cung cấp không được để trống!");
                nameField.requestFocus();
                return;
            }
            Supplier s = isNew ? new Supplier() : sup;
            s.setName(name);
            s.setContactPerson(contactField.getText().trim());
            s.setPhone(phoneField.getText().trim());
            s.setEmail(emailField.getText().trim());
            s.setAddress(addressArea.getText().trim());

            String error = controller.saveSupplier(s);
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
}
