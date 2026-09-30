package com.library.view;

import com.library.dao.CustomerMembershipDAO;
import com.library.dao.MembershipTierDAO;
import com.library.controller.CustomerController;
import com.library.model.Customer;
import com.library.model.CustomerMembership;
import com.library.model.MembershipTier;
import com.library.util.AppConstants;
import com.library.util.Icons;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MembershipPanel extends JPanel {
    
    private final CustomerMembershipDAO membershipDAO;
    private final MembershipTierDAO tierDAO;
    private final CustomerController customerController;
    private JTable membershipTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<TierFilterItem> tierFilterCombo;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public MembershipPanel() {
        this.membershipDAO = new CustomerMembershipDAO();
        this.tierDAO = new MembershipTierDAO();
        this.customerController = new CustomerController();
        initComponents();
        loadMemberships();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(AppConstants.PADDING, AppConstants.PADDING));
        setBackground(AppConstants.BG_DARK);
        
        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(AppConstants.BG_DARK);
        
        // Title
        JLabel titleLabel = new JLabel("Quản Lý Gói Thành Viên");
        titleLabel.setFont(AppConstants.FONT_TITLE);
        titleLabel.setForeground(AppConstants.TEXT_PRIMARY);
        panel.add(titleLabel, BorderLayout.WEST);
        
        // Search and filter panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(AppConstants.BG_DARK);
        
        // Search field
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setForeground(AppConstants.TEXT_PRIMARY);
        searchPanel.add(searchLabel);
        
        searchField = new JTextField(20);
        searchField.setBackground(AppConstants.BG_INPUT);
        searchField.setForeground(AppConstants.TEXT_PRIMARY);
        searchField.setToolTipText("Tìm theo tên khách hàng");
        searchPanel.add(searchField);
        
        JButton searchBtn = new JButton("Tìm");
        searchBtn.setIcon(Icons.button("search"));
        searchBtn.setBackground(AppConstants.PRIMARY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.addActionListener(e -> loadMemberships());
        searchPanel.add(searchBtn);
        
        // Tier filter
        JLabel filterLabel = new JLabel("Gói:");
        filterLabel.setForeground(AppConstants.TEXT_PRIMARY);
        searchPanel.add(filterLabel);
        
        tierFilterCombo = new JComboBox<>();
        tierFilterCombo.setBackground(AppConstants.BG_INPUT);
        tierFilterCombo.setForeground(AppConstants.TEXT_PRIMARY);
        loadTierFilter();
        tierFilterCombo.addActionListener(e -> loadMemberships());
        searchPanel.add(tierFilterCombo);
        
        panel.add(searchPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "Khách hàng", "Gói", "Ngày bắt đầu", "Ngày kết thúc", 
                           "Số ngày còn lại", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        membershipTable = new JTable(tableModel);
        membershipTable.setBackground(AppConstants.BG_CARD);
        membershipTable.setForeground(AppConstants.TEXT_PRIMARY);
        membershipTable.setSelectionBackground(AppConstants.PRIMARY);
        membershipTable.setSelectionForeground(Color.WHITE);
        membershipTable.setRowHeight(AppConstants.TABLE_ROW_HEIGHT);
        membershipTable.setFont(AppConstants.FONT_BODY);
        membershipTable.getTableHeader().setBackground(AppConstants.BG_CARD_HOVER);
        membershipTable.getTableHeader().setForeground(AppConstants.TEXT_PRIMARY);
        membershipTable.getTableHeader().setFont(AppConstants.FONT_HEADING);
        
        membershipTable.getColumnModel().getColumn(0).setMinWidth(0);
        membershipTable.getColumnModel().getColumn(0).setMaxWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(membershipTable);
        scrollPane.getViewport().setBackground(AppConstants.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        
        return scrollPane;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppConstants.PADDING_SM, 0));
        panel.setBackground(AppConstants.BG_DARK);
        
        JButton addBtn = createButton("Đăng ký gói", AppConstants.ACCENT);
        addBtn.setIcon(Icons.button("plus"));
        addBtn.addActionListener(e -> showRegisterDialog());
        panel.add(addBtn);
        
        JButton renewBtn = createButton("Gia hạn", AppConstants.PRIMARY);
        renewBtn.setIcon(Icons.button("calendar"));
        renewBtn.addActionListener(e -> renewMembership());
        panel.add(renewBtn);
        
        JButton cancelBtn = createButton("Hủy gói", AppConstants.DANGER);
        cancelBtn.setIcon(Icons.button("x"));
        cancelBtn.addActionListener(e -> cancelMembership());
        panel.add(cancelBtn);
        
        JButton refreshBtn = createButton("Làm mới", AppConstants.PRIMARY_DARK);
        refreshBtn.setIcon(Icons.button("refresh"));
        refreshBtn.addActionListener(e -> loadMemberships());
        panel.add(refreshBtn);
        
        JButton updateExpiredBtn = createButton("Cập nhật hết hạn", AppConstants.WARNING);
        updateExpiredBtn.setIcon(Icons.button("clock"));
        updateExpiredBtn.addActionListener(e -> updateExpiredMemberships());
        panel.add(updateExpiredBtn);
        
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
    
    private void loadMemberships() {
        tableModel.setRowCount(0);
        List<CustomerMembership> memberships = membershipDAO.getAllMemberships();
        
        String searchText = searchField.getText().trim().toLowerCase();
        TierFilterItem selectedTier = (TierFilterItem) tierFilterCombo.getSelectedItem();
        Integer filterTierId = (selectedTier != null && selectedTier.tier != null) ? selectedTier.tier.getId() : null;
        
        // Batch load all customers and tiers to avoid N+1 queries
        java.util.Map<Integer, Customer> customerMap = new java.util.HashMap<>();
        java.util.Map<Integer, MembershipTier> tierMap = new java.util.HashMap<>();
        
        for (CustomerMembership membership : memberships) {
            if (!customerMap.containsKey(membership.getCustomerId())) {
                Customer c = customerController.getCustomerById(membership.getCustomerId());
                if (c != null) customerMap.put(membership.getCustomerId(), c);
            }
            if (!tierMap.containsKey(membership.getTierId())) {
                MembershipTier t = tierDAO.getTierById(membership.getTierId());
                if (t != null) tierMap.put(membership.getTierId(), t);
            }
        }
        
        for (CustomerMembership membership : memberships) {
            Customer customer = customerMap.get(membership.getCustomerId());
            String customerName = customer != null ? customer.getFullName() : "N/A";
            
            // Filter by search text
            if (!searchText.isEmpty() && !customerName.toLowerCase().contains(searchText)) {
                continue;
            }
            
            // Filter by tier
            if (filterTierId != null && membership.getTierId() != filterTierId) {
                continue;
            }
            
            MembershipTier tier = tierMap.get(membership.getTierId());
            String tierName = tier != null ? tier.getTierName() : "N/A";
            
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), membership.getEndDate());
            
            Object[] row = {
                membership.getId(),
                customerName,
                tierName,
                membership.getStartDate().format(DATE_FORMATTER),
                membership.getEndDate().format(DATE_FORMATTER),
                daysLeft > 0 ? daysLeft + " ngày" : "Đã hết hạn",
                getStatusText(membership.getStatus())
            };
            tableModel.addRow(row);
        }
    }
    
    private void showRegisterDialog() {
        RegisterMembershipDialog dialog = new RegisterMembershipDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadMemberships();
        }
    }
    
    private void renewMembership() {
        int selectedRow = membershipTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn gói cần gia hạn!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int membershipId = (int) tableModel.getValueAt(selectedRow, 0);
        
        String input = JOptionPane.showInputDialog(this, "Nhập số tháng gia hạn:", "1");
        if (input != null && !input.trim().isEmpty()) {
            try {
                int months = Integer.parseInt(input.trim());
                CustomerMembership membership = membershipDAO.getMembershipById(membershipId);
                
                LocalDate newEndDate = membership.getEndDate().plusMonths(months);
                
                if (membershipDAO.renewMembership(membershipId, newEndDate)) {
                    JOptionPane.showMessageDialog(this, "Gia hạn thành công!", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadMemberships();
                } else {
                    JOptionPane.showMessageDialog(this, "Gia hạn thất bại!", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Số tháng không hợp lệ!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void cancelMembership() {
        int selectedRow = membershipTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn gói cần hủy!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn hủy gói thành viên này?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int membershipId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (membershipDAO.cancelMembership(membershipId)) {
                JOptionPane.showMessageDialog(this, "Hủy gói thành công!", 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadMemberships();
            } else {
                JOptionPane.showMessageDialog(this, "Hủy gói thất bại!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateExpiredMemberships() {
        int updated = membershipDAO.updateExpiredMemberships();
        JOptionPane.showMessageDialog(this, 
            "Đã cập nhật " + updated + " gói hết hạn", 
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        loadMemberships();
    }
    
    private String getStatusText(String status) {
        return switch (status) {
            case AppConstants.MEMBERSHIP_STATUS_ACTIVE -> "Hoạt động";
            case AppConstants.MEMBERSHIP_STATUS_EXPIRED -> "Hết hạn";
            case AppConstants.MEMBERSHIP_STATUS_SUSPENDED -> "Tạm ngưng";
            default -> status;
        };
    }
    
    private void loadTierFilter() {
        tierFilterCombo.removeAllItems();
        tierFilterCombo.addItem(new TierFilterItem(null));
        
        List<MembershipTier> tiers = tierDAO.getAllTiers();
        for (MembershipTier tier : tiers) {
            tierFilterCombo.addItem(new TierFilterItem(tier));
        }
    }
    
    private static class TierFilterItem {
        final MembershipTier tier;
        
        TierFilterItem(MembershipTier tier) {
            this.tier = tier;
        }
        
        @Override
        public String toString() {
            return tier == null ? "-- Tất cả gói --" : tier.getTierName();
        }
    }
}
