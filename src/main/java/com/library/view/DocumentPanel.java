package com.library.view;

import com.library.controller.AuthController;
import com.library.controller.DocumentController;
import com.library.model.Category;
import com.library.model.Document;
import com.library.util.AppConstants;
import com.library.util.CsvExporter;
import com.library.util.CurrencyUtil;
import com.library.util.Icons;
import com.library.view.components.FormPanel;
import com.library.view.components.StyledButton;
import com.library.view.components.StyledTable;
import com.library.view.components.UiFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel quản lý tài liệu & danh mục với giao diện Master-Detail
 */
public class DocumentPanel extends JPanel {

    private final DocumentController controller = new DocumentController();
    private StyledTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private List<Category> categories;
    private Timer searchDebounce;
    private JLabel countLabel;

    // Master-Detail components
    private JSplitPane splitPane;
    private JPanel detailContainer;
    private CardLayout detailCardLayout;
    private Document currentEditingDoc;

    public DocumentPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(AppConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createToolbar(), BorderLayout.NORTH);

        // Setup detail container
        detailCardLayout = new CardLayout();
        detailContainer = new JPanel(detailCardLayout);
        detailContainer.setBackground(AppConstants.BG_DARK);
        detailContainer.setMinimumSize(new Dimension(380, 0));

        // State 1: Empty state (No document selected)
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(AppConstants.BG_CARD);
        emptyPanel.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        JLabel emptyLabel = new JLabel("Chọn một tài liệu để xem hoặc Thêm mới");
        emptyLabel.setForeground(AppConstants.TEXT_MUTED);
        emptyLabel.setFont(AppConstants.FONT_SUBTITLE);
        emptyLabel.setIcon(Icons.get("file-text", 48, AppConstants.BORDER));
        emptyLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        emptyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        emptyLabel.setIconTextGap(16);
        emptyPanel.add(emptyLabel);
        detailContainer.add(emptyPanel, "EMPTY");

        // State 2: Form container (For add/edit)
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(AppConstants.BG_DARK);
        detailContainer.add(formContainer, "FORM");

