package com.library.view.components;

import com.library.util.AppConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * JButton tùy chỉnh với gradient, hover effect, rounded corners
 */
public class StyledButton extends JButton {

    public StyledButton(String text) {
        super(text);
        setFont(AppConstants.FONT_HEADING);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(140, 36));
        setBackground(AppConstants.PRIMARY);
        setForeground(Color.WHITE);
    }

    // Added for backward compatibility with old UI logic
    public StyledButton(String text, Color bgColor, Color hoverColor) {
        this(text);
        setBackground(bgColor);
    }

    public static StyledButton danger(String text) {
        StyledButton btn = new StyledButton(text);
        btn.setBackground(AppConstants.DANGER);
        return btn;
    }

    public static StyledButton success(String text) {
        StyledButton btn = new StyledButton(text);
        btn.setBackground(AppConstants.ACCENT);
        return btn;
    }

    public static StyledButton warning(String text) {
        StyledButton btn = new StyledButton(text);
        btn.setBackground(AppConstants.WARNING);
        return btn;
    }

    public void setColors(Color bg, Color hover) {
        setBackground(bg);
        // Hover color sẽ được FlatLaf tự động tính toán dựa trên background
    }
}
