package com.library.view;

import com.library.controller.CustomerController;
import com.library.controller.SaleController;
import com.library.model.Customer;
import com.library.model.Sale;
import com.library.util.AppConstants;
import com.library.util.Icons;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SalePanel extends JPanel {
    
    private final SaleController saleController;
    private final CustomerController customerController;
    private JTable saleTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public SalePanel() {
        this.saleController = new SaleController();
        this.customerController = new CustomerController();
        initComponents();
        loadSales();
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
        
        JLabel titleLabel = new JLabel("Quản Lý Bán Sách");
        titleLabel.setFont(AppConstants.FONT_TITLE);
        titleLabel.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(titleLabel);
        
        panel.add(Box.createHorizontalStrut(30));
        
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
        searchBtn.addActionListener(e -> searchSales());
        panel.add(searchBtn);
        
        return panel;
    }
    
    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "Mã đơn", "Khách hàng", "Ngày bán", "Tổng tiền", 
                           "Giảm giá", "Thành tiền", "Thanh toán"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        saleTable = new JTable(tableModel);
        saleTable.setBackground(AppConstants.BG_CARD);
        saleTable.setForeground(AppConstants.TEXT_PRIMARY);
        saleTable.setSelectionBackground(AppConstants.PRIMARY);
        saleTable.setSelectionForeground(Color.WHITE);
        saleTable.setRowHeight(AppConstants.TABLE_ROW_HEIGHT);
        saleTable.setFont(AppConstants.FONT_BODY);
        saleTable.getTableHeader().setBackground(AppConstants.BG_CARD_HOVER);
        saleTable.getTableHeader().setForeground(AppConstants.TEXT_PRIMARY);
        saleTable.getTableHeader().setFont(AppConstants.FONT_HEADING);
        
        saleTable.getColumnModel().getColumn(0).setMinWidth(0);
        saleTable.getColumnModel().getColumn(0).setMaxWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(saleTable);
        scrollPane.getViewport().setBackground(AppConstants.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        
        return scrollPane;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppConstants.PADDING_SM, 0));
        panel.setBackground(AppConstants.BG_DARK);
        
        JButton addBtn = createButton("Tạo đơn bán", AppConstants.ACCENT);
        addBtn.setIcon(Icons.button("wallet"));
        addBtn.addActionListener(e -> showCreateSaleDialog());
        panel.add(addBtn);
        
        JButton viewBtn = createButton("Xem chi tiết", AppConstants.PRIMARY_DARK);
        viewBtn.setIcon(Icons.button("eye"));
        viewBtn.addActionListener(e -> viewSaleDetails());
        panel.add(viewBtn);
        
        JButton refreshBtn = createButton("Làm mới", AppConstants.PRIMARY_DARK);
        refreshBtn.setIcon(Icons.button("refresh"));
        refreshBtn.addActionListener(e -> loadSales());
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
    
    private void loadSales() {
        tableModel.setRowCount(0);
        List<Sale> sales = saleController.getAllSales();
        
        for (Sale sale : sales) {
            String customerName = "Khách vãng lai";
            if (sale.getCustomerId() != null) {
                Customer customer = customerController.getCustomerById(sale.getCustomerId());
                customerName = customer != null ? customer.getFullName() : "N/A";
            }
            
            Object[] row = {
                sale.getId(),
                sale.getSaleCode(),
                customerName,
                sale.getSaleDate().format(DATETIME_FORMATTER),
                String.format("%,.0f VNĐ", sale.getTotalAmount()),
                String.format("%,.0f VNĐ", sale.getDiscountAmount()),
                String.format("%,.0f VNĐ", sale.getFinalAmount()),
                getPaymentMethodText(sale.getPaymentMethod())
            };
            tableModel.addRow(row);
        }
    }
    
    private void searchSales() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        
        List<Sale> sales = saleController.searchSales(keyword);
        for (Sale sale : sales) {
            String customerName = "Khách vãng lai";
            if (sale.getCustomerId() != null) {
                Customer customer = customerController.getCustomerById(sale.getCustomerId());
                customerName = customer != null ? customer.getFullName() : "N/A";
            }
            
            Object[] row = {
                sale.getId(),
                sale.getSaleCode(),
                customerName,
                sale.getSaleDate().format(DATETIME_FORMATTER),
                String.format("%,.0f VNĐ", sale.getTotalAmount()),
                String.format("%,.0f VNĐ", sale.getDiscountAmount()),
                String.format("%,.0f VNĐ", sale.getFinalAmount()),
                getPaymentMethodText(sale.getPaymentMethod())
            };
            tableModel.addRow(row);
        }
    }
    
    private void showCreateSaleDialog() {
        CreateSaleDialog dialog = new CreateSaleDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadSales();
        }
    }
    
    private void viewSaleDetails() {
        int selectedRow = saleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn bán!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int saleId = (int) tableModel.getValueAt(selectedRow, 0);
        SaleDetailDialog dialog = new SaleDetailDialog((Frame) SwingUtilities.getWindowAncestor(this), saleId);
        dialog.setVisible(true);
    }
    
    private String getPaymentMethodText(String method) {
        return switch (method) {
            case AppConstants.PAYMENT_CASH -> "Tiền mặt";
            case AppConstants.PAYMENT_CARD -> "Thẻ";
            case AppConstants.PAYMENT_TRANSFER -> "Chuyển khoản";
            default -> method;
        };
    }
}
