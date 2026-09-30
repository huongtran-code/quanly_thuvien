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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BorrowDetailsDialog extends JDialog {
    private final BorrowController borrowController;
    private final CustomerController customerController;
    private final DocumentDAO documentDAO;
    private final Borrow borrow;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public BorrowDetailsDialog(Frame parent, int borrowId) {
        super(parent, "Chi tiết phiếu mượn", true);
        
        this.borrowController = new BorrowController();
        this.customerController = new CustomerController();
        this.documentDAO = new DocumentDAO();
        this.borrow = borrowController.getBorrowById(borrowId);
        
        if (borrow == null) {
            JOptionPane.showMessageDialog(parent, "Không tìm thấy phiếu mượn!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        initComponents();
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppConstants.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Chi tiết phiếu mượn: " + borrow.getBorrowCode());
        titleLabel.setFont(AppConstants.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Status badge
        JLabel statusLabel = new JLabel(getStatusText(borrow.getStatus()));
        statusLabel.setFont(AppConstants.FONT_BODY);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(getStatusColor(borrow.getStatus()));
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        headerPanel.add(statusLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Info panel
        JPanel infoPanel = createInfoPanel();
        contentPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Items table
        JPanel itemsPanel = createItemsPanel();
        contentPanel.add(itemsPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        if (!borrow.isReturned()) {
            JButton returnBtn = new JButton("Trả sách", Icons.button("check"));
            returnBtn.setFont(AppConstants.FONT_BODY);
            returnBtn.setBackground(AppConstants.ACCENT);
            returnBtn.setForeground(Color.WHITE);
            returnBtn.addActionListener(e -> returnBorrow());
            buttonPanel.add(returnBtn);
        }
        
        JButton closeBtn = new JButton("Đóng", Icons.button("x"));
        closeBtn.setFont(AppConstants.FONT_BODY);
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Thông tin phiếu mượn"));
        
        // Get customer
        Customer customer = customerController.getCustomerById(borrow.getCustomerId());
        String customerName = customer != null ? customer.getFullName() : "N/A";
        
        addInfoRow(panel, "Mã phiếu:", borrow.getBorrowCode());
        addInfoRow(panel, "Khách hàng:", customerName);
        addInfoRow(panel, "Ngày mượn:", borrow.getBorrowDate().format(DATE_FORMATTER));
        addInfoRow(panel, "Hạn trả:", borrow.getDueDate().format(DATE_FORMATTER));
        
        if (borrow.getReturnDate() != null) {
            addInfoRow(panel, "Ngày trả:", borrow.getReturnDate().format(DATE_FORMATTER));
        }
        
        addInfoRow(panel, "Tổng số sách:", String.valueOf(borrow.getTotalBooks()));
        
        if (borrow.getLateFee() > 0) {
            addInfoRow(panel, "Phí trễ hạn:", String.format("%,.0f VNĐ", borrow.getLateFee()));
        }
        
        if (borrow.isOverdue() && !borrow.isReturned()) {
            long lateDays = borrow.calculateLateDays();
            JLabel lateLabel = new JLabel("Trễ hạn: " + lateDays + " ngày");
            lateLabel.setForeground(AppConstants.DANGER);
            lateLabel.setFont(AppConstants.FONT_BODY.deriveFont(Font.BOLD));
            panel.add(new JLabel());
            panel.add(lateLabel);
        }
        
        return panel;
    }
    
    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(AppConstants.FONT_BODY);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(AppConstants.FONT_BODY.deriveFont(Font.BOLD));
        
        panel.add(lblLabel);
        panel.add(lblValue);
    }
    
    private JPanel createItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Danh sách sách mượn"));
        
        String[] columns = {"Mã tài liệu", "Tên sách", "Tác giả", "Số lượng"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setFont(AppConstants.FONT_BODY);
        table.setRowHeight(30);
        table.getTableHeader().setFont(AppConstants.FONT_BODY.deriveFont(Font.BOLD));
        
        // Load items
        List<BorrowItem> items = borrowController.getBorrowItems(borrow.getId());
        for (BorrowItem item : items) {
            Document doc = documentDAO.findById(item.getDocumentId());
            if (doc != null) {
                model.addRow(new Object[]{
                    doc.getId(),
                    doc.getTitle(),
                    doc.getAuthor(),
                    item.getQuantity()
                });
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void returnBorrow() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xác nhận trả sách cho phiếu mượn: " + borrow.getBorrowCode() + "?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (borrowController.returnBorrow(borrow.getId(), null)) {
                    JOptionPane.showMessageDialog(this, "Trả sách thành công!", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Trả sách thất bại!", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private String getStatusText(String status) {
        switch (status) {
            case AppConstants.BORROW_STATUS_BORROWED:
                return "Đang mượn";
            case AppConstants.BORROW_STATUS_RETURNED:
                return "Đã trả";
            case AppConstants.BORROW_STATUS_OVERDUE:
                return "Quá hạn";
            default:
                return status;
        }
    }
    
    private Color getStatusColor(String status) {
        switch (status) {
            case AppConstants.BORROW_STATUS_BORROWED:
                return AppConstants.PRIMARY;
            case AppConstants.BORROW_STATUS_RETURNED:
                return AppConstants.ACCENT;
            case AppConstants.BORROW_STATUS_OVERDUE:
                return AppConstants.DANGER;
            default:
                return Color.GRAY;
        }
    }
}
