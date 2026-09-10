package com.library.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import java.awt.Color;

/**
 * Tiện ích tải icon SVG từ resources (/icons/*.svg) với kích thước
 * và màu tùy chọn (tint qua FlatSVGIcon.ColorFilter).
 */
public final class Icons {

    private Icons() {}

    /** Icon SVG với màu gốc (#94a3b8 - TEXT_SECONDARY) */
    public static FlatSVGIcon get(String name, int size) {
        return new FlatSVGIcon("icons/" + name + ".svg", size, size);
    }

    /** Icon SVG được tint sang màu chỉ định */
    public static FlatSVGIcon get(String name, int size, Color tint) {
        FlatSVGIcon icon = get(name, size);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> tint));
        return icon;
    }

    /** Icon chuẩn cho nút (14px, màu trắng) */
    public static FlatSVGIcon button(String name) {
        return get(name, 14, Color.WHITE);
    }

    /** Icon tiêu đề panel (20px, màu primary light) */
    public static FlatSVGIcon title(String name) {
        return get(name, 20, AppConstants.PRIMARY_LIGHT);
    }
}
