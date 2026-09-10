package com.library.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Cấu hình dịch vụ AI (OpenAI-compatible)
 */
public class AIConfig {

    private static final String CONFIG_FILE = "ai_config.properties";

    public static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    public static final String GEMINI_MODEL = "gemini-2.5-flash";

    public static final String OPENAI_BASE_URL = "https://api.openai.com/v1";
    public static final String OPENAI_MODEL = "gpt-4o-mini";

    public static final String HIGHWAY_BASE_URL = "https://api.highwayapi.ai/openai/v1";
    public static final String HIGHWAY_MODEL = "gpt-4o-mini";

    public static final String DEFAULT_BASE_URL = GEMINI_BASE_URL;
    public static final String DEFAULT_API_KEY = "";
    public static final String DEFAULT_MODEL = GEMINI_MODEL;

    private static String apiKey = DEFAULT_API_KEY;
    private static String baseUrl = DEFAULT_BASE_URL;
    private static String model = DEFAULT_MODEL;
    private static int timeoutSeconds = 60;

    static {
        loadConfig();
    }

    public static synchronized void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(in);
                apiKey = props.getProperty("ai.api_key", DEFAULT_API_KEY);
                baseUrl = props.getProperty("ai.base_url", DEFAULT_BASE_URL);
                model = props.getProperty("ai.model", DEFAULT_MODEL);
                timeoutSeconds = Integer.parseInt(props.getProperty("ai.timeout", "60"));
            } catch (Exception e) {
                System.err.println("Không thể đọc file cấu hình AI, sử dụng mặc định: " + e.getMessage());
            }
        }
    }

    public static synchronized void saveConfig(String newApiKey, String newBaseUrl, String newModel) {
        apiKey = newApiKey;
        baseUrl = newBaseUrl;
        model = newModel;

        Properties props = new Properties();
        props.setProperty("ai.api_key", apiKey);
        props.setProperty("ai.base_url", baseUrl);
        props.setProperty("ai.model", model);
        props.setProperty("ai.timeout", String.valueOf(timeoutSeconds));

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "AI Configuration Settings");
        } catch (IOException e) {
            System.err.println("Lỗi lưu cấu hình AI: " + e.getMessage());
        }
    }

    public static synchronized String getApiKey() {
        return apiKey;
    }

    public static synchronized void setApiKey(String key) {
        apiKey = key;
    }

    public static synchronized String getBaseUrl() {
        return baseUrl;
    }

    public static synchronized void setBaseUrl(String url) {
        baseUrl = url;
    }

    public static synchronized String getModel() {
        return model;
    }

    public static synchronized void setModel(String m) {
        model = m;
    }

    public static synchronized int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
