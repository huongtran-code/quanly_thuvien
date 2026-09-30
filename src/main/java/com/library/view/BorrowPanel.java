package com.library.view;

import com.library.controller.BorrowController;
import com.library.controller.CustomerController;
import com.library.model.Borrow;
import com.library.model.BorrowItem;
import com.library.model.Customer;
import com.library.util.AppConstants;
import com.library.util.Icons;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BorrowPanel extends JPanel {
    
    private final BorrowController borrowController;
    private final CustomerController customerController;
    private JTable borrowTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public BorrowPanel() {
        this.borrowController = new BorrowController();
        this.customerController = new CustomerController();
        initComponents();
        loadBorrows();
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
        
        JLabel titleLabel = new JLabel("Quản Lý Mượn Sách");
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
        searchBtn.addActionListener(e -> searchBorrows());
        panel.add(searchBtn);
        
        JLabel filterLabel = new JLabel("Trạng thái:");
        filterLabel.setForeground(AppConstants.TEXT_SECONDARY);
        panel.add(filterLabel);
        
        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Đang mượn", "Đã trả", "Quá hạn"});
        statusFilter.setBackground(AppConstants.BG_INPUT);
        statusFilter.setForeground(AppConstants.TEXT_PRIMARY);
        statusFilter.addActionListener(e -> filterByStatus());
        panel.add(statusFilter);
        
        return panel;
    }
    
    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "Mã phiếu", "Khách hàng", "Ngày mượn", "Hạn trả", 
                           "Ngày trả", "Số sách", "Phí trễ", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        borrowTable = new JTable(tableModel);
        borrowTable.setBackground(AppConstants.BG_CARD);
        borrowTable.setForeground(AppConstants.TEXT_PRIMARY);
        borrowTable.setSelectionBackground(AppConstants.PRIMARY);
        borrowTable.setSelectionForeground(Color.WHITE);
        borrowTable.setRowHeight(AppConstants.TABLE_ROW_HEIGHT);
        borrowTable.setFont(AppConstants.FONT_BODY);
        borrowTable.getTableHeader().setBackground(AppConstants.BG_CARD_HOVER);
        borrowTable.getTableHeader().setForeground(AppConstants.TEXT_PRIMARY);
        borrowTable.getTableHeader().setFont(AppConstants.FONT_HEADING);
        
        borrowTable.getColumnModel().getColumn(0).setMinWidth(0);
        borrowTable.getColumnModel().getColumn(0).setMaxWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(borrowTable);
        scrollPane.getViewport().setBackground(AppConstants.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        
        return scrollPane;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppConstants.PADDING_SM, 0));
        panel.setBackground(AppConstants.BG_DARK);
        
        JButton addBtn = createButton("Tạo phiếu mượn", AppConstants.ACCENT);
        addBtn.setIcon(Icons.button("plus"));
        addBtn.addActionListener(e -> showCreateBorrowDialog());
        panel.add(addBtn);
        
        JButton returnBtn = createButton("Trả sách", AppConstants.PRIMARY);
        returnBtn.setIcon(Icons.button("check"));
        returnBtn.addActionListener(e -> returnBorrow());
        panel.add(returnBtn);
        
        JButton viewBtn = createButton("Xem chi tiết", AppConstants.PRIMARY_DARK);
        viewBtn.setIcon(Icons.button("eye"));
        viewBtn.addActionListener(e -> viewBorrowDetails());
        panel.add(viewBtn);
        
        JButton overdueBtn = createButton("Cập nhật quá hạn", AppConstants.WARNING);
        overdueBtn.setIcon(Icons.button("alert-triangle"));
        overdueBtn.addActionListener(e -> updateOverdueStatus());
        panel.add(overdueBtn);
        
        JButton refreshBtn = createButton("Làm mới", AppConstants.PRIMARY_DARK);
        refreshBtn.setIcon(Icons.button("refresh"));
        refreshBtn.addActionListener(e -> loadBorrows());
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
    
    private void loadBorrows() {
        tableModel.setRowCount(0);
        List<Borrow> borrows = borrowController.getAllBorrows();
        
        for (Borrow borrow : borrows) {
            Customer customer = customerController.getCustomerById(borrow.getCustomerId());
            String customerName = customer != null ? customer.getFullName() : "N/A";
            
            Object[] row = {
                borrow.getId(),
                borrow.getBorrowCode(),
                customerName,
                borrow.getBorrowDate().format(DATETIME_FORMATTER),
                borrow.getDueDate().format(DATETIME_FORMATTER),
                borrow.getReturnDate() != null ? borrow.getReturnDate().format(DATETIME_FORMATTER) : "",
                borrow.getTotalBooks(),
                String.format("%,.0f VNĐ", borrow.getLateFee()),
                getStatusText(borrow.getStatus())
            };
            tableModel.addRow(row);
        }
    }
    
    private void searchBorrows() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        
        List<Borrow> borrows = borrowController.searchBorrows(keyword);
        for (Borrow borrow : borrows) {
            Customer customer = customerController.getCustomerById(borrow.getCustomerId());
            String customerName = customer != null ? customer.getFullName() : "N/A";
            
            Object[] row = {
                borrow.getId(),
                borrow.getBorrowCode(),
                customerName,
                borrow.getBorrowDate().format(DATETIME_FORMATTER),
                borrow.getDueDate().format(DATETIME_FORMATTER),
                borrow.getReturnDate() != null ? borrow.getReturnDate().format(DATETIME_FORMATTER) : "",
                borrow.getTotalBooks(),
                String.format("%,.0f VNĐ", borrow.getLateFee()),
                getStatusText(borrow.getStatus())
            };
            tableModel.addRow(row);
        }
    }
    
    private void filterByStatus() {
        String selected = (String) statusFilter.getSelectedItem();
        if ("Tất cả".equals(selected)) {
            loadBorrows();
            return;
        }
        
        String status = switch (selected) {
            case "Đang mượn" -> AppConstants.BORROW_STATUS_BORROWED;
            case "Đã trả" -> AppConstants.BORROW_STATUS_RETURNED;
            case "Quá hạn" -> AppConstants.BORROW_STATUS_OVERDUE;
            default -> null;
        };
        
        if (status != null) {
            tableModel.setRowCount(0);
            List<Borrow> borrows = borrowController.getBorrowsByStatus(status);
            for (Borrow borrow : borrows) {
                Customer customer = customerController.getCustomerById(borrow.getCustomerId());
                String customerName = customer != null ? customer.getFullName() : "N/A";
                
                Object[] row = {
                    borrow.getId(),
                    borrow.getBorrowCode(),
                    customerName,
                    borrow.getBorrowDate().format(DATETIME_FORMATTER),
                    borrow.getDueDate().format(DATETIME_FORMATTER),
                    borrow.getReturnDate() != null ? borrow.getReturnDate().format(DATETIME_FORMATTER) : "",
                    borrow.getTotalBooks(),
                    String.format("%,.0f VNĐ", borrow.getLateFee()),
                    getStatusText(borrow.getStatus())
                };
                tableModel.addRow(row);
            }
        }
    }
    
    private void showCreateBorrowDialog() {
        CreateBorrowDialog dialog = new CreateBorrowDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadBorrows();
        }
    }
    
    private void returnBorrow() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu mượn!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int borrowId = (int) tableModel.getValueAt(selectedRow, 0);
        Borrow borrow = borrowController.getBorrowById(borrowId);
        
        if (borrow.isReturned()) {
            JOptionPane.showMessageDialog(this, "Phiếu mượn đã được trả!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Xác nhận trả sách cho phiếu mượn " + borrow.getBorrowCode() + "?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (borrowController.returnBorrow(borrowId, null)) {
                    JOptionPane.showMessageDialog(this, "Trả sách thành công!", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadBorrows();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void viewBorrowDetails() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu mượn!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int borrowId = (int) tableModel.getValueAt(selectedRow, 0);
        
        BorrowDetailsDialog dialog = new BorrowDetailsDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), borrowId);
        dialog.setVisible(true);
        
        // Reload if returned
        loadBorrows();
    }
    
    private void updateOverdueStatus() {
        int updated = borrowController.updateOverdueStatus();
        JOptionPane.showMessageDialog(this, 
            "Đã cập nhật " + updated + " phiếu mượn quá hạn", 
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        loadBorrows();
    }
    
    private String getStatusText(String status) {
        return switch (status) {
            case AppConstants.BORROW_STATUS_BORROWED -> "Đang mượn";
            case AppConstants.BORROW_STATUS_RETURNED -> "Đã trả";
            case AppConstants.BORROW_STATUS_OVERDUE -> "Quá hạn";
            default -> status;
        };
    }
}
