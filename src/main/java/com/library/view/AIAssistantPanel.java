package com.library.view;

import com.library.config.AIConfig;
import com.library.service.AIService;
import com.library.util.AppConstants;
import com.library.util.Icons;
import com.library.view.components.StyledButton;
import com.library.view.components.UiFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Tab Trợ lý AI Thư viện - Trò chuyện, giải đáp nghiệp vụ, tư vấn mua sắm tài liệu
 */
public class AIAssistantPanel extends JPanel {

    private JPanel chatContainer;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private StyledButton btnSend;
    private JLabel statusLabel;

    private static final String SYSTEM_PROMPT =
            "Bạn là Trợ lý AI Quản lý Mua sắm & Nghiệp vụ Thư viện chuyên nghiệp. "
            + "Bạn có nhiệm vụ hỗ trợ thủ thư và ban quản lý thư viện giải đáp các vấn đề như: "
            + "1. Tư vấn lựa chọn tài liệu, sách chuyên ngành, sách tham khảo cho bạn đọc sinh viên và giảng viên.\n"
            + "2. Hướng dẫn nghiệp vụ lập kế hoạch mua sắm, thẩm định báo giá nhà cung cấp và tối ưu hóa ngân sách.\n"
            + "3. Phân tích xu hướng đọc sách và đề xuất danh mục sách mới nổi bật.\n"
            + "4. Giải đáp các câu hỏi quy trình quản lý kho, kiểm kê, xuất nhập tài liệu.\n"
            + "Hãy trả lời bằng tiếng Việt lịch sự, súc tích, logic và chuyên nghiệp.";

