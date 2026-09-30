package com.library.view;

import com.library.controller.CustomerController;
import com.library.controller.SaleController;
import com.library.dao.DocumentDAO;
import com.library.model.Customer;
import com.library.model.Document;
import com.library.model.Sale;
import com.library.model.SaleItem;
import com.library.util.AppConstants;
import com.library.util.CurrencyUtil;
import com.library.util.Icons;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SaleDetailDialog extends JDialog {
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final SaleController saleController;
    private final CustomerController customerController;
    private final DocumentDAO documentDAO;
    
    public SaleDetailDialog(Frame parent, int saleId) {
        super(parent, "Chi Tiết Đơn Bán", true);
        this.saleController = new SaleController();
        this.customerController = new CustomerController();
        this.documentDAO = new DocumentDAO();
        
        initComponents(saleId);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents(int saleId) {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(AppConstants.BG_DARK);
        
        Sale sale = saleController.getSaleById(saleId);
        if (sale == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy đơn bán!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        List<SaleItem> items = saleController.getSaleItems(saleId);
        
        add(createHeaderPanel(sale), BorderLayout.NORTH);
        add(createItemsPanel(items), BorderLayout.CENTER);
        add(createSummaryPanel(sale), BorderLayout.EAST);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        setSize(900, 600);
    }
    
    private JPanel createHeaderPanel(Sale sale) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppConstants.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        addLabel(panel, "Mã đơn:", gbc);
        
        gbc.gridx = 1;
        addValue(panel, sale.getSaleCode(), gbc);
        
        gbc.gridx = 2;
        addLabel(panel, "Ngày bán:", gbc);
        
        gbc.gridx = 3;
        addValue(panel, sale.getSaleDate().format(DATETIME_FORMATTER), gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        addLabel(panel, "Khách hàng:", gbc);
        
        gbc.gridx = 1;
        String customerName = "Khách vãng lai";
        if (sale.getCustomerId() != null) {
            Customer customer = customerController.getCustomerById(sale.getCustomerId());
            if (customer != null) customerName = customer.getFullName();
        }
        addValue(panel, customerName, gbc);
        
        gbc.gridx = 2;
        addLabel(panel, "Thanh toán:", gbc);
        
        gbc.gridx = 3;
        String paymentMethod = switch (sale.getPaymentMethod()) {
            case AppConstants.PAYMENT_CARD -> "Thẻ";
            case AppConstants.PAYMENT_TRANSFER -> "Chuyển khoản";
            default -> "Tiền mặt";
        };
        addValue(panel, paymentMethod, gbc);
        
        return panel;
    }
    
    private void addLabel(JPanel panel, String text, GridBagConstraints gbc) {
        JLabel label = new JLabel(text);
        label.setForeground(AppConstants.TEXT_SECONDARY);
        label.setFont(AppConstants.FONT_BODY);
        panel.add(label, gbc);
    }
    
    private void addValue(JPanel panel, String text, GridBagConstraints gbc) {
        JLabel label = new JLabel(text);
        label.setForeground(AppConstants.TEXT_PRIMARY);
        label.setFont(AppConstants.FONT_HEADING);
        panel.add(label, gbc);
    }
    
    private JPanel createItemsPanel(List<SaleItem> items) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AppConstants.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Danh sách sách");
        titleLabel.setForeground(AppConstants.TEXT_PRIMARY);
        titleLabel.setFont(AppConstants.FONT_HEADING);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Tên sách", "Đơn giá", "Số lượng", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        for (SaleItem item : items) {
            Document doc = documentDAO.findById(item.getDocumentId());
            String docTitle = doc != null ? doc.getTitle() : "ID: " + item.getDocumentId();
            
            model.addRow(new Object[]{
                item.getDocumentId(),
                docTitle,
                CurrencyUtil.format(item.getUnitPrice()),
                item.getQuantity(),
                CurrencyUtil.format(item.getSubtotal())
            });
        }
        
        JTable table = new JTable(model);
        table.setBackground(AppConstants.BG_CARD);
        table.setForeground(AppConstants.TEXT_PRIMARY);
        table.setSelectionBackground(AppConstants.PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(AppConstants.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSummaryPanel(Sale sale) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppConstants.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(250, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        
        gbc.gridy = 0;
        JLabel titleLabel = new JLabel("TỔNG TIỀN");
        titleLabel.setFont(AppConstants.FONT_TITLE);
        titleLabel.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        JLabel totalTitleLabel = new JLabel("Tổng cộng:");
        totalTitleLabel.setForeground(AppConstants.TEXT_SECONDARY);
        panel.add(totalTitleLabel, gbc);
        
        gbc.gridy = 2;
        JLabel totalLabel = new JLabel(CurrencyUtil.format(sale.getTotalAmount()));
        totalLabel.setFont(AppConstants.FONT_HEADING);
        totalLabel.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(totalLabel, gbc);
        
        gbc.gridy = 3;
        JLabel discountTitleLabel = new JLabel("Giảm giá:");
        discountTitleLabel.setForeground(AppConstants.TEXT_SECONDARY);
        panel.add(discountTitleLabel, gbc);
        
        gbc.gridy = 4;
        JLabel discountLabel = new JLabel(CurrencyUtil.format(sale.getDiscountAmount()));
        discountLabel.setFont(AppConstants.FONT_HEADING);
        discountLabel.setForeground(AppConstants.ACCENT);
        panel.add(discountLabel, gbc);
        
        gbc.gridy = 5;
        JSeparator separator = new JSeparator();
        panel.add(separator, gbc);
        
        gbc.gridy = 6;
        JLabel finalTitleLabel = new JLabel("Thành tiền:");
        finalTitleLabel.setForeground(AppConstants.TEXT_SECONDARY);
        panel.add(finalTitleLabel, gbc);
        
        gbc.gridy = 7;
        JLabel finalAmountLabel = new JLabel(CurrencyUtil.format(sale.getFinalAmount()));
        finalAmountLabel.setFont(AppConstants.FONT_TITLE);
        finalAmountLabel.setForeground(AppConstants.ACCENT);
        panel.add(finalAmountLabel, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(AppConstants.BG_DARK);
        
        JButton closeBtn = new JButton("Đóng");
        closeBtn.setIcon(Icons.button("x"));
        closeBtn.setBackground(AppConstants.DANGER);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> dispose());
        panel.add(closeBtn);
        
        return panel;
    }
}
