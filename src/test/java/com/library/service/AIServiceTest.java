package com.library.service;

import com.library.config.AIConfig;
import com.library.model.BookMetadataSuggestion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AIServiceTest {

    @Test
    public void testAIConfig() {
        assertNotNull(AIConfig.getApiKey());
        assertEquals("sk_M24YFwUSGkpRTmWuyTbe6CfKPhqNpHNGQqjIPzjcDJo", AIConfig.getApiKey());
        assertNotNull(AIConfig.getBaseUrl());
    }

    @Test
    public void testAIServiceConnection() {
        try {
            String response = AIService.chat("Chào bạn, hãy trả lời đúng 1 từ: 'OK'", "Bạn là trợ lý ảo.");
            System.out.println("AI Response Test: " + response);
            assertNotNull(response);
            assertFalse(response.trim().isEmpty());
        } catch (Exception e) {
            System.out.println("AI Service live call result / note: " + e.getMessage());
            // Có thể endpoint yêu cầu mạng / IP phù hợp
        }
    }
}
