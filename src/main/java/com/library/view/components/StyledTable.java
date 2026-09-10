package com.library.view.components;

import com.library.util.AppConstants;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * JTable tùy chỉnh với alternate row colors, custom header, rounded selection
 */
public class StyledTable extends JTable {

    public StyledTable(DefaultTableModel model) {
        super(model);
        setupStyle();
    }

    private void setupStyle() {
        // General
        setFont(AppConstants.FONT_BODY);
        setRowHeight(AppConstants.TABLE_ROW_HEIGHT);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 1));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFillsViewportHeight(true);

        // Colors
        setBackground(AppConstants.BG_CARD);
        setForeground(AppConstants.TEXT_PRIMARY);
        setSelectionBackground(AppConstants.PRIMARY.darker());
        setSelectionForeground(Color.WHITE);
        setGridColor(AppConstants.BORDER);

        // Header
        JTableHeader header = getTableHeader();
        header.setFont(AppConstants.FONT_HEADING);
        header.setBackground(AppConstants.BG_CARD);
        header.setForeground(AppConstants.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setReorderingAllowed(false);

        // Custom renderer cho alternate rows
        setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    /**
     * Renderer với alternate row colors
     */
    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                c.setBackground(AppConstants.PRIMARY.darker());
                c.setForeground(Color.WHITE);
            } else {
                c.setBackground(row % 2 == 0 ? AppConstants.BG_CARD : AppConstants.BG_ROW_ALT);
                c.setForeground(AppConstants.TEXT_PRIMARY);
            }

            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            // Căn phải cho số
            if (value instanceof Number) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            return c;
        }
    }

    /**
     * Renderer cho cột trạng thái (badge màu)
     */
    public static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = value != null ? value.toString() : "";
            Color statusColor = AppConstants.getStatusColor(status);

            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(AppConstants.FONT_SMALL.deriveFont(Font.BOLD));

            if (isSelected) {
                c.setBackground(AppConstants.PRIMARY.darker());
                c.setForeground(Color.WHITE);
            } else {
                c.setForeground(statusColor);
                c.setBackground(row % 2 == 0 ? AppConstants.BG_CARD : AppConstants.BG_ROW_ALT);
            }

            // Translate status
            String translated = switch (status) {
                case "DRAFT" -> "● Nháp";
                case "APPROVED" -> "● Đã duyệt";
                case "RECEIVED" -> "● Đã nhận";
                case "CANCELLED" -> "● Đã hủy";
                case "ACTIVE" -> "● Hoạt động";
                case "CLOSED" -> "● Đã đóng";
                default -> status;
            };
            setText(translated);

            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            return c;
        }
    }

    /**
     * Tiện ích: wrap table trong JScrollPane đã styled
     */
    public JScrollPane wrapInScrollPane() {
        JScrollPane scroll = new JScrollPane(this);
        scroll.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER, 1));
        scroll.getViewport().setBackground(AppConstants.BG_CARD);
        return scroll;
    }

    /**
     * Renderer highlight row theo cột tồn kho:
     * - Đỏ nhạt: stock ≤ LOW_STOCK_THRESHOLD (5)
     * - Cam nhạt: stock ≤ WARN_STOCK_THRESHOLD (15)
     */
    public static class StockAwareRenderer extends DefaultTableCellRenderer {
        private final int stockColumnIndex;

        public StockAwareRenderer(int stockColumnIndex) {
            this.stockColumnIndex = stockColumnIndex;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            // Căn phải cho số
            if (value instanceof Number) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (isSelected) {
                c.setBackground(AppConstants.PRIMARY.darker());
                c.setForeground(Color.WHITE);
                return c;
            }

            // Kiểm tra tồn kho
            int stock = getStockValue(table, row);
            Color baseBg = row % 2 == 0 ? AppConstants.BG_CARD : AppConstants.BG_ROW_ALT;

            if (stock >= 0 && stock <= AppConstants.LOW_STOCK_THRESHOLD) {
                // Đỏ - tồn kho rất thấp
                c.setBackground(blendColor(baseBg, AppConstants.LOW_STOCK_BG));
                c.setForeground(AppConstants.LOW_STOCK_TEXT);
            } else if (stock >= 0 && stock <= AppConstants.WARN_STOCK_THRESHOLD) {
                // Cam - cảnh báo
                c.setBackground(blendColor(baseBg, AppConstants.WARN_STOCK_BG));
                c.setForeground(AppConstants.WARN_STOCK_TEXT);
            } else {
                c.setBackground(baseBg);
                c.setForeground(AppConstants.TEXT_PRIMARY);
            }

            return c;
        }

        private int getStockValue(JTable table, int row) {
            try {
                Object val = table.getModel().getValueAt(row, stockColumnIndex);
                if (val instanceof Number) return ((Number) val).intValue();
                return Integer.parseInt(val.toString());
            } catch (Exception e) {
                return -1;
            }
        }

        private Color blendColor(Color base, Color overlay) {
            float alpha = overlay.getAlpha() / 255f;
            int r = (int)(base.getRed() * (1 - alpha) + overlay.getRed() * alpha);
            int g = (int)(base.getGreen() * (1 - alpha) + overlay.getGreen() * alpha);
            int b = (int)(base.getBlue() * (1 - alpha) + overlay.getBlue() * alpha);
            return new Color(
                Math.min(255, Math.max(0, r)),
                Math.min(255, Math.max(0, g)),
                Math.min(255, Math.max(0, b))
            );
        }
    }

    /**
     * Renderer badge cho cột tồn kho: hiển thị icon cảnh báo + số lượng đỏ/cam
     */
    public static class StockBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            setFont(AppConstants.FONT_BODY.deriveFont(Font.BOLD));

            int stock = -1;
            try {
                if (value instanceof Number) stock = ((Number) value).intValue();
                else stock = Integer.parseInt(value.toString());
            } catch (Exception ignored) {}

            if (isSelected) {
                c.setBackground(AppConstants.PRIMARY.darker());
                c.setForeground(Color.WHITE);
            } else {
                Color baseBg = row % 2 == 0 ? AppConstants.BG_CARD : AppConstants.BG_ROW_ALT;
                if (stock >= 0 && stock <= AppConstants.LOW_STOCK_THRESHOLD) {
                    setText("⚠ " + stock);
                    c.setForeground(AppConstants.DANGER);
                    c.setBackground(new Color(127, 29, 29, 40));
                } else if (stock >= 0 && stock <= AppConstants.WARN_STOCK_THRESHOLD) {
                    setText("● " + stock);
                    c.setForeground(AppConstants.WARNING);
                    c.setBackground(baseBg);
                } else {
                    setText(String.valueOf(stock >= 0 ? stock : value));
                    c.setForeground(AppConstants.ACCENT);
                    c.setBackground(baseBg);
                }
            }

            return c;
        }
    }

    /**
     * Áp dụng renderer stock-aware cho tất cả cột + badge cho cột tồn kho
     */
    public void enableStockHighlight(int stockColumnIndex) {
        // Chỉ đổi màu riêng cột tồn kho (StockBadgeRenderer), không bôi đậm cả dòng gây rối
        getColumnModel().getColumn(stockColumnIndex).setCellRenderer(new StockBadgeRenderer());
    }
}
