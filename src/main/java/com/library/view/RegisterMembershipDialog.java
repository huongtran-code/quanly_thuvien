package com.library.view;

import com.library.controller.CustomerController;
import com.library.dao.CustomerMembershipDAO;
import com.library.dao.MembershipTierDAO;
import com.library.model.Customer;
import com.library.model.CustomerMembership;
import com.library.model.MembershipTier;
import com.library.util.AppConstants;
import com.library.util.CurrencyUtil;
import com.library.util.Icons;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class RegisterMembershipDialog extends JDialog {
    
    private final CustomerMembershipDAO membershipDAO;
    private final MembershipTierDAO tierDAO;
    private final CustomerController customerController;
    
    private JComboBox<CustomerItem> customerCombo;
    private JComboBox<TierItem> tierCombo;
    private JSpinner monthsSpinner;
    private JTextArea tierDetailsArea;
    private JLabel totalPriceLabel;
    
    private boolean success = false;
    
    public RegisterMembershipDialog(Frame parent) {
        super(parent, "Đăng Ký Gói Thành Viên", true);
        this.membershipDAO = new CustomerMembershipDAO();
        this.tierDAO = new MembershipTierDAO();
        this.customerController = new CustomerController();
        
        initComponents();
        loadData();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(AppConstants.BG_DARK);
        
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        setSize(600, 500);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppConstants.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Customer
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel customerLabel = new JLabel("Khách hàng:");
        customerLabel.setForeground(AppConstants.TEXT_PRIMARY);
        customerLabel.setFont(AppConstants.FONT_HEADING);
        panel.add(customerLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        customerCombo = new JComboBox<>();
        customerCombo.setBackground(AppConstants.BG_INPUT);
        customerCombo.setForeground(AppConstants.TEXT_PRIMARY);
        customerCombo.setFont(AppConstants.FONT_BODY);
        panel.add(customerCombo, gbc);
        
        // Tier
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel tierLabel = new JLabel("Gói thành viên:");
        tierLabel.setForeground(AppConstants.TEXT_PRIMARY);
        tierLabel.setFont(AppConstants.FONT_HEADING);
        panel.add(tierLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        tierCombo = new JComboBox<>();
        tierCombo.setBackground(AppConstants.BG_INPUT);
        tierCombo.setForeground(AppConstants.TEXT_PRIMARY);
        tierCombo.setFont(AppConstants.FONT_BODY);
        tierCombo.addActionListener(e -> updateTierDetails());
        panel.add(tierCombo, gbc);
        
        // Tier Details
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        tierDetailsArea = new JTextArea(6, 40);
        tierDetailsArea.setEditable(false);
        tierDetailsArea.setBackground(AppConstants.BG_INPUT);
        tierDetailsArea.setForeground(AppConstants.TEXT_PRIMARY);
        tierDetailsArea.setFont(AppConstants.FONT_BODY);
        tierDetailsArea.setLineWrap(true);
        tierDetailsArea.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(tierDetailsArea);
        detailsScroll.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        panel.add(detailsScroll, gbc);
        
        // Duration
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel monthsLabel = new JLabel("Số tháng đăng ký:");
        monthsLabel.setForeground(AppConstants.TEXT_PRIMARY);
        monthsLabel.setFont(AppConstants.FONT_HEADING);
        panel.add(monthsLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel monthsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        monthsPanel.setBackground(AppConstants.BG_CARD);
        monthsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        monthsSpinner.setPreferredSize(new Dimension(100, 30));
        monthsSpinner.setFont(AppConstants.FONT_BODY);
        monthsSpinner.addChangeListener(e -> updateTotalPrice());
        monthsPanel.add(monthsSpinner);
        panel.add(monthsPanel, gbc);
        
        // Total Price
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pricePanel.setBackground(AppConstants.BG_CARD);
        pricePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.ACCENT, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel priceTitleLabel = new JLabel("Tổng chi phí: ");
        priceTitleLabel.setForeground(AppConstants.TEXT_SECONDARY);
        priceTitleLabel.setFont(AppConstants.FONT_HEADING);
        pricePanel.add(priceTitleLabel);
        
        totalPriceLabel = new JLabel("0 VND");
        totalPriceLabel.setForeground(AppConstants.ACCENT);
        totalPriceLabel.setFont(AppConstants.FONT_TITLE);
        pricePanel.add(totalPriceLabel);
        
        panel.add(pricePanel, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(AppConstants.BG_DARK);
        
        JButton saveBtn = new JButton("Đăng ký");
        saveBtn.setIcon(Icons.button("check"));
        saveBtn.setBackground(AppConstants.ACCENT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(AppConstants.FONT_HEADING);
        saveBtn.addActionListener(e -> registerMembership());
        panel.add(saveBtn);
        
        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setIcon(Icons.button("x"));
        cancelBtn.setBackground(AppConstants.DANGER);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(AppConstants.FONT_HEADING);
        cancelBtn.addActionListener(e -> dispose());
        panel.add(cancelBtn);
        
        return panel;
    }
    
    private void loadData() {
        // Load customers
        customerCombo.removeAllItems();
        List<Customer> customers = customerController.getAllCustomers();
        for (Customer customer : customers) {
            if (customer.isActive()) {
                customerCombo.addItem(new CustomerItem(customer));
            }
        }
        
        // Load tiers
        tierCombo.removeAllItems();
        List<MembershipTier> tiers = tierDAO.getAllTiers();
        for (MembershipTier tier : tiers) {
            tierCombo.addItem(new TierItem(tier));
        }
        
        if (tierCombo.getItemCount() > 0) {
            updateTierDetails();
        }
    }
    
    private void updateTierDetails() {
        TierItem selected = (TierItem) tierCombo.getSelectedItem();
        if (selected == null) {
            return;
        }
        
        MembershipTier tier = selected.tier;
        StringBuilder details = new StringBuilder();
        details.append("📚 Quyền lợi gói ").append(tier.getTierName()).append(":\n\n");
        details.append("• Mức giảm giá: ").append(tier.getDiscountPercent()).append("%\n");
        details.append("• Số sách được mượn: ").append(tier.getMaxBooks()).append(" quyển\n");
        details.append("• Thời hạn mượn: ").append(tier.getBorrowDays()).append(" ngày\n");
        details.append("• Giá gói/tháng: ").append(CurrencyUtil.format(tier.getPrice())).append("\n");
        
        tierDetailsArea.setText(details.toString());
        updateTotalPrice();
    }
    
    private void updateTotalPrice() {
        TierItem selected = (TierItem) tierCombo.getSelectedItem();
        if (selected == null) {
            totalPriceLabel.setText("0 VND");
            return;
        }
        
        int months = (Integer) monthsSpinner.getValue();
        double total = selected.tier.getPrice() * months;
        totalPriceLabel.setText(CurrencyUtil.format(total));
    }
    
    private void registerMembership() {
        CustomerItem selectedCustomer = (CustomerItem) customerCombo.getSelectedItem();
        TierItem selectedTier = (TierItem) tierCombo.getSelectedItem();
        
        if (selectedCustomer == null || selectedTier == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ thông tin!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int months = (Integer) monthsSpinner.getValue();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(months);
        
        CustomerMembership membership = new CustomerMembership();
        membership.setCustomerId(selectedCustomer.customer.getId());
        membership.setTierId(selectedTier.tier.getId());
        membership.setStartDate(startDate);
        membership.setEndDate(endDate);
        membership.setStatus("ACTIVE");
        
        try {
            if (membershipDAO.createMembership(membership)) {
                success = true;
                JOptionPane.showMessageDialog(this, 
                    "Đăng ký gói thành viên thành công!\nKhách hàng: " + selectedCustomer.customer.getFullName() + 
                    "\nGói: " + selectedTier.tier.getTierName() + 
                    "\nHiệu lực: " + startDate + " - " + endDate, 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Đăng ký thất bại!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), 
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
    
    private static class TierItem {
        final MembershipTier tier;
        
        TierItem(MembershipTier tier) {
            this.tier = tier;
        }
        
        @Override
        public String toString() {
            return tier.getTierName() + " - " + CurrencyUtil.format(tier.getPrice()) + "/tháng";
        }
    }
}
