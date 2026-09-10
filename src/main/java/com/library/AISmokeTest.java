package com.library;

import com.library.config.AIConfig;
import com.library.model.BookMetadataSuggestion;
import com.library.service.AIService;

/**
 * Smoke test kiểm tra hoạt động của AI API trực tiếp
 */
public class AISmokeTest {

    public static void main(String[] args) {
        System.out.println("=== KIỂM TRA TÍCH HỢP AI API ===");
        System.out.println("Base URL: " + AIConfig.getBaseUrl());
        System.out.println("Model: " + AIConfig.getModel());
        System.out.println("API Key: " + AIConfig.getApiKey().substring(0, 8) + "...");

        try {
            System.out.println("\n1. Thử nghiệm Chat với AI:");
            String reply = AIService.chat("Chào bạn! Hãy giới thiệu ngắn gọn trong 1 câu vai trò của bạn.", "Bạn là Trợ lý AI Thư viện.");
            System.out.println("-> Phản hồi từ AI:\n" + reply);

            System.out.println("\n2. Thử nghiệm AI Gợi ý sách 'Clean Code':");
            BookMetadataSuggestion sugg = AIService.suggestBookInfo("Clean Code Robert C Martin");
            System.out.println("-> Tên sách: " + sugg.getTitle());
            System.out.println("-> Tác giả: " + sugg.getAuthor());
            System.out.println("-> NXB: " + sugg.getPublisher());
            System.out.println("-> Năm XB: " + sugg.getPublicationYear());
            System.out.println("-> Thể loại: " + sugg.getCategoryName());
            System.out.println("-> Giá ước tính: " + sugg.getEstimatedPrice());
            System.out.println("-> Mô tả: " + sugg.getDescription());

            System.out.println("\n=== TẤT CẢ TÍNH NĂNG AI HOẠT ĐỘNG HOÀN HẢO! ===");
        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra AI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
