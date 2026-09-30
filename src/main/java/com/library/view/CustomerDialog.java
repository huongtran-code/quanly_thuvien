package com.library.view;

import com.library.model.Customer;
import com.library.util.AppConstants;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CustomerDialog extends JDialog {
    
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JTextField dobField;
    private JTextField idCardField;
    private JTextArea notesArea;
    private JComboBox<String> statusCombo;
    
    private boolean confirmed = false;
    private Customer customer;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public CustomerDialog(Frame parent, Customer existingCustomer) {
        super(parent, existingCustomer == null ? "Thêm Khách Hàng" : "Sửa Khách Hàng", true);
        this.customer = existingCustomer;
        
        initComponents();
        
        if (existingCustomer != null) {
            loadCustomerData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(AppConstants.PADDING, AppConstants.PADDING));
        getContentPane().setBackground(AppConstants.BG_DARK);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(AppConstants.BG_DARK);
        formPanel.setBorder(BorderFactory.createEmptyBorder(AppConstants.PADDING, AppConstants.PADDING, 
            AppConstants.PADDING, AppConstants.PADDING));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        int row = 0;
        
        // Full Name
        addLabel(formPanel, "Họ tên:", gbc, 0, row);
        fullNameField = new JTextField(25);
        styleTextField(fullNameField);
        gbc.gridx = 1;
        gbc.gridy = row++;
        formPanel.add(fullNameField, gbc);
        
        // Email
        addLabel(formPanel, "Email:", gbc, 0, row);
        emailField = new JTextField(25);
        styleTextField(emailField);
        gbc.gridx = 1;
        gbc.gridy = row++;
        formPanel.add(emailField, gbc);
        
        // Phone
        addLabel(formPanel, "Số điện thoại:", gbc, 0, row);
        phoneField = new JTextField(25);
        styleTextField(phoneField);
        gbc.gridx = 1;
        gbc.gridy = row++;
        formPanel.add(phoneField, gbc);
        
        // Address
        addLabel(formPanel, "Địa chỉ:", gbc, 0, row);
        addressArea = new JTextArea(3, 25);
        styleTextArea(addressArea);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        gbc.gridx = 1;
        gbc.gridy = row++;
        formPanel.add(addressScroll, gbc);
        
        // Date of Birth
        addLabel(formPanel, "Ngày sinh (dd/MM/yyyy):", gbc, 0, row);
        dobField = new JTextField(25);
        styleTextField(dobField);
        gbc.gridx = 1;
        gbc.gridy = row++;
        formPanel.add(dobField, gbc);
        
        // ID Card
        addLabel(formPanel, "CMND/CCCD:", gbc, 0, row);
        idCardField = new JTextField(25);
        styleTextField(idCardField);
        gbc.gridx = 1;
        gbc.gridy = row++;
        formPanel.add(idCardField, gbc);
        
        // Status
        addLabel(formPanel, "Trạng thái:", gbc, 0, row);
        statusCombo = new JComboBox<>(new String[]{"Hoạt động", "Tạm ngưng", "Không hoạt động"});
        statusCombo.setBackground(AppConstants.BG_INPUT);
        statusCombo.setForeground(AppConstants.TEXT_PRIMARY);
        gbc.gridx = 1;
        gbc.gridy = row++;
        formPanel.add(statusCombo, gbc);
        
        // Notes
        addLabel(formPanel, "Ghi chú:", gbc, 0, row);
        notesArea = new JTextArea(3, 25);
        styleTextArea(notesArea);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        gbc.gridx = 1;
        gbc.gridy = row++;
        formPanel.add(notesScroll, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(AppConstants.BG_DARK);
        
        JButton saveBtn = new JButton("Lưu");
        saveBtn.setBackground(AppConstants.ACCENT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> saveCustomer());
        buttonPanel.add(saveBtn);
        
        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setBackground(AppConstants.DANGER);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(cancelBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addLabel(JPanel panel, String text, GridBagConstraints gbc, int x, int y) {
        JLabel label = new JLabel(text);
        label.setForeground(AppConstants.TEXT_PRIMARY);
        label.setFont(AppConstants.FONT_BODY);
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(label, gbc);
    }
    
    private void styleTextField(JTextField field) {
        field.setBackground(AppConstants.BG_INPUT);
        field.setForeground(AppConstants.TEXT_PRIMARY);
        field.setCaretColor(AppConstants.TEXT_PRIMARY);
        field.setFont(AppConstants.FONT_BODY);
    }
    
    private void styleTextArea(JTextArea area) {
        area.setBackground(AppConstants.BG_INPUT);
        area.setForeground(AppConstants.TEXT_PRIMARY);
        area.setCaretColor(AppConstants.TEXT_PRIMARY);
        area.setFont(AppConstants.FONT_BODY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    }
    
    private void loadCustomerData() {
        fullNameField.setText(customer.getFullName());
        emailField.setText(customer.getEmail());
        phoneField.setText(customer.getPhone());
        addressArea.setText(customer.getAddress());
        
        if (customer.getDateOfBirth() != null) {
            dobField.setText(customer.getDateOfBirth().format(DATE_FORMATTER));
        }
        
        idCardField.setText(customer.getIdCard());
        notesArea.setText(customer.getNotes());
        
        String status = switch (customer.getStatus()) {
            case AppConstants.CUSTOMER_STATUS_ACTIVE -> "Hoạt động";
            case AppConstants.CUSTOMER_STATUS_SUSPENDED -> "Tạm ngưng";
            case AppConstants.CUSTOMER_STATUS_INACTIVE -> "Không hoạt động";
            default -> "Hoạt động";
        };
        statusCombo.setSelectedItem(status);
    }
    
    private void saveCustomer() {
        // Validate
        if (fullNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập email!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Parse date of birth
        LocalDate dob = null;
        if (!dobField.getText().trim().isEmpty()) {
            try {
                dob = LocalDate.parse(dobField.getText().trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng (dd/MM/yyyy)!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Create or update customer
        if (customer == null) {
            customer = new Customer();
        }
        
        customer.setFullName(fullNameField.getText().trim());
        customer.setEmail(emailField.getText().trim());
        customer.setPhone(phoneField.getText().trim());
        customer.setAddress(addressArea.getText().trim());
        customer.setDateOfBirth(dob);
        customer.setIdCard(idCardField.getText().trim());
        customer.setNotes(notesArea.getText().trim());
        
        String selectedStatus = (String) statusCombo.getSelectedItem();
        String status = switch (selectedStatus) {
            case "Hoạt động" -> AppConstants.CUSTOMER_STATUS_ACTIVE;
            case "Tạm ngưng" -> AppConstants.CUSTOMER_STATUS_SUSPENDED;
            case "Không hoạt động" -> AppConstants.CUSTOMER_STATUS_INACTIVE;
            default -> AppConstants.CUSTOMER_STATUS_ACTIVE;
        };
        customer.setStatus(status);
        
        if (customer.getRegistrationDate() == null) {
            customer.setRegistrationDate(LocalDate.now());
        }
        
        confirmed = true;
        dispose();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public Customer getCustomer() {
        return customer;
    }
}
