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
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class CreateSaleDialog extends JDialog {
    
    private final SaleController saleController;
    private final CustomerController customerController;
    private final DocumentDAO documentDAO;
    
    private JComboBox<CustomerItem> customerCombo;
    private JComboBox<String> paymentMethodCombo;
    private DefaultTableModel itemsTableModel;
    private JTable itemsTable;
    private JTextField documentIdField;
    private JSpinner quantitySpinner;
    private JLabel totalLabel;
    private JLabel discountLabel;
    private JLabel finalAmountLabel;
    
    private boolean success = false;
    
    public CreateSaleDialog(Frame parent) {
        super(parent, "Tạo Đơn Bán Sách", true);
        this.saleController = new SaleController();
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
        add(createSummaryPanel(), BorderLayout.EAST);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        setSize(900, 600);
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
        JLabel customerLabel = new JLabel("Khách hàng (tùy chọn):");
        customerLabel.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(customerLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        customerCombo = new JComboBox<>();
        customerCombo.setBackground(AppConstants.BG_INPUT);
        customerCombo.setForeground(AppConstants.TEXT_PRIMARY);
        customerCombo.addActionListener(e -> updateSummary());
        panel.add(customerCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel paymentLabel = new JLabel("Phương thức thanh toán:");
        paymentLabel.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(paymentLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        paymentMethodCombo = new JComboBox<>(new String[]{"Tiền mặt", "Thẻ", "Chuyển khoản"});
        paymentMethodCombo.setBackground(AppConstants.BG_INPUT);
        paymentMethodCombo.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(paymentMethodCombo, gbc);
        
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
        
        JLabel docLabel = new JLabel("Tài liệu:");
        docLabel.setForeground(AppConstants.TEXT_PRIMARY);
        addItemPanel.add(docLabel);
        
        documentIdField = new JTextField(8);
        documentIdField.setBackground(AppConstants.BG_INPUT);
        documentIdField.setForeground(AppConstants.TEXT_PRIMARY);
        documentIdField.setToolTipText("Nhập ID tài liệu hoặc click 'Tìm sách'");
        addItemPanel.add(documentIdField);
        
        JButton searchDocBtn = new JButton("Tìm sách");
        searchDocBtn.setIcon(Icons.button("search"));
        searchDocBtn.setBackground(AppConstants.PRIMARY);
        searchDocBtn.setForeground(Color.WHITE);
        searchDocBtn.addActionListener(e -> showDocumentSearchDialog());
        addItemPanel.add(searchDocBtn);
        
        JLabel qtyLabel = new JLabel("Số lượng:");
        qtyLabel.setForeground(AppConstants.TEXT_PRIMARY);
        addItemPanel.add(qtyLabel);
        
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        quantitySpinner.setPreferredSize(new Dimension(60, 25));
        addItemPanel.add(quantitySpinner);
        
        JButton addBtn = new JButton("Thêm sách");
        addBtn.setIcon(Icons.button("plus"));
        addBtn.setBackground(AppConstants.ACCENT);
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> addItem());
        addItemPanel.add(addBtn);
        
        panel.add(addItemPanel, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Tên sách", "Đơn giá", "Số lượng", "Thành tiền"};
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
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppConstants.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(250, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel summaryTitle = new JLabel("TỔNG TIỀN");
        summaryTitle.setFont(AppConstants.FONT_HEADING);
        summaryTitle.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(summaryTitle, gbc);
        
        gbc.gridy = 1;
        JLabel totalTitleLabel = new JLabel("Tổng cộng:");
        totalTitleLabel.setForeground(AppConstants.TEXT_SECONDARY);
        panel.add(totalTitleLabel, gbc);
        
        gbc.gridy = 2;
        totalLabel = new JLabel("0 VND");
        totalLabel.setFont(AppConstants.FONT_SUBTITLE);
        totalLabel.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(totalLabel, gbc);
        
        gbc.gridy = 3;
        JLabel discountTitleLabel = new JLabel("Giảm giá:");
        discountTitleLabel.setForeground(AppConstants.TEXT_SECONDARY);
        panel.add(discountTitleLabel, gbc);
        
        gbc.gridy = 4;
        discountLabel = new JLabel("0 VND");
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
        finalAmountLabel = new JLabel("0 VND");
        finalAmountLabel.setFont(AppConstants.FONT_TITLE);
        finalAmountLabel.setForeground(AppConstants.ACCENT);
        panel.add(finalAmountLabel, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(AppConstants.BG_DARK);
        
        JButton saveBtn = new JButton("Tạo đơn bán");
        saveBtn.setIcon(Icons.button("check"));
        saveBtn.setBackground(AppConstants.ACCENT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> createSale());
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
        customerCombo.addItem(new CustomerItem(null));
        
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
            
            double subtotal = doc.getUnitPrice() * quantity;
            itemsTableModel.addRow(new Object[]{
                documentId, 
                doc.getTitle(), 
                CurrencyUtil.format(doc.getUnitPrice()),
                quantity, 
                CurrencyUtil.format(subtotal)
            });
            
            documentIdField.setText("");
            quantitySpinner.setValue(1);
            updateSummary();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID tài liệu không hợp lệ!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeSelectedItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow >= 0) {
            itemsTableModel.removeRow(selectedRow);
            updateSummary();
        }
    }
    
    private void showDocumentSearchDialog() {
        List<Document> allDocs = documentDAO.findAll();
        
        if (allDocs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có tài liệu nào trong hệ thống!", 
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Tạo dialog chọn sách
        JDialog searchDialog = new JDialog(this, "Chọn Tài Liệu", true);
        searchDialog.setLayout(new BorderLayout(10, 10));
        searchDialog.getContentPane().setBackground(AppConstants.BG_DARK);
        
        // ── Panel tìm kiếm ──
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(AppConstants.BG_CARD);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstants.BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel searchIcon = new JLabel(Icons.get("search", 16, AppConstants.TEXT_SECONDARY));
        searchPanel.add(searchIcon, BorderLayout.WEST);

        JTextField filterField = new JTextField();
        filterField.setBackground(AppConstants.BG_INPUT);
        filterField.setForeground(AppConstants.TEXT_PRIMARY);
        filterField.setCaretColor(AppConstants.TEXT_PRIMARY);
        filterField.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        filterField.setFont(AppConstants.FONT_BODY);
        filterField.setToolTipText("Tìm theo tên sách, tác giả hoặc ID...");

        JLabel hintLabel = new JLabel("Tìm theo tên sách, tác giả hoặc ID");
        hintLabel.setForeground(AppConstants.TEXT_MUTED);
        hintLabel.setFont(AppConstants.FONT_SMALL);

        JPanel fieldPanel = new JPanel(new BorderLayout(4, 2));
        fieldPanel.setOpaque(false);
        fieldPanel.add(filterField, BorderLayout.CENTER);
        fieldPanel.add(hintLabel, BorderLayout.SOUTH);
        searchPanel.add(fieldPanel, BorderLayout.CENTER);

        searchDialog.add(searchPanel, BorderLayout.NORTH);
        
        // ── Bảng hiển thị sách ──
        String[] columns = {"ID", "Tên sách", "Tác giả", "Đơn giá", "Tồn kho"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 || col == 4 ? Integer.class : Object.class;
            }
        };
        
        for (Document doc : allDocs) {
            if (doc.getStockQuantity() > 0) {
                model.addRow(new Object[]{
                    doc.getId(),
                    doc.getTitle(),
                    doc.getAuthor(),
                    CurrencyUtil.format(doc.getUnitPrice()),
                    doc.getStockQuantity()
                });
            }
        }
        
        JTable table = new JTable(model);
        table.setBackground(AppConstants.BG_CARD);
        table.setForeground(AppConstants.TEXT_PRIMARY);
        table.setSelectionBackground(AppConstants.PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(30);
        table.setFont(AppConstants.FONT_BODY);
        table.getTableHeader().setBackground(AppConstants.BG_CARD_HOVER);
        table.getTableHeader().setForeground(AppConstants.TEXT_PRIMARY);
        table.getTableHeader().setFont(AppConstants.FONT_HEADING);

        // Cột ID hẹp hơn
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(70);

        // ── Bộ lọc tìm kiếm thời gian thực ──
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        filterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = filterField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    // Lọc theo cột ID (0), Tên sách (1), Tác giả (2)
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 0, 1, 2));
                }
            }
        });

        // Nhấn Enter để chọn dòng đang highlight
        filterField.addActionListener(e -> {
            if (table.getRowCount() == 1) {
                table.setRowSelectionInterval(0, 0);
            }
        });

        // Double-click để chọn nhanh
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                    int docId = (Integer) model.getValueAt(modelRow, 0);
                    documentIdField.setText(String.valueOf(docId));
                    searchDialog.dispose();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(AppConstants.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        searchDialog.add(scrollPane, BorderLayout.CENTER);
        
        // ── Nút chọn / hủy ──
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(AppConstants.BG_DARK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));
        
        JButton selectBtn = new JButton("Chọn");
        selectBtn.setIcon(Icons.button("check"));
        selectBtn.setBackground(AppConstants.ACCENT);
        selectBtn.setForeground(Color.WHITE);
        selectBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                int docId = (Integer) model.getValueAt(modelRow, 0);
                documentIdField.setText(String.valueOf(docId));
                searchDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(searchDialog, "Vui lòng chọn một tài liệu!", 
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(selectBtn);
        
        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setIcon(Icons.button("x"));
        cancelBtn.setBackground(AppConstants.DANGER);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> searchDialog.dispose());
        buttonPanel.add(cancelBtn);
        
        searchDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        searchDialog.setSize(720, 520);
        searchDialog.setLocationRelativeTo(this);
        searchDialog.setVisible(true);
    }
    
    private void updateSummary() {
        double total = 0;
        for (int i = 0; i < itemsTableModel.getRowCount(); i++) {
            int docId = (Integer) itemsTableModel.getValueAt(i, 0);
            int qty = (Integer) itemsTableModel.getValueAt(i, 3);
            Document doc = documentDAO.findById(docId);
            if (doc != null) {
                total += doc.getUnitPrice() * qty;
            }
        }
        
        double discount = 0;
        CustomerItem selected = (CustomerItem) customerCombo.getSelectedItem();
        if (selected != null && selected.customer != null) {
            // Tính giảm giá từ gói thành viên của khách hàng
            discount = saleController.calculateDiscount(selected.customer.getId(), total);
        }
        
        double finalAmount = total - discount;
        
        totalLabel.setText(CurrencyUtil.format(total));
        discountLabel.setText(CurrencyUtil.format(discount));
        finalAmountLabel.setText(CurrencyUtil.format(finalAmount));
    }
    
    private void createSale() {
        if (itemsTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng thêm ít nhất 1 cuốn sách!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<SaleItem> items = new ArrayList<>();
        for (int i = 0; i < itemsTableModel.getRowCount(); i++) {
            int docId = (Integer) itemsTableModel.getValueAt(i, 0);
            int qty = (Integer) itemsTableModel.getValueAt(i, 3);
            
            SaleItem item = new SaleItem();
            item.setDocumentId(docId);
            item.setQuantity(qty);
            items.add(item);
        }
        
        try {
            Sale sale = new Sale();
            
            CustomerItem selected = (CustomerItem) customerCombo.getSelectedItem();
            if (selected != null && selected.customer != null) {
                sale.setCustomerId(selected.customer.getId());
            }
            
            String paymentMethod = switch (paymentMethodCombo.getSelectedIndex()) {
                case 1 -> AppConstants.PAYMENT_CARD;
                case 2 -> AppConstants.PAYMENT_TRANSFER;
                default -> AppConstants.PAYMENT_CASH;
            };
            sale.setPaymentMethod(paymentMethod);
            
            if (saleController.createSale(sale, items, null)) {
                success = true;
                JOptionPane.showMessageDialog(this, "Tạo đơn bán thành công!", 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể tạo đơn bán. Vui lòng thử lại!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
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
            if (customer == null) {
                return "Khách vãng lai";
            }
            return customer.getCustomerCode() + " - " + customer.getFullName();
        }
    }
}