    public AIAssistantPanel() {
        initUI();
        addWelcomeMessage();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 10));
        setBackground(AppConstants.BG_DARK);
        setBorder(new EmptyBorder(AppConstants.PADDING, AppConstants.PADDING, AppConstants.PADDING, AppConstants.PADDING));

        // 1. Header Toolbar
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // 2. Chat Area
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(AppConstants.BG_DARK);

        JPanel chatWrapper = new JPanel(new BorderLayout());
        chatWrapper.setBackground(AppConstants.BG_DARK);
        chatWrapper.add(chatContainer, BorderLayout.NORTH);

        scrollPane = new JScrollPane(chatWrapper);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        scrollPane.setBackground(AppConstants.BG_DARK);
        scrollPane.getViewport().setBackground(AppConstants.BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 3. Bottom Panel (Quick Prompts + Input Bar)
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);

        JPanel quickPrompts = createQuickPromptsPanel();
        bottomPanel.add(quickPrompts, BorderLayout.NORTH);

        JPanel inputBar = createInputBar();
        bottomPanel.add(inputBar, BorderLayout.CENTER);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel title = UiFactory.panelTitle("Trợ lý AI Thư viện", Icons.get("bot", 22, AppConstants.PRIMARY_LIGHT));
        statusLabel = new JLabel("● Sẵn sàng (" + AIConfig.getModel() + ")");
        statusLabel.setFont(AppConstants.FONT_SMALL);
        statusLabel.setForeground(AppConstants.ACCENT);

        left.add(title);
        left.add(statusLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        StyledButton btnConfig = UiFactory.secondaryButton("⚙ Cài đặt AI");
        btnConfig.setPreferredSize(new Dimension(120, 32));
        btnConfig.addActionListener(e -> showConfigDialog());

        StyledButton btnClear = UiFactory.secondaryButton("Xóa đoạn chat");
        btnClear.setIcon(Icons.get("trash", 14, AppConstants.TEXT_PRIMARY));
        btnClear.setPreferredSize(new Dimension(135, 32));
        btnClear.addActionListener(e -> {
            chatContainer.removeAll();
            addWelcomeMessage();
            chatContainer.revalidate();
            chatContainer.repaint();
        });

        right.add(btnConfig);
        right.add(btnClear);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createQuickPromptsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);

        JLabel lblHint = new JLabel("Gợi ý nhanh: ");
        lblHint.setFont(AppConstants.FONT_SMALL);
        lblHint.setForeground(AppConstants.TEXT_MUTED);
        panel.add(lblHint);

        String[] prompts = {
            "Gợi ý sách CNTT & AI nên mua cho sinh viên",
            "Cách tối ưu ngân sách mua sắm năm học mới",
            "Mẹo thương lượng giá với nhà xuất bản",
            "Quy trình kiểm kê và thanh lý sách cũ"
        };

        for (String prompt : prompts) {
            JButton chip = new JButton(prompt);
            chip.setFont(AppConstants.FONT_SMALL);
            chip.setForeground(AppConstants.TEXT_PRIMARY);
            chip.setBackground(AppConstants.BG_CARD);
            chip.setFocusPainted(false);
            chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppConstants.BORDER, 1),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
            chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chip.addActionListener(e -> {
                inputField.setText(prompt);
                sendMessage();
            });
            panel.add(chip);
        }

        return panel;
    }

    private JPanel createInputBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);

        inputField = new JTextField();
        UiFactory.styleField(inputField);
        inputField.setPreferredSize(new Dimension(300, 38));
        inputField.putClientProperty("JTextField.placeholderText", "Hỏi AI bất kỳ điều gì về mua sắm sách, ngân sách, nghiệp vụ thư viện... (Nhấn Enter để gửi)");

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    sendMessage();
                }
            }
        });

        btnSend = new StyledButton("Gửi", AppConstants.PRIMARY, AppConstants.PRIMARY_DARK);
        btnSend.setIcon(Icons.get("play", 14, Color.WHITE));
        btnSend.setPreferredSize(new Dimension(90, 38));
        btnSend.addActionListener(e -> sendMessage());

        bar.add(inputField, BorderLayout.CENTER);
        bar.add(btnSend, BorderLayout.EAST);
        return bar;
    }

    private void addWelcomeMessage() {
        String welcome = "Xin chào! Tôi là **Trợ lý AI Thư viện** được tích hợp mô hình ngôn ngữ lớn.\n\n"
                + "Tôi có thể giúp bạn:\n"
                + "• **Tư vấn danh mục sách & giáo trình** cần trang bị theo từng chuyên ngành.\n"
                + "• **Phân tích chiến lược phân bổ ngân sách** và đánh giá đề xuất mua sắm.\n"
                + "• **Tự động trích xuất thông tin, tóm tắt sách** và định giá tham khảo.\n"
                + "• **Giải đáp quy trình nghiệp vụ** quản lý tài liệu và nhà cung cấp.\n\n"
                + "Hãy nhập câu hỏi bên dưới hoặc chọn gợi ý nhanh để bắt đầu!";
        appendMessage("Trợ lý AI", welcome, false);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        appendMessage("Bạn", text, true);
        inputField.setText("");
        inputField.setEnabled(false);
        btnSend.setEnabled(false);
        statusLabel.setText("● Đang xử lý câu trả lời...");
        statusLabel.setForeground(AppConstants.WARNING);

        JPanel typingBubble = appendTypingIndicator();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                String dynamicPrompt = buildDynamicSystemPrompt();
                return AIService.chat(text, dynamicPrompt);
            }

            @Override
            protected void done() {
                chatContainer.remove(typingBubble);
                try {
                    String reply = get();
                    appendMessage("Trợ lý AI", reply, false);
                    statusLabel.setText("● Sẵn sàng (" + AIConfig.getModel() + ")");
                    statusLabel.setForeground(AppConstants.ACCENT);
                } catch (Exception ex) {
                    String err = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    appendMessage("Lỗi AI", "Không thể hoàn thành yêu cầu: " + err, false);
                    statusLabel.setText("● Lỗi kết nối");
                    statusLabel.setForeground(AppConstants.DANGER);
                } finally {
                    inputField.setEnabled(true);
                    btnSend.setEnabled(true);
                    inputField.requestFocusInWindow();
                    chatContainer.revalidate();
                    chatContainer.repaint();
                    scrollToBottom();
                }
            }
        };

        worker.execute();
    }

    private JPanel appendTypingIndicator() {
        JPanel bubble = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bubble.setOpaque(false);
        JLabel lbl = new JLabel("🤖 AI đang suy nghĩ...");
        lbl.setFont(AppConstants.FONT_SMALL);
        lbl.setForeground(AppConstants.TEXT_SECONDARY);
        bubble.add(lbl);
        chatContainer.add(bubble);
        chatContainer.revalidate();
        scrollToBottom();
        return bubble;
    }

    private void appendMessage(String sender, String message, boolean isUser) {
        JPanel row = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 6));
        row.setOpaque(false);

        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(isUser ? AppConstants.PRIMARY_DARK : AppConstants.BG_CARD);
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isUser ? AppConstants.PRIMARY : AppConstants.BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel meta = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 4, 0));
        meta.setOpaque(false);
        JLabel lblSender = new JLabel(sender);
        lblSender.setFont(AppConstants.FONT_HEADING);
        lblSender.setForeground(isUser ? Color.WHITE : AppConstants.PRIMARY_LIGHT);

        String timeStr = new SimpleDateFormat("HH:mm").format(new Date());
        JLabel lblTime = new JLabel(timeStr);
        lblTime.setFont(AppConstants.FONT_SMALL);
        lblTime.setForeground(AppConstants.TEXT_MUTED);

        meta.add(lblSender);
        meta.add(lblTime);

        JEditorPane textPane = new JEditorPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        textPane.setFont(AppConstants.FONT_BODY);

        String htmlContent = formatMarkdownToHtml(message);
        textPane.setText("<html><body style='color:#f8fafc; font-family:sans-serif; font-size:12px; margin:0; padding:0; width:520px;'>"
                + htmlContent + "</body></html>");

        bubble.add(meta);
        bubble.add(Box.createVerticalStrut(4));
        bubble.add(textPane);

        row.add(bubble);
        chatContainer.add(row);
        chatContainer.revalidate();
        chatContainer.repaint();
        scrollToBottom();
    }

    private String formatMarkdownToHtml(String text) {
        if (text == null) return "";
        String formatted = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")
                .replaceAll("\\*(.*?)\\*", "<i>$1</i>")
                .replaceAll("`([^`]+)`", "<code style='background:#334155; padding:2px 4px; border-radius:3px;'>$1</code>")
                .replace("\n", "<br/>");
        return formatted;
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void showConfigDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cấu hình dịch vụ AI", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(520, 360);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(AppConstants.BG_DARK);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(16, 20, 10, 20));

        String[] providers = {
            "Google Gemini (Khuyên dùng - Miễn phí)",
            "OpenAI Official (gpt-4o-mini)",
            "HighwayAPI Proxy",
            "Tùy chỉnh (Custom)"
        };
        JComboBox<String> comboProvider = new JComboBox<>(providers);
        comboProvider.setFont(AppConstants.FONT_BODY);

        JTextField txtKey = new JTextField(AIConfig.getApiKey());
        UiFactory.styleField(txtKey);
        txtKey.putClientProperty("JTextField.placeholderText", "Nhập API Key (ví dụ: AIzaSy...)");

        JTextField txtBaseUrl = new JTextField(AIConfig.getBaseUrl());
        UiFactory.styleField(txtBaseUrl);

        JTextField txtModel = new JTextField(AIConfig.getModel());
        UiFactory.styleField(txtModel);

        JLabel lblHelp = new JLabel("<html>Lấy Gemini API Key miễn phí tại: <font color='#6366f1'>https://aistudio.google.com/apikey</font></html>");
        lblHelp.setFont(AppConstants.FONT_SMALL);
        lblHelp.setForeground(AppConstants.TEXT_MUTED);

        if (AIConfig.getBaseUrl().contains("googleapis")) {
            comboProvider.setSelectedIndex(0);
        } else if (AIConfig.getBaseUrl().contains("api.openai.com")) {
            comboProvider.setSelectedIndex(1);
        } else if (AIConfig.getBaseUrl().contains("highwayapi")) {
            comboProvider.setSelectedIndex(2);
        } else {
            comboProvider.setSelectedIndex(3);
        }

        comboProvider.addActionListener(e -> {
            int idx = comboProvider.getSelectedIndex();
            if (idx == 0) {
                txtBaseUrl.setText(AIConfig.GEMINI_BASE_URL);
                txtModel.setText(AIConfig.GEMINI_MODEL);
                lblHelp.setText("<html>Lấy Gemini API Key miễn phí tại: <font color='#6366f1'>https://aistudio.google.com/apikey</font></html>");
            } else if (idx == 1) {
                txtBaseUrl.setText(AIConfig.OPENAI_BASE_URL);
                txtModel.setText(AIConfig.OPENAI_MODEL);
                lblHelp.setText("<html>Lấy OpenAI Key tại: <font color='#6366f1'>https://platform.openai.com/api-keys</font></html>");
            } else if (idx == 2) {
                txtBaseUrl.setText(AIConfig.HIGHWAY_BASE_URL);
                txtModel.setText(AIConfig.HIGHWAY_MODEL);
                lblHelp.setText("<html>Provider proxy của HighwayAPI (Cần nạp credit)</html>");
            }
        });

        com.library.view.components.FormPanel form = new com.library.view.components.FormPanel(120);
        form.addField("Nguồn AI:", comboProvider);
        form.addField("API Key:", txtKey);
        form.addField("Base URL:", txtBaseUrl);
        form.addField("Model:", txtModel);

        content.add(form);
        content.add(Box.createVerticalStrut(8));
        content.add(lblHelp);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setOpaque(false);

        StyledButton btnSave = StyledButton.success("Lưu cấu hình");
        btnSave.setPreferredSize(new Dimension(120, 34));
        btnSave.addActionListener(e -> {
            String key = txtKey.getText().trim();
            if (key.isEmpty()) {
                UiFactory.showWarning(dialog, "Vui lòng nhập API Key!");
                txtKey.requestFocus();
                return;
            }
            AIConfig.saveConfig(key, txtBaseUrl.getText().trim(), txtModel.getText().trim());
            statusLabel.setText("● Sẵn sàng (" + AIConfig.getModel() + ")");
            statusLabel.setForeground(AppConstants.ACCENT);
            JOptionPane.showMessageDialog(dialog, "Đã lưu cấu hình AI thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        StyledButton btnClose = UiFactory.secondaryButton("Hủy");
        btnClose.setPreferredSize(new Dimension(90, 34));
        btnClose.addActionListener(e -> dialog.dispose());

        actions.add(btnClose);
        actions.add(btnSave);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Tự động tổng hợp ngữ cảnh dữ liệu thực tế từ CSDL để AI trả lời chính xác
     */
    private String buildDynamicSystemPrompt() {
        StringBuilder sb = new StringBuilder(SYSTEM_PROMPT);
        try {
            com.library.dao.DocumentDAO docDAO = new com.library.dao.DocumentDAO();
            java.util.List<com.library.model.Document> docs = docDAO.findAll();
            if (docs != null && !docs.isEmpty()) {
                sb.append("\n\n=== DỮ LIỆU THỰC TẾ TRONG KHO THƯ VIỆN HIỆN TẠI (Tổng: ").append(docs.size()).append(" tài liệu) ===\n");
                int limit = Math.min(docs.size(), 60);
                for (int i = 0; i < limit; i++) {
                    com.library.model.Document d = docs.get(i);
                    sb.append(String.format("- ID %d: \"%s\" | Tác giả: %s | Danh mục: %s | Tồn kho: %d | Đơn giá: %,d ₫ | NXB: %s (%d)\n",
                            d.getId(), d.getTitle(),
                            d.getAuthor() != null && !d.getAuthor().isEmpty() ? d.getAuthor() : "Chưa rõ",
                            d.getCategoryName() != null ? d.getCategoryName() : "Khác",
                            d.getStockQuantity(),
                            (long) d.getUnitPrice(),
                            d.getPublisher() != null && !d.getPublisher().isEmpty() ? d.getPublisher() : "N/A",
                            d.getPublishYear()));
                }
                if (docs.size() > limit) {
                    sb.append("... và ").append(docs.size() - limit).append(" tài liệu khác.\n");
                }
            }

            com.library.dao.BudgetDAO budgetDAO = new com.library.dao.BudgetDAO();
            java.util.List<com.library.model.Budget> budgets = budgetDAO.findAll();
            if (budgets != null && !budgets.isEmpty()) {
                sb.append("\n=== NGUỒN NGÂN SÁCH HIỆN CÓ ===\n");
                for (com.library.model.Budget b : budgets) {
                    sb.append(String.format("- %s (Năm %d): Tổng %,d ₫, Đã chi %,d ₫, Còn lại %,d ₫ (Trạng thái: %s)\n",
                            b.getBudgetName(), b.getFiscalYear(),
                            (long) b.getTotalAmount(), (long) b.getSpentAmount(), (long) b.getRemainingAmount(),
                            b.getStatus()));
                }
            }

            sb.append("\nQUY TẮC PHẢN HỒI QUAN TRỌNG:\n"
                    + "- Khi người dùng hỏi xem thư viện có những sách gì, sách thuộc lĩnh vực nào, sách nào sắp hết tồn kho, hoặc ngân sách còn bao nhiêu, BẮT BUỘC tra cứu và liệt kê chính xác các cuốn sách từ DỮ LIỆU THỰC TẾ TRONG KHO THƯ VIỆN HIỆN TẠI ở trên kèm số lượng tồn kho và tác giả.\n"
                    + "- Nếu người dùng hỏi gợi ý sách mới cần mua thêm ngoài danh mục hiện có, hãy đưa ra gợi ý xuất sắc phù hợp xu hướng.");
        } catch (Exception ignored) {}
        return sb.toString();
    }
}
