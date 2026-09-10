package com.library.view.components;

import com.library.util.AppConstants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder pattern cho form nhập liệu với layout đẹp
 */
public class FormPanel extends JPanel {

    private final List<JComponent> fields = new ArrayList<>();
    private final int labelWidth;

    public FormPanel() {
        this(120);
    }

    public FormPanel(int labelWidth) {
        this.labelWidth = labelWidth;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(AppConstants.BG_CARD);
        setBorder(BorderFactory.createEmptyBorder(
                AppConstants.PADDING, AppConstants.PADDING_LG,
                AppConstants.PADDING, AppConstants.PADDING_LG));
    }

    /**
     * Thêm trường text
     */
    public JTextField addTextField(String label) {
        JTextField field = new JTextField();
        styleField(field);
        addRow(label, field);
        fields.add(field);
        return field;
    }

    /**
     * Thêm trường text với giá trị mặc định
     */
    public JTextField addTextField(String label, String defaultValue) {
        JTextField field = addTextField(label);
        field.setText(defaultValue);
        return field;
    }

    /**
     * Thêm trường text kèm nút bấm (ví dụ AI gợi ý)
     */
    public JTextField addTextFieldWithButton(String label, String defaultValue, String btnText, Icon btnIcon, java.util.function.Consumer<JButton> btnConfig) {
        JPanel wrapper = new JPanel(new BorderLayout(6, 0));
        wrapper.setOpaque(false);
        JTextField field = new JTextField(defaultValue != null ? defaultValue : "");
        styleField(field);

        JButton btn = new JButton(btnText);
        btn.setFont(AppConstants.FONT_SMALL);
        btn.setForeground(Color.WHITE);
        btn.setBackground(AppConstants.PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConstants.PRIMARY_DARK, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        if (btnIcon != null) {
            btn.setIcon(btnIcon);
            btn.setIconTextGap(4);
        }
        if (btnConfig != null) {
            btnConfig.accept(btn);
        }

        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(btn, BorderLayout.EAST);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        addRow(label, wrapper);
        fields.add(field);
        return field;
    }

    /**
     * Thêm trường password
     */
    public JPasswordField addPasswordField(String label) {
        JPasswordField field = new JPasswordField();
        styleField(field);
        addRow(label, field);
        fields.add(field);
        return field;
    }

    /**
     * Thêm combobox
     */
    public <T> JComboBox<T> addComboBox(String label, T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(AppConstants.FONT_BODY);
        combo.setBackground(AppConstants.BG_INPUT);
        combo.setForeground(AppConstants.TEXT_PRIMARY);
        combo.setPreferredSize(new Dimension(300, 32));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        addRow(label, combo);
        fields.add(combo);
        return combo;
    }

    /**
     * Thêm spinner số
     */
    public JSpinner addSpinner(String label, int min, int max, int value) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(AppConstants.FONT_BODY);
        spinner.setPreferredSize(new Dimension(300, 32));
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        addRow(label, spinner);
        fields.add(spinner);
        return spinner;
    }

    /**
     * Thêm text area
     */
    public JTextArea addTextArea(String label, int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setFont(AppConstants.FONT_BODY);
        area.setBackground(AppConstants.BG_INPUT);
        area.setForeground(AppConstants.TEXT_PRIMARY);
        area.setCaretColor(AppConstants.TEXT_PRIMARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConstants.BORDER),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(300, rows * 24));
        scroll.setBorder(null);
        addRow(label, scroll);
        fields.add(area);
        return area;
    }

    /**
     * Thêm separator
     */
    public void addSeparator() {
        add(Box.createVerticalStrut(8));
        JSeparator sep = new JSeparator();
        sep.setForeground(AppConstants.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        add(sep);
        add(Box.createVerticalStrut(8));
    }

    /**
     * Thêm section title
     */
    public void addSectionTitle(String title) {
        add(Box.createVerticalStrut(12));
        JLabel label = new JLabel(title);
        label.setFont(AppConstants.FONT_SUBTITLE);
        label.setForeground(AppConstants.PRIMARY_LIGHT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(label);
        add(Box.createVerticalStrut(8));
    }

    /**
     * Thêm component bất kỳ (JPanel, JComboBox, JSpinner, ...)
     */
    public void addField(String label, JComponent field) {
        addRow(label, field);
    }

    private void addRow(String labelText, JComponent field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(AppConstants.BG_CARD);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel label = new JLabel(labelText + ":");
        label.setFont(AppConstants.FONT_BODY);
        label.setForeground(AppConstants.TEXT_SECONDARY);
        label.setPreferredSize(new Dimension(labelWidth, 32));
        label.setMinimumSize(new Dimension(labelWidth, 32));
        label.setMaximumSize(new Dimension(labelWidth, 32));

        row.add(label);
        row.add(Box.createHorizontalStrut(8));
        row.add(field);

        add(row);
        add(Box.createVerticalStrut(8));
    }

    private void styleField(JTextField field) {
        field.setFont(AppConstants.FONT_BODY);
        field.setBackground(AppConstants.BG_INPUT);
        field.setForeground(AppConstants.TEXT_PRIMARY);
        field.setCaretColor(AppConstants.TEXT_PRIMARY);
        field.setPreferredSize(new Dimension(300, 32));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConstants.BORDER),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
    }
}
