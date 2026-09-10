package com.library.util;

import java.awt.*;

/**
 * Hằng số giao diện và ứng dụng
 */
public final class AppConstants {

    private AppConstants() {}

    // ── Application ──
    public static final String APP_NAME = "Quản Lý Mua Sắm Tài Liệu Thư Viện";
    public static final String APP_VERSION = "1.0";
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 800;

    // ── Color Palette (Dark Theme) ──
    public static final Color PRIMARY        = new Color(99, 102, 241);   // Indigo 500
    public static final Color PRIMARY_DARK   = new Color(79, 70, 229);    // Indigo 600
    public static final Color PRIMARY_LIGHT  = new Color(165, 180, 252);  // Indigo 300
    public static final Color ACCENT         = new Color(16, 185, 129);   // Emerald 500
    public static final Color ACCENT_DARK    = new Color(5, 150, 105);    // Emerald 600
    public static final Color WARNING        = new Color(245, 158, 11);   // Amber 500
    public static final Color DANGER         = new Color(239, 68, 68);    // Red 500
    public static final Color DANGER_DARK    = new Color(220, 38, 38);    // Red 600

    // Tồn kho thấp - row highlight
    public static final Color LOW_STOCK_BG   = new Color(127, 29, 29, 40);   // Red 900 semi-transparent
    public static final Color LOW_STOCK_TEXT  = new Color(252, 165, 165);     // Red 300
    public static final Color WARN_STOCK_BG  = new Color(120, 53, 15, 35);   // Amber 900 semi-transparent
    public static final Color WARN_STOCK_TEXT = new Color(253, 230, 138);     // Amber 200
    public static final int LOW_STOCK_THRESHOLD = 5;
    public static final int WARN_STOCK_THRESHOLD = 15;

    public static final Color BG_DARK        = new Color(15, 23, 42);     // Slate 900
    public static final Color BG_CARD        = new Color(30, 41, 59);     // Slate 800
    public static final Color BG_CARD_HOVER  = new Color(51, 65, 85);     // Slate 700
    public static final Color BG_INPUT       = new Color(51, 65, 85);     // Slate 700
    public static final Color BG_ROW_ALT     = new Color(38, 50, 70);     // Hàng xen kẽ trong bảng
    public static final Color BORDER         = new Color(71, 85, 105);    // Slate 600

    public static final Color TEXT_PRIMARY   = new Color(248, 250, 252);  // Slate 50
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);  // Slate 400
    public static final Color TEXT_MUTED     = new Color(100, 116, 139);  // Slate 500

    // ── Status Colors ──
    public static final Color STATUS_DRAFT     = new Color(148, 163, 184);  // Gray
    public static final Color STATUS_APPROVED  = new Color(59, 130, 246);   // Blue
    public static final Color STATUS_RECEIVED  = new Color(34, 197, 94);    // Green
    public static final Color STATUS_CANCELLED = new Color(239, 68, 68);    // Red
    public static final Color STATUS_ACTIVE    = new Color(34, 197, 94);
    public static final Color STATUS_CLOSED    = new Color(148, 163, 184);

    // ── Fonts ──
    public static final Font FONT_TITLE     = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE  = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_HEADING   = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BODY      = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL     = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO      = new Font("Monospaced", Font.PLAIN, 13);

    // ── Dimensions ──
    public static final int PADDING = 16;
    public static final int PADDING_SM = 8;
    public static final int PADDING_LG = 24;
    public static final int BORDER_RADIUS = 8;
    public static final int TABLE_ROW_HEIGHT = 36;

    // ── User Roles ──
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_LIBRARIAN = "LIBRARIAN";

    /**
     * Lấy màu theo trạng thái đơn mua
     */
    public static Color getOrderStatusColor(String status) {
        return switch (status) {
            case "DRAFT"     -> STATUS_DRAFT;
            case "APPROVED"  -> STATUS_APPROVED;
            case "RECEIVED"  -> STATUS_RECEIVED;
            case "CANCELLED" -> STATUS_CANCELLED;
            default -> TEXT_SECONDARY;
        };
    }

    /**
     * Lấy màu theo trạng thái ngân sách
     */
    public static Color getBudgetStatusColor(String status) {
        return switch (status) {
            case "ACTIVE" -> STATUS_ACTIVE;
            case "CLOSED" -> STATUS_CLOSED;
            default -> TEXT_SECONDARY;
        };
    }

    /**
     * Lấy màu theo trạng thái bất kỳ (đơn mua hoặc ngân sách)
     */
    public static Color getStatusColor(String status) {
        return switch (status) {
            case "DRAFT"     -> STATUS_DRAFT;
            case "APPROVED"  -> STATUS_APPROVED;
            case "RECEIVED"  -> STATUS_RECEIVED;
            case "CANCELLED" -> STATUS_CANCELLED;
            case "ACTIVE"    -> STATUS_ACTIVE;
            case "CLOSED"    -> STATUS_CLOSED;
            default -> TEXT_SECONDARY;
        };
    }
}
