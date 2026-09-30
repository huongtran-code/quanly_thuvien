package com.library.view;

import com.library.controller.BorrowController;
import com.library.controller.CustomerController;
import com.library.dao.DocumentDAO;
import com.library.model.Borrow;
import com.library.model.BorrowItem;
import com.library.model.Customer;
import com.library.model.Document;
import com.library.util.AppConstants;
import com.library.util.Icons;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CreateBorrowDialog extends JDialog {
    
    private final BorrowController borrowController;
    private final CustomerController customerController;
    private final DocumentDAO documentDAO;
    
    private JComboBox<CustomerItem> customerCombo;
    private DefaultTableModel itemsTableModel;
    private JTable itemsTable;
    private JTextField documentIdField;
    private JSpinner quantitySpinner;
    
    private boolean success = false;
    
    public CreateBorrowDialog(Frame parent) {
        super(parent, "Tạo Phiếu Mượn Sách", true);
        this.borrowController = new BorrowController();
        this.customerController = new CustomerController();
        this.documentDAO = new DocumentDAO();
        
        initComponents();
        loadCustomers();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(AppConstants.BG_DARK);
        
        add(createFormPanel(), BorderLayout.NORTH);
        add(createItemsPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        setSize(700, 500);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppConstants.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel customerLabel = new JLabel("Khách hàng:");
        customerLabel.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(customerLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        customerCombo = new JComboBox<>();
        customerCombo.setBackground(AppConstants.BG_INPUT);
        customerCombo.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(customerCombo, gbc);
        
        return panel;
    }
    
    private JPanel createItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AppConstants.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel addItemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addItemPanel.setBackground(AppConstants.BG_CARD);
        addItemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel docLabel = new JLabel("ID Tài liệu:");
        docLabel.setForeground(AppConstants.TEXT_PRIMARY);
        addItemPanel.add(docLabel);
        
        documentIdField = new JTextField(10);
        documentIdField.setBackground(AppConstants.BG_INPUT);
        documentIdField.setForeground(AppConstants.TEXT_PRIMARY);
        addItemPanel.add(documentIdField);
        
        JLabel qtyLabel = new JLabel("Số lượng:");
        qtyLabel.setForeground(AppConstants.TEXT_PRIMARY);
        addItemPanel.add(qtyLabel);
        
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        quantitySpinner.setPreferredSize(new Dimension(60, 25));
        addItemPanel.add(quantitySpinner);
        
        JButton addBtn = new JButton("Thêm sách");
        addBtn.setIcon(Icons.button("plus"));
        addBtn.setBackground(AppConstants.ACCENT);
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> addItem());
        addItemPanel.add(addBtn);
        
        panel.add(addItemPanel, BorderLayout.NORTH);
        
        String[] columns = {"ID Tài liệu", "Tên sách", "Số lượng"};
        itemsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        itemsTable = new JTable(itemsTableModel);
        itemsTable.setBackground(AppConstants.BG_CARD);
        itemsTable.setForeground(AppConstants.TEXT_PRIMARY);
        itemsTable.setSelectionBackground(AppConstants.PRIMARY);
        itemsTable.setSelectionForeground(Color.WHITE);
        itemsTable.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.getViewport().setBackground(AppConstants.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton removeBtn = new JButton("Xóa sách đã chọn");
        removeBtn.setIcon(Icons.button("trash"));
        removeBtn.setBackground(AppConstants.DANGER);
        removeBtn.setForeground(Color.WHITE);
        removeBtn.addActionListener(e -> removeSelectedItem());
        
        JPanel removePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        removePanel.setBackground(AppConstants.BG_DARK);
        removePanel.add(removeBtn);
        panel.add(removePanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(AppConstants.BG_DARK);
        
        JButton saveBtn = new JButton("Tạo phiếu");
        saveBtn.setIcon(Icons.button("check"));
        saveBtn.setBackground(AppConstants.ACCENT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> createBorrow());
        panel.add(saveBtn);
        
        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setIcon(Icons.button("x"));
        cancelBtn.setBackground(AppConstants.DANGER);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> dispose());
        panel.add(cancelBtn);
        
        return panel;
    }
    
    private void loadCustomers() {
        customerCombo.removeAllItems();
        List<Customer> customers = customerController.getAllCustomers();
        for (Customer customer : customers) {
            if (customer.isActive()) {
                customerCombo.addItem(new CustomerItem(customer));
            }
        }
    }
    
    private void addItem() {
        try {
            int documentId = Integer.parseInt(documentIdField.getText().trim());
            int quantity = (Integer) quantitySpinner.getValue();
            
            Document doc = documentDAO.findById(documentId);
            if (doc == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy tài liệu!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (doc.getStockQuantity() < quantity) {
                JOptionPane.showMessageDialog(this, "Không đủ tồn kho! Còn " + doc.getStockQuantity() + " quyển", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            itemsTableModel.addRow(new Object[]{documentId, doc.getTitle(), quantity});
            documentIdField.setText("");
            quantitySpinner.setValue(1);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID tài liệu không hợp lệ!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeSelectedItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow >= 0) {
            itemsTableModel.removeRow(selectedRow);
        }
    }
    
    private void createBorrow() {
        CustomerItem selected = (CustomerItem) customerCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (itemsTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng thêm ít nhất 1 cuốn sách!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<BorrowItem> items = new ArrayList<>();
        for (int i = 0; i < itemsTableModel.getRowCount(); i++) {
            int docId = (Integer) itemsTableModel.getValueAt(i, 0);
            int qty = (Integer) itemsTableModel.getValueAt(i, 2);
            
            BorrowItem item = new BorrowItem();
            item.setDocumentId(docId);
            item.setQuantity(qty);
            items.add(item);
        }
        
        try {
            Borrow borrow = new Borrow();
            borrow.setCustomerId(selected.customer.getId());
            
            if (borrowController.createBorrow(borrow, items, null)) {
                success = true;
                JOptionPane.showMessageDialog(this, "Tạo phiếu mượn thành công!", 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Tạo phiếu mượn thất bại. Vui lòng thử lại.", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    private static class CustomerItem {
        final Customer customer;
        
        CustomerItem(Customer customer) {
            this.customer = customer;
        }
        
        @Override
        public String toString() {
            return customer.getCustomerCode() + " - " + customer.getFullName();
        }
    }
}
