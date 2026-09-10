package com.library;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.library.util.AppConstants;
import com.library.view.LoginView;
import com.library.view.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point: Khởi tạo FlatLaf theme và hiển thị LoginView
 */
public class Main {

    public static void main(String[] args) {
        // Set Modern Mac Dark Look and Feel
        try {
            FlatMacDarkLaf.setup();

            // Customize FlatLaf defaults for Modern Dark Mode
            UIManager.put("defaultFont", AppConstants.FONT_BODY);
            
            // Tăng độ bo góc (Rounded Corners) để giao diện mềm mại hơn
            UIManager.put("Component.arc", 12);
            UIManager.put("Button.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            
            // Tối ưu Padding & Margin để tăng không gian trắng (Whitespace)
            UIManager.put("Button.margins", new Insets(6, 16, 6, 16));
            UIManager.put("Component.innerFocusWidth", 1);
            UIManager.put("TextComponent.margins", new Insets(6, 10, 6, 10));
            
            // Cải thiện Thanh cuộn (Scrollbar)
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

            // Cấu hình Bảng (Table) gọn gàng, không bị rối mắt
            UIManager.put("Table.rowHeight", AppConstants.TABLE_ROW_HEIGHT);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.selectionBackground", AppConstants.PRIMARY.darker());
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            
            // Cấu hình Tab (TabbedPane)
            UIManager.put("TabbedPane.selectedBackground", AppConstants.BG_CARD);
            UIManager.put("TabbedPane.underlineColor", AppConstants.PRIMARY);
            UIManager.put("TabbedPane.focusColor", AppConstants.PRIMARY);
            UIManager.put("TabbedPane.tabInsets", new Insets(8, 16, 8, 16));
            
            // Phối màu nền (Color Palette) từ AppConstants
            UIManager.put("ComboBox.background", AppConstants.BG_INPUT);
            UIManager.put("ComboBox.buttonBackground", AppConstants.BG_INPUT);
            UIManager.put("TextField.background", AppConstants.BG_INPUT);
            UIManager.put("PasswordField.background", AppConstants.BG_INPUT);
            UIManager.put("Spinner.background", AppConstants.BG_INPUT);
            UIManager.put("OptionPane.background", AppConstants.BG_CARD);
            UIManager.put("Panel.background", AppConstants.BG_DARK);
            UIManager.put("Menu.selectionBackground", AppConstants.PRIMARY);
            UIManager.put("MenuItem.selectionBackground", AppConstants.PRIMARY);
        } catch (Exception e) {
            System.err.println("Không thể cài FlatLaf, sử dụng theme mặc định.");
        }

        // Launch on EDT
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView(() -> {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            });
            loginView.setVisible(true);
        });
    }
}
