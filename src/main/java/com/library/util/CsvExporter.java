package com.library.util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility xuất dữ liệu từ JTable ra file CSV (mở được bằng Excel).
 * Sử dụng BOM UTF-8 để Excel hiển thị tiếng Việt đúng.
 */
public class CsvExporter {

    private CsvExporter() {}

    /**
     * Xuất toàn bộ dữ liệu trong tableModel ra file CSV.
     * Hiển thị dialog chọn nơi lưu file.
     *
     * @param parent    Component cha (để hiển thị dialog)
     * @param model     Dữ liệu bảng
     * @param baseName  Tên file gợi ý (không cần .csv)
     * @return true nếu xuất thành công
     */
    public static boolean exportToCSV(java.awt.Component parent, DefaultTableModel model, String baseName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn nơi lưu file CSV");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        chooser.setSelectedFile(new File(baseName + "_" + timestamp + ".csv"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            // BOM UTF-8 để Excel nhận dạng encoding
            writer.write('\uFEFF');

            // Header
            int colCount = model.getColumnCount();
            for (int i = 0; i < colCount; i++) {
                if (i > 0) writer.print(",");
                writer.print(escapeCSV(model.getColumnName(i)));
            }
            writer.println();

            // Rows
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < colCount; col++) {
                    if (col > 0) writer.print(",");
                    Object val = model.getValueAt(row, col);
                    writer.print(escapeCSV(val != null ? val.toString() : ""));
                }
                writer.println();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Lỗi khi xuất file: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Escape giá trị cho CSV: nếu chứa dấu phẩy, xuống dòng, hoặc dấu ngoặc kép thì bọc trong ""
     */
    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
