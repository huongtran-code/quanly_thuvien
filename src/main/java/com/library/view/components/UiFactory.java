package com.library.view.components;

import com.library.util.AppConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Factory tập trung các component/hàm UI dùng chung,
 * tránh lặp code style ở từng panel.
 */
public final class UiFactory {

    private UiFactory() {}

    // ── Labels ──

    /** Label phụ (màu xám nhạt) dùng cạnh input */
    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppConstants.FONT_BODY);
        label.setForeground(AppConstants.TEXT_SECONDARY);
        return label;
    }

    /** Tiêu đề panel (to, đậm) */
    public static JLabel panelTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppConstants.FONT_SUBTITLE);
        label.setForeground(AppConstants.TEXT_PRIMARY);
        return label;
    }

    /** Tiêu đề panel kèm icon SVG */
    public static JLabel panelTitle(String text, Icon icon) {
        JLabel label = panelTitle(text);
        label.setIcon(icon);
        label.setIconTextGap(8);
        return label;
    }

    // ── Inputs ──

    /** Áp style chuẩn cho text field */
    public static void styleField(JTextField field) {
        field.setFont(AppConstants.FONT_BODY);
        field.setBackground(AppConstants.BG_INPUT);
        field.setForeground(AppConstants.TEXT_PRIMARY);
        field.setCaretColor(AppConstants.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConstants.BORDER),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
    }

    /** Text field đã style, có placeholder (FlatLaf) */
    public static JTextField textField(int width, String placeholder) {
        JTextField field = new JTextField();
        styleField(field);
        field.setPreferredSize(new Dimension(width, 32));
        if (placeholder != null) {
            field.putClientProperty("JTextField.placeholderText", placeholder);
        }
        return field;
    }

    /** Combo box đã style */
    public static <T> JComboBox<T> comboBox(T[] items, int width) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(AppConstants.FONT_BODY);
        combo.setPreferredSize(new Dimension(width, 32));
        return combo;
    }

    // ── Toolbar ──

    /** Toolbar chuẩn: tiêu đề bên trái, các nút bên phải */
    public static JPanel toolbar(String title, JComponent... rightItems) {
        return toolbar(title, null, rightItems);
    }

    /** Toolbar chuẩn với icon tiêu đề */
    public static JPanel toolbar(String title, Icon titleIcon, JComponent... rightItems) {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(AppConstants.BG_DARK);
        toolbar.add(titleIcon != null ? panelTitle(title, titleIcon) : panelTitle(title), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        for (JComponent item : rightItems) {
            actions.add(item);
        }
        toolbar.add(actions, BorderLayout.EAST);
        return toolbar;
    }

    /** Nút phụ (xám) cho các hành động thứ cấp: Hủy, Làm mới... */
    public static StyledButton secondaryButton(String text) {
        StyledButton btn = new StyledButton(text);
        btn.setBackground(AppConstants.BG_INPUT);
        btn.setForeground(AppConstants.TEXT_PRIMARY);
        return btn;
    }

    // ── Dialogs ──

    /**
     * Tạo dialog modal chuẩn: đóng bằng phím ESC, canh giữa parent.
     */
    public static JDialog modalDialog(Component parent, String title, int width, int height) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(window instanceof Frame f ? f : null, title, true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(parent);
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        return dialog;
    }

    /** Panel chứa nút của dialog (canh phải) */
    public static JPanel dialogButtons(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        panel.setBackground(AppConstants.BG_CARD);
        for (JButton btn : buttons) {
            panel.add(btn);
        }
        return panel;
    }

    // ── Messages ──

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    /** Hộp thoại xác nhận Yes/No, trả về true nếu chọn Yes */
    public static boolean confirm(Component parent, String message, String title) {
        return JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Hộp thoại xác nhận với icon cảnh báo */
    public static boolean confirmWarning(Component parent, String message, String title) {
        return JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
