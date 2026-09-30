package com.library.view;

import com.library.controller.CustomerController;
import com.library.controller.FineController;
import com.library.model.Customer;
import com.library.model.Fine;
import com.library.util.AppConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FinePanel extends JPanel {
    
    private final FineController fineController;
    private final CustomerController customerController;
    private JTable fineTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public FinePanel() {
        this.fineController = new FineController();
        this.customerController = new CustomerController();
        initComponents();
        loadFines();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(AppConstants.PADDING, AppConstants.PADDING));
        setBackground(AppConstants.BG_DARK);
        
        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppConstants.PADDING_SM, 0));
        panel.setBackground(AppConstants.BG_DARK);
        
        JLabel titleLabel = new JLabel("Quản Lý Phạt");
        titleLabel.setFont(AppConstants.FONT_TITLE);
        titleLabel.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(titleLabel);
        
        panel.add(Box.createHorizontalStrut(30));
        
        JLabel filterLabel = new JLabel("Trạng thái:");
        filterLabel.setForeground(AppConstants.TEXT_SECONDARY);
        panel.add(filterLabel);
        
        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Chưa thanh toán", "Đã thanh toán", "Miễn phạt"});
        statusFilter.setBackground(AppConstants.BG_INPUT);
        statusFilter.setForeground(AppConstants.TEXT_PRIMARY);
        statusFilter.addActionListener(e -> filterByStatus());
        panel.add(statusFilter);
        
        return panel;
    }
    
    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "Khách hàng", "Loại phạt", "Số tiền", "Lý do", 
                           "Ngày tạo", "Ngày thanh toán", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        fineTable = new JTable(tableModel);
        fineTable.setBackground(AppConstants.BG_CARD);
        fineTable.setForeground(AppConstants.TEXT_PRIMARY);
        fineTable.setSelectionBackground(AppConstants.PRIMARY);
        fineTable.setSelectionForeground(Color.WHITE);
        fineTable.setRowHeight(AppConstants.TABLE_ROW_HEIGHT);
        fineTable.setFont(AppConstants.FONT_BODY);
        fineTable.getTableHeader().setBackground(AppConstants.BG_CARD_HOVER);
        fineTable.getTableHeader().setForeground(AppConstants.TEXT_PRIMARY);
        fineTable.getTableHeader().setFont(AppConstants.FONT_HEADING);
        
        fineTable.getColumnModel().getColumn(0).setMinWidth(0);
        fineTable.getColumnModel().getColumn(0).setMaxWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(fineTable);
        scrollPane.getViewport().setBackground(AppConstants.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        
        return scrollPane;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppConstants.PADDING_SM, 0));
        panel.setBackground(AppConstants.BG_DARK);
        
        JButton payBtn = createButton("Thanh toán", AppConstants.ACCENT);
        payBtn.addActionListener(e -> payFine());
        panel.add(payBtn);
        
        JButton waiveBtn = createButton("Miễn phạt", AppConstants.WARNING);
        waiveBtn.addActionListener(e -> waiveFine());
        panel.add(waiveBtn);
        
        JButton refreshBtn = createButton("Làm mới", AppConstants.PRIMARY_DARK);
        refreshBtn.addActionListener(e -> loadFines());
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
    
    private void loadFines() {
        tableModel.setRowCount(0);
        List<Fine> fines = fineController.getAllFines();
        
        for (Fine fine : fines) {
            Customer customer = customerController.getCustomerById(fine.getCustomerId());
            String customerName = customer != null ? customer.getFullName() : "N/A";
            
            Object[] row = {
                fine.getId(),
                customerName,
                getFineTypeText(fine.getFineType()),
                String.format("%,.0f VNĐ", fine.getAmount()),
                fine.getReason(),
                fine.getCreatedAt().format(DATETIME_FORMATTER),
                fine.getPaidDate() != null ? fine.getPaidDate().format(DATETIME_FORMATTER) : "",
                getStatusText(fine.getStatus())
            };
            tableModel.addRow(row);
        }
    }
    
    private void filterByStatus() {
        String selected = (String) statusFilter.getSelectedItem();
        if ("Tất cả".equals(selected)) {
            loadFines();
            return;
        }
        
        String status = switch (selected) {
            case "Chưa thanh toán" -> AppConstants.FINE_STATUS_UNPAID;
            case "Đã thanh toán" -> AppConstants.FINE_STATUS_PAID;
            case "Miễn phạt" -> AppConstants.FINE_STATUS_WAIVED;
            default -> null;
        };
        
        if (status != null) {
            tableModel.setRowCount(0);
            List<Fine> fines = fineController.getFinesByStatus(status);
            for (Fine fine : fines) {
                Customer customer = customerController.getCustomerById(fine.getCustomerId());
                String customerName = customer != null ? customer.getFullName() : "N/A";
                
                Object[] row = {
                    fine.getId(),
                    customerName,
                    getFineTypeText(fine.getFineType()),
                    String.format("%,.0f VNĐ", fine.getAmount()),
                    fine.getReason(),
                    fine.getCreatedAt().format(DATETIME_FORMATTER),
                    fine.getPaidDate() != null ? fine.getPaidDate().format(DATETIME_FORMATTER) : "",
                    getStatusText(fine.getStatus())
                };
                tableModel.addRow(row);
            }
        }
    }
    
    private void payFine() {
        int selectedRow = fineTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khoản phạt!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int fineId = (int) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Xác nhận thanh toán khoản phạt này?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (fineController.payFine(fineId, null)) {
                    JOptionPane.showMessageDialog(this, "Thanh toán thành công!", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadFines();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void waiveFine() {
        int selectedRow = fineTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khoản phạt!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int fineId = (int) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Xác nhận miễn phạt cho khoản này?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (fineController.waiveFine(fineId, null)) {
                    JOptionPane.showMessageDialog(this, "Miễn phạt thành công!", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadFines();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private String getFineTypeText(String type) {
        return switch (type) {
            case AppConstants.FINE_TYPE_LATE_RETURN -> "Trả trễ";
            case AppConstants.FINE_TYPE_DAMAGED -> "Hư hỏng";
            case AppConstants.FINE_TYPE_LOST -> "Mất sách";
            default -> type;
        };
    }
    
    private String getStatusText(String status) {
        return switch (status) {
            case AppConstants.FINE_STATUS_UNPAID -> "Chưa thanh toán";
            case AppConstants.FINE_STATUS_PAID -> "Đã thanh toán";
            case AppConstants.FINE_STATUS_WAIVED -> "Miễn phạt";
            default -> status;
        };
    }
}
