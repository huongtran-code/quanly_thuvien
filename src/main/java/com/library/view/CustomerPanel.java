package com.library.view;

import com.library.controller.CustomerController;
import com.library.model.Customer;
import com.library.util.AppConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomerPanel extends JPanel {
    
    private final CustomerController controller;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public CustomerPanel() {
        this.controller = new CustomerController();
        initComponents();
        loadCustomers();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(AppConstants.PADDING, AppConstants.PADDING));
        setBackground(AppConstants.BG_DARK);
        
        // Top Panel - Search & Filters
        add(createTopPanel(), BorderLayout.NORTH);
        
        // Center - Table
        add(createTablePanel(), BorderLayout.CENTER);
        
        // Bottom - Action Buttons
        add(createBottomPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppConstants.PADDING_SM, 0));
        panel.setBackground(AppConstants.BG_DARK);
        
        // Title
        JLabel titleLabel = new JLabel("Quản Lý Khách Hàng");
        titleLabel.setFont(AppConstants.FONT_TITLE);
        titleLabel.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(titleLabel);
        
        panel.add(Box.createHorizontalStrut(30));
        
        // Search
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setForeground(AppConstants.TEXT_SECONDARY);
        panel.add(searchLabel);
        
        searchField = new JTextField(20);
        searchField.setBackground(AppConstants.BG_INPUT);
        searchField.setForeground(AppConstants.TEXT_PRIMARY);
        searchField.setCaretColor(AppConstants.TEXT_PRIMARY);
        panel.add(searchField);
        
        JButton searchBtn = new JButton("Tìm");
        searchBtn.setBackground(AppConstants.PRIMARY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.addActionListener(e -> searchCustomers());
        panel.add(searchBtn);
        
        // Status Filter
        JLabel filterLabel = new JLabel("Trạng thái:");
        filterLabel.setForeground(AppConstants.TEXT_SECONDARY);
        panel.add(filterLabel);
        
        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Hoạt động", "Tạm ngưng", "Không hoạt động"});
        statusFilter.setBackground(AppConstants.BG_INPUT);
        statusFilter.setForeground(AppConstants.TEXT_PRIMARY);
        statusFilter.addActionListener(e -> filterByStatus());
        panel.add(statusFilter);
        
        return panel;
    }
    
    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "Mã KH", "Họ tên", "Email", "Số ĐT", "Địa chỉ", 
                           "Ngày đăng ký", "Tổng mượn", "Tổng phạt", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        customerTable = new JTable(tableModel);
        customerTable.setBackground(AppConstants.BG_CARD);
        customerTable.setForeground(AppConstants.TEXT_PRIMARY);
        customerTable.setSelectionBackground(AppConstants.PRIMARY);
        customerTable.setSelectionForeground(Color.WHITE);
        customerTable.setRowHeight(AppConstants.TABLE_ROW_HEIGHT);
        customerTable.setFont(AppConstants.FONT_BODY);
        customerTable.getTableHeader().setBackground(AppConstants.BG_CARD_HOVER);
        customerTable.getTableHeader().setForeground(AppConstants.TEXT_PRIMARY);
        customerTable.getTableHeader().setFont(AppConstants.FONT_HEADING);
        
        // Hide ID column
        customerTable.getColumnModel().getColumn(0).setMinWidth(0);
        customerTable.getColumnModel().getColumn(0).setMaxWidth(0);
        customerTable.getColumnModel().getColumn(0).setWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.getViewport().setBackground(AppConstants.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        
        return scrollPane;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppConstants.PADDING_SM, 0));
        panel.setBackground(AppConstants.BG_DARK);
        
        JButton addBtn = createButton("Thêm khách hàng", AppConstants.ACCENT);
        addBtn.addActionListener(e -> showAddDialog());
        panel.add(addBtn);
        
        JButton editBtn = createButton("Sửa", AppConstants.PRIMARY);
        editBtn.addActionListener(e -> showEditDialog());
        panel.add(editBtn);
        
        JButton deleteBtn = createButton("Xóa", AppConstants.DANGER);
        deleteBtn.addActionListener(e -> deleteCustomer());
        panel.add(deleteBtn);
        
        JButton suspendBtn = createButton("Tạm ngưng", AppConstants.WARNING);
        suspendBtn.addActionListener(e -> suspendCustomer());
        panel.add(suspendBtn);
        
        JButton activateBtn = createButton("Kích hoạt", AppConstants.ACCENT);
        activateBtn.addActionListener(e -> activateCustomer());
        panel.add(activateBtn);
        
        JButton refreshBtn = createButton("Làm mới", AppConstants.PRIMARY_DARK);
        refreshBtn.addActionListener(e -> loadCustomers());
        panel.add(refreshBtn);
        
        return panel;
    }
    
    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(AppConstants.FONT_BODY);
        return button;
    }
    
    private void loadCustomers() {
        tableModel.setRowCount(0);
        List<Customer> customers = controller.getAllCustomers();
        
        for (Customer customer : customers) {
            Object[] row = {
                customer.getId(),
                customer.getCustomerCode(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getRegistrationDate().format(DATE_FORMATTER),
                customer.getTotalBorrowed(),
                String.format("%,.0f VNĐ", customer.getTotalFines()),
                getStatusText(customer.getStatus())
            };
            tableModel.addRow(row);
        }
    }
    
    private void searchCustomers() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        
        List<Customer> customers = controller.searchCustomers(keyword);
        for (Customer customer : customers) {
            Object[] row = {
                customer.getId(),
                customer.getCustomerCode(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getRegistrationDate().format(DATE_FORMATTER),
                customer.getTotalBorrowed(),
                String.format("%,.0f VNĐ", customer.getTotalFines()),
                getStatusText(customer.getStatus())
            };
            tableModel.addRow(row);
        }
    }
    
    private void filterByStatus() {
        String selected = (String) statusFilter.getSelectedItem();
        if ("Tất cả".equals(selected)) {
            loadCustomers();
            return;
        }
        
        String status = switch (selected) {
            case "Hoạt động" -> AppConstants.CUSTOMER_STATUS_ACTIVE;
            case "Tạm ngưng" -> AppConstants.CUSTOMER_STATUS_SUSPENDED;
            case "Không hoạt động" -> AppConstants.CUSTOMER_STATUS_INACTIVE;
            default -> null;
        };
        
        if (status != null) {
            tableModel.setRowCount(0);
            List<Customer> customers = controller.getCustomersByStatus(status);
            for (Customer customer : customers) {
                Object[] row = {
                    customer.getId(),
                    customer.getCustomerCode(),
                    customer.getFullName(),
                    customer.getEmail(),
                    customer.getPhone(),
                    customer.getAddress(),
                    customer.getRegistrationDate().format(DATE_FORMATTER),
                    customer.getTotalBorrowed(),
                    String.format("%,.0f VNĐ", customer.getTotalFines()),
                    getStatusText(customer.getStatus())
                };
                tableModel.addRow(row);
            }
        }
    }
    
    private void showAddDialog() {
        CustomerDialog dialog = new CustomerDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Customer customer = dialog.getCustomer();
            try {
                if (controller.createCustomer(customer)) {
                    JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadCustomers();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm khách hàng thất bại!", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showEditDialog() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        Customer customer = controller.getCustomerById(customerId);
        
        CustomerDialog dialog = new CustomerDialog((Frame) SwingUtilities.getWindowAncestor(this), customer);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Customer updatedCustomer = dialog.getCustomer();
            updatedCustomer.setId(customerId);
            
            try {
                if (controller.updateCustomer(updatedCustomer)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thành công!", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadCustomers();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thất bại!", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa khách hàng này?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int customerId = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                if (controller.deleteCustomer(customerId)) {
                    JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công!", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadCustomers();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa khách hàng thất bại!", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void suspendCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            if (controller.suspendCustomer(customerId)) {
                JOptionPane.showMessageDialog(this, "Tạm ngưng khách hàng thành công!", 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadCustomers();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void activateCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            if (controller.activateCustomer(customerId)) {
                JOptionPane.showMessageDialog(this, "Kích hoạt khách hàng thành công!", 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadCustomers();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getStatusText(String status) {
        return switch (status) {
            case AppConstants.CUSTOMER_STATUS_ACTIVE -> "Hoạt động";
            case AppConstants.CUSTOMER_STATUS_SUSPENDED -> "Tạm ngưng";
            case AppConstants.CUSTOMER_STATUS_INACTIVE -> "Không hoạt động";
            default -> status;
        };
    }
}