        // Split pane
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTablePanel(), detailContainer);
        splitPane.setDividerLocation(AppConstants.WINDOW_WIDTH - 420);
        splitPane.setResizeWeight(1.0); // Table gets extra space
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setDividerSize(10);

        add(splitPane, BorderLayout.CENTER);
        detailCardLayout.show(detailContainer, "EMPTY");

        // Setup selection listener for Master-Detail sync
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int id = (int) tableModel.getValueAt(row, 0);
                    Document doc = controller.getDocumentById(id);
                    if (doc != null) {
                        showDetailForm(doc);
                    }
                }
            }
        });

        loadData();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(AppConstants.BG_DARK);

        // Left: Search + Filter
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        searchField = UiFactory.textField(250, "Tìm kiếm tài liệu...");
        searchField.putClientProperty("JTextField.leadingIcon", Icons.get("search", 14));
        searchField.addActionListener(e -> doSearch());

        // Tìm kiếm trực tiếp khi gõ (debounce 300ms)
        searchDebounce = new Timer(300, e -> doSearch());
        searchDebounce.setRepeats(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchDebounce.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { searchDebounce.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { searchDebounce.restart(); }
        });

        // Category filter
        categories = controller.getAllCategories();
        String[] catNames = new String[categories.size() + 1];
        catNames[0] = "-- Tất cả danh mục --";
        for (int i = 0; i < categories.size(); i++) {
            catNames[i + 1] = categories.get(i).getName();
        }
        categoryFilter = UiFactory.comboBox(catNames, 180);
        categoryFilter.addActionListener(e -> doFilter());

        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(UiFactory.fieldLabel("Danh mục:"));
        searchPanel.add(categoryFilter);

        // Right: Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionPanel.setOpaque(false);

        StyledButton toolsBtn = UiFactory.secondaryButton("⚙ Công cụ ▾");
        toolsBtn.setPreferredSize(new Dimension(120, 32));
        JPopupMenu toolsMenu = new JPopupMenu();
        toolsMenu.setBackground(AppConstants.BG_CARD);
        toolsMenu.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));

        JMenuItem advSearchItem = createMenuItem("🔎 Tìm kiếm nâng cao", e -> showAdvancedSearch());
        JMenuItem exportItem = createMenuItem("📁 Xuất CSV", e -> {
            if (CsvExporter.exportToCSV(this, tableModel, "tai_lieu")) {
                UiFactory.showInfo(this, "Đã xuất file CSV thành công!");
            }
        });
        JMenuItem lowStockItem = createMenuItem("🔴 Lọc tồn kho thấp", e -> showLowStockFilter());
        JMenuItem refreshItem = createMenuItem("🔄 Làm mới dữ liệu", e -> {
            loadData();
            categoryFilter.setSelectedIndex(0);
            searchField.setText("");
        });

        toolsMenu.add(advSearchItem);
        toolsMenu.add(lowStockItem);
        toolsMenu.addSeparator();
        toolsMenu.add(exportItem);
        toolsMenu.addSeparator();
        toolsMenu.add(refreshItem);

        toolsBtn.addActionListener(e -> toolsMenu.show(toolsBtn, 0, toolsBtn.getHeight()));
        actionPanel.add(toolsBtn);

        if (AuthController.getInstance().isAdmin()) {
            actionPanel.add(Box.createHorizontalStrut(8));

            StyledButton addBtn = new StyledButton("Thêm mới");
            addBtn.setIcon(Icons.button("plus"));
            addBtn.setPreferredSize(new Dimension(110, 32));
            addBtn.addActionListener(e -> {
                table.clearSelection();
                showDetailForm(null);
            });

            StyledButton deleteBtn = UiFactory.secondaryButton("Xóa");
            deleteBtn.setForeground(AppConstants.DANGER);
            deleteBtn.setIcon(Icons.button("trash"));
            deleteBtn.setPreferredSize(new Dimension(80, 32));
            deleteBtn.addActionListener(e -> deleteSelected());

            StyledButton catBtn = UiFactory.secondaryButton("Danh mục");
            catBtn.setIcon(Icons.button("folder"));
            catBtn.setPreferredSize(new Dimension(110, 32));
            catBtn.addActionListener(e -> showCategoryDialog());

            actionPanel.add(catBtn);
            actionPanel.add(addBtn);
            // Sửa button removed: selection triggers edit form automatically in Master-Detail
            actionPanel.add(deleteBtn);
        }

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        return toolbar;
    }

    private JMenuItem createMenuItem(String text, java.awt.event.ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(AppConstants.FONT_BODY);
        item.setForeground(AppConstants.TEXT_PRIMARY);
        item.setBackground(AppConstants.BG_CARD);
        item.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        item.addActionListener(action);
        return item;
    }

    private JPanel createTablePanel() {
        String[] columns = {"ID", "Tên tài liệu", "Tác giả", "Danh mục", "Đơn giá", "Tồn kho"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        // Highlight tồn kho thấp ở cột "Tồn kho" (cột 5 trong bảng mới rút gọn)
        table.enableStockHighlight(5);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppConstants.BG_DARK);
        
        // Add a subtle border to the table wrapper for Master-Detail separation
        JScrollPane scroll = table.wrapInScrollPane();
        scroll.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        panel.add(scroll, BorderLayout.CENTER);

        // Info bar
        JPanel infoBar = new JPanel(new BorderLayout());
        infoBar.setBackground(AppConstants.BG_CARD);
        infoBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        countLabel = new JLabel();
        countLabel.setFont(AppConstants.FONT_SMALL);
        countLabel.setForeground(AppConstants.TEXT_MUTED);
        countLabel.setIcon(Icons.get("list", 12));
        countLabel.setIconTextGap(6);
        infoBar.add(countLabel, BorderLayout.WEST);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        legend.setOpaque(false);
        legend.add(createLegendLabel("⚠ ≤" + AppConstants.LOW_STOCK_THRESHOLD, AppConstants.DANGER));
        legend.add(createLegendLabel("● ≤" + AppConstants.WARN_STOCK_THRESHOLD, AppConstants.WARNING));
        legend.add(createLegendLabel("● Đủ", AppConstants.ACCENT));
        infoBar.add(legend, BorderLayout.EAST);

        panel.add(infoBar, BorderLayout.SOUTH);
        tableModel.addTableModelListener(e -> updateInfoBar());

        return panel;
    }

    private JLabel createLegendLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_SMALL.deriveFont(Font.BOLD));
        lbl.setForeground(color);
        return lbl;
    }

    private void updateInfoBar() {
        int total = tableModel.getRowCount();
        int lowCount = 0;
        int warnCount = 0;
        for (int i = 0; i < total; i++) {
            try {
                int stock = Integer.parseInt(tableModel.getValueAt(i, 5).toString());
                if (stock <= AppConstants.LOW_STOCK_THRESHOLD) lowCount++;
                else if (stock <= AppConstants.WARN_STOCK_THRESHOLD) warnCount++;
            } catch (Exception ignored) {}
        }
        StringBuilder sb = new StringBuilder("Tổng: " + total + " tài liệu");
        if (lowCount > 0) sb.append("  •  🔴 ").append(lowCount).append(" cần nhập gấp");
        if (warnCount > 0) sb.append("  •  🟡 ").append(warnCount).append(" sắp hết");
        countLabel.setText(sb.toString());
    }

    private void loadData() {
        refreshTable(controller.getAllDocuments());
    }

    public void refreshData() {
        loadData();
    }

    private void refreshTable(List<Document> docs) {
        tableModel.setRowCount(0);
        for (Document doc : docs) {
            tableModel.addRow(new Object[]{
                    doc.getId(),
                    doc.getTitle(),
                    doc.getAuthor(),
                    doc.getCategoryName(),
                    CurrencyUtil.format(doc.getUnitPrice()),
                    doc.getStockQuantity()
            });
        }
    }

    private void doSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty() && categoryFilter.getSelectedIndex() > 0) {
            return;
        }
        if (categoryFilter.getSelectedIndex() != 0) {
            categoryFilter.setSelectedIndex(0);
        }
        refreshTable(controller.searchDocuments(keyword));
    }

    private void doFilter() {
        int idx = categoryFilter.getSelectedIndex();
        if (idx <= 0) {
            loadData();
        } else {
            Category cat = categories.get(idx - 1);
            refreshTable(controller.filterByCategory(cat.getId()));
            searchField.setText("");
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiFactory.showWarning(this, "Vui lòng chọn tài liệu cần xóa!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);
        if (UiFactory.confirmWarning(this,
                "Bạn có chắc muốn xóa tài liệu:\n\"" + title + "\"?", "Xác nhận xóa")) {
            String error = controller.deleteDocument(id);
            if (error != null) {
                UiFactory.showError(this, error);
            } else {
                loadData();
                detailCardLayout.show(detailContainer, "EMPTY");
            }
        }
    }

    private void showDetailForm(Document doc) {
        boolean isNew = (doc == null);
        currentEditingDoc = doc;

        JPanel formContainer = (JPanel) detailContainer.getComponent(1);
        formContainer.removeAll();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppConstants.BG_CARD);
        wrapper.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppConstants.BG_CARD);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstants.BORDER),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        JLabel titleLbl = UiFactory.panelTitle(isNew ? "Thêm tài liệu mới" : "Chi tiết tài liệu");
        headerPanel.add(titleLbl, BorderLayout.WEST);

        if (!isNew) {
            JLabel idBadge = new JLabel("#" + doc.getId());
            idBadge.setFont(AppConstants.FONT_BODY.deriveFont(Font.BOLD));
            idBadge.setForeground(AppConstants.TEXT_MUTED);
            headerPanel.add(idBadge, BorderLayout.EAST);
        }

        wrapper.add(headerPanel, BorderLayout.NORTH);

        // Form
        FormPanel form = new FormPanel(110);
        
        final JButton[] aiBtnHolder = new JButton[1];
        JTextField titleField = form.addTextFieldWithButton(
                "Tên tài liệu",
                isNew ? "" : doc.getTitle(),
                "🪄 AI",
                null,
                btn -> {
                    aiBtnHolder[0] = btn;
                    btn.setToolTipText("AI gợi ý thông tin sách");
                }
        );

        JTextField authorField = form.addTextField("Tác giả", isNew ? "" : doc.getAuthor());
        JTextField isbnField = form.addTextField("ISBN", isNew ? "" : doc.getIsbn());

        categories = controller.getAllCategories();
        String[] catNames = new String[categories.size()];
        int selectedCat = 0;
        for (int i = 0; i < categories.size(); i++) {
            catNames[i] = categories.get(i).getName();
            if (!isNew && categories.get(i).getId() == doc.getCategoryId()) selectedCat = i;
        }
        JComboBox<String> catCombo = form.addComboBox("Danh mục", catNames);
        if(catNames.length > 0) catCombo.setSelectedIndex(selectedCat);

        JTextField publisherField = form.addTextField("Nhà xuất bản", isNew ? "" : doc.getPublisher());
        JSpinner yearSpinner = form.addSpinner("Năm XB", 1900, 2100, isNew ? 2024 : doc.getPublishYear());
        JTextField priceField = form.addTextField("Đơn giá (₫)", isNew ? "0" : String.valueOf((long) doc.getUnitPrice()));
        JSpinner stockSpinner = form.addSpinner("Tồn kho", 0, 100000, isNew ? 0 : doc.getStockQuantity());

        if (aiBtnHolder[0] != null) {
            aiBtnHolder[0].addActionListener(e -> {
                String query = titleField.getText().trim();
                if (query.isEmpty()) {
                    UiFactory.showWarning(this, "Nhập tên sách để gọi AI!");
                    titleField.requestFocus();
                    return;
                }
                aiBtnHolder[0].setEnabled(false);
                aiBtnHolder[0].setText("...");

                SwingWorker<com.library.model.BookMetadataSuggestion, Void> worker = new SwingWorker<>() {
                    @Override
                    protected com.library.model.BookMetadataSuggestion doInBackground() throws Exception {
                        return com.library.service.AIService.suggestBookInfo(query);
                    }
                    @Override
                    protected void done() {
                        try {
                            com.library.model.BookMetadataSuggestion sugg = get();
                            if (sugg != null) {
                                if (sugg.getTitle() != null && !sugg.getTitle().isEmpty()) titleField.setText(sugg.getTitle());
                                if (sugg.getAuthor() != null && !sugg.getAuthor().isEmpty()) authorField.setText(sugg.getAuthor());
                                if (sugg.getPublisher() != null && !sugg.getPublisher().isEmpty()) publisherField.setText(sugg.getPublisher());
                                if (sugg.getIsbn() != null && !sugg.getIsbn().isEmpty() && (isbnField.getText().trim().isEmpty() || isNew)) isbnField.setText(sugg.getIsbn());
                                if (sugg.getPublicationYear() != null && sugg.getPublicationYear() > 1900) yearSpinner.setValue(sugg.getPublicationYear());
                                if (sugg.getEstimatedPrice() != null && sugg.getEstimatedPrice() > 0) priceField.setText(String.valueOf(sugg.getEstimatedPrice().longValue()));
                                if (sugg.getCategoryName() != null) {
                                    String suggCatLower = sugg.getCategoryName().toLowerCase();
                                    for (int i = 0; i < categories.size(); i++) {
                                        if (suggCatLower.contains(categories.get(i).getName().toLowerCase())) {
                                            catCombo.setSelectedIndex(i);
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            UiFactory.showError(DocumentPanel.this, "Lỗi gọi AI: " + ex.getMessage());
                        } finally {
                            aiBtnHolder[0].setEnabled(true);
                            aiBtnHolder[0].setText("🪄 AI");
                        }
                    }
                };
                worker.execute();
            });
        }

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(AppConstants.BG_CARD);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppConstants.BORDER));

        StyledButton saveBtn = StyledButton.success("Lưu");
        saveBtn.setIcon(Icons.button("save"));
        StyledButton cancelBtn = UiFactory.secondaryButton(isNew ? "Hủy" : "Đóng");

        cancelBtn.addActionListener(e -> {
            table.clearSelection();
            detailCardLayout.show(detailContainer, "EMPTY");
        });

        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                UiFactory.showError(this, "Tên tài liệu không được để trống!");
                titleField.requestFocus();
                return;
            }
            Document d = isNew ? new Document() : doc;
            d.setTitle(title);
            d.setAuthor(authorField.getText().trim());
            d.setIsbn(isbnField.getText().trim());
            if (catCombo.getSelectedIndex() >= 0) {
                d.setCategoryId(categories.get(catCombo.getSelectedIndex()).getId());
            }
            d.setPublisher(publisherField.getText().trim());
            d.setPublishYear((int) yearSpinner.getValue());
            try {
                d.setUnitPrice(Double.parseDouble(priceField.getText().trim().replaceAll("[^0-9]", "")));
            } catch (NumberFormatException ex) {
                UiFactory.showError(this, "Đơn giá không hợp lệ!");
                priceField.requestFocus();
                return;
            }
            d.setStockQuantity((int) stockSpinner.getValue());

            String error = controller.saveDocument(d);
            if (error != null) {
                UiFactory.showError(this, error);
            } else {
                loadData();
                reloadCategoryFilter();
                UiFactory.showInfo(this, "Lưu thành công!");
                if (isNew) {
                    detailCardLayout.show(detailContainer, "EMPTY");
                }
            }
        });

        if (AuthController.getInstance().isAdmin()) {
            actionPanel.add(cancelBtn);
            actionPanel.add(saveBtn);
        } else {
            actionPanel.add(cancelBtn); // viewers can only close
            form.setEnabled(false); // Make form read-only if not admin
        }

        wrapper.add(actionPanel, BorderLayout.SOUTH);

        formContainer.add(wrapper, BorderLayout.CENTER);
        formContainer.revalidate();
        formContainer.repaint();

        detailCardLayout.show(detailContainer, "FORM");
    }

    private void showCategoryDialog() {
        JDialog dialog = UiFactory.modalDialog(SwingUtilities.getWindowAncestor(this), "Quản lý danh mục", 500, 400);
        // ... (existing category dialog logic)
        String[] cols = {"ID", "Tên danh mục", "Mô tả"};
        DefaultTableModel catModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        StyledTable catTable = new StyledTable(catModel);

        Runnable refreshCats = () -> {
            catModel.setRowCount(0);
            for (Category c : controller.getAllCategories()) {
                catModel.addRow(new Object[]{c.getId(), c.getName(), c.getDescription()});
            }
        };
        refreshCats.run();

        StyledButton addCatBtn = StyledButton.success("Thêm");
        addCatBtn.setIcon(Icons.button("plus"));
        addCatBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(dialog, "Tên danh mục:", "Thêm danh mục", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                String desc = JOptionPane.showInputDialog(dialog, "Mô tả:", "Thêm danh mục", JOptionPane.PLAIN_MESSAGE);
                Category cat = new Category(name.trim(), desc != null ? desc.trim() : "");
                String error = controller.saveCategory(cat);
                if (error != null) UiFactory.showError(dialog, error);
                else refreshCats.run();
            }
        });

        StyledButton delCatBtn = StyledButton.danger("Xóa");
        delCatBtn.setIcon(Icons.button("trash"));
        delCatBtn.addActionListener(e -> {
            int row = catTable.getSelectedRow();
            if (row < 0) {
                UiFactory.showWarning(dialog, "Chọn danh mục cần xóa!");
                return;
            }
            int id = (int) catModel.getValueAt(row, 0);
            String name = (String) catModel.getValueAt(row, 1);
            if (UiFactory.confirmWarning(dialog, "Xóa danh mục \"" + name + "\"?", "Xác nhận")) {
                String error = controller.deleteCategory(id);
                if (error != null) UiFactory.showError(dialog, error);
                else refreshCats.run();
            }
        });

        dialog.setLayout(new BorderLayout());
        dialog.add(catTable.wrapInScrollPane(), BorderLayout.CENTER);
        dialog.add(UiFactory.dialogButtons(addCatBtn, delCatBtn), BorderLayout.SOUTH);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { reloadCategoryFilter(); }
        });
        dialog.setVisible(true);
    }

    private void reloadCategoryFilter() {
        categories = controller.getAllCategories();
        categoryFilter.removeAllItems();
        categoryFilter.addItem("-- Tất cả danh mục --");
        for (Category c : categories) categoryFilter.addItem(c.getName());
    }

    private void showLowStockFilter() {
        List<Document> lowDocs = controller.getLowStockDocuments();
        if (lowDocs.isEmpty()) {
            UiFactory.showInfo(this, "Không có tài liệu tồn kho thấp!");
            return;
        }
        refreshTable(lowDocs);
        categoryFilter.setSelectedIndex(0);
        searchField.setText("");
    }

    private void showAdvancedSearch() {
        JDialog dialog = UiFactory.modalDialog(SwingUtilities.getWindowAncestor(this), "Tìm kiếm nâng cao", 520, 500);
        FormPanel form = new FormPanel();
        JTextField fTitle = form.addTextField("Tên sách");
        JTextField fAuthor = form.addTextField("Tác giả");
        JTextField fIsbn = form.addTextField("ISBN");
        JTextField fPublisher = form.addTextField("NXB");

        String[] catOptions = new String[categories.size() + 1];
        catOptions[0] = "-- Tất cả --";
        for (int i = 0; i < categories.size(); i++) catOptions[i + 1] = categories.get(i).getName();
        JComboBox<String> fCategory = form.addComboBox("Danh mục", catOptions);

        JPanel yearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); yearPanel.setOpaque(false);
        JTextField fYearFrom = UiFactory.textField(80, "Từ"); JTextField fYearTo = UiFactory.textField(80, "Đến");
        yearPanel.add(fYearFrom); yearPanel.add(new JLabel("→")); yearPanel.add(fYearTo); form.addField("Năm XB:", yearPanel);

        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); pricePanel.setOpaque(false);
        JTextField fPriceFrom = UiFactory.textField(100, "Từ"); JTextField fPriceTo = UiFactory.textField(100, "Đến");
        pricePanel.add(fPriceFrom); pricePanel.add(new JLabel("→")); pricePanel.add(fPriceTo); form.addField("Đơn giá:", pricePanel);

        JPanel stockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); stockPanel.setOpaque(false);
        JTextField fStockFrom = UiFactory.textField(80, "Từ"); JTextField fStockTo = UiFactory.textField(80, "Đến");
        stockPanel.add(fStockFrom); stockPanel.add(new JLabel("→")); stockPanel.add(fStockTo); form.addField("Tồn kho:", stockPanel);

        StyledButton searchBtn = StyledButton.success("Tìm kiếm");
        searchBtn.addActionListener(e -> {
            Integer catId = fCategory.getSelectedIndex() > 0 ? categories.get(fCategory.getSelectedIndex() - 1).getId() : null;
            List<Document> results = controller.advancedSearch(fTitle.getText(), fAuthor.getText(), fIsbn.getText(), fPublisher.getText(),
                    parseIntOrNull(fYearFrom.getText()), parseIntOrNull(fYearTo.getText()), parseDoubleOrNull(fPriceFrom.getText()), parseDoubleOrNull(fPriceTo.getText()), parseIntOrNull(fStockFrom.getText()), parseIntOrNull(fStockTo.getText()), catId);
            refreshTable(results);
            dialog.dispose();
            UiFactory.showInfo(this, "Tìm thấy " + results.size() + " kết quả.");
        });
        StyledButton cancelBtn = UiFactory.secondaryButton("Hủy");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(AppConstants.BG_DARK);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        content.add(form, BorderLayout.CENTER);
        content.add(UiFactory.dialogButtons(cancelBtn, searchBtn), BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return null; }
    }
}
