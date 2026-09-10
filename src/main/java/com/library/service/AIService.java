package com.library.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.library.config.AIConfig;
import com.library.model.BookMetadataSuggestion;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service xử lý các tác vụ AI thông qua OpenAI Compatible API
 */
public class AIService {

    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    /**
     * Gửi yêu cầu Chat đến AI Model
     *
     * @param userMessage  Tin nhắn từ người dùng
     * @param systemPrompt Hướng dẫn hệ thống (vai trò AI)
     * @return Câu trả lời từ AI
     * @throws Exception Khi gặp lỗi kết nối hoặc API trả về lỗi
     */
    public static String chat(String userMessage, String systemPrompt) throws Exception {
        String apiKey = AIConfig.getApiKey();
        String baseUrl = AIConfig.getBaseUrl();
        String model = AIConfig.getModel();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key AI chưa được cấu hình! Vui lòng mở 'Cài đặt AI' để nhập key.");
        }

        baseUrl = baseUrl.trim();
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }

        // Tự động sử dụng Google Gemini Native REST API khi dùng Google API
        if (baseUrl.contains("googleapis.com")) {
            return callGeminiNative(userMessage, systemPrompt, apiKey, baseUrl, model);
        }

        // Đảm bảo endpoint OpenAI là /chat/completions
        String endpoint = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("temperature", 0.7);

        JsonArray messages = new JsonArray();

        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            JsonObject sysMsg = new JsonObject();
            sysMsg.addProperty("role", "system");
            sysMsg.addProperty("content", systemPrompt);
            messages.add(sysMsg);
        }

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);

        requestBody.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(AIConfig.getTimeoutSeconds()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Authorization", "Bearer " + apiKey.trim())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String errorMsg = "Lỗi gọi AI (HTTP " + response.statusCode() + "): " + response.body();
            try {
                JsonObject errObj = JsonParser.parseString(response.body()).getAsJsonObject();
                if (errObj.has("error") && errObj.get("error").isJsonObject()) {
                    JsonObject errDetail = errObj.getAsJsonObject("error");
                    if (errDetail.has("message")) {
                        errorMsg = errDetail.get("message").getAsString();
                    }
                }
            } catch (Exception ignored) {}
            throw new RuntimeException(errorMsg);
        }

        JsonObject resJson = JsonParser.parseString(response.body()).getAsJsonObject();
        if (resJson.has("choices") && resJson.getAsJsonArray("choices").size() > 0) {
            JsonObject firstChoice = resJson.getAsJsonArray("choices").get(0).getAsJsonObject();
            if (firstChoice.has("message") && firstChoice.getAsJsonObject("message").has("content")) {
                return firstChoice.getAsJsonObject("message").get("content").getAsString().trim();
            }
        }

        throw new RuntimeException("Phản hồi từ AI không chứa nội dung hợp lệ.");
    }

    /**
     * Gọi trực tiếp Google Gemini Native REST API (v1beta/models/{model}:generateContent)
     */
    private static String callGeminiNative(String userMessage, String systemPrompt, String apiKey, String baseUrl, String model) throws Exception {
        String cleanBase = baseUrl.replaceAll("/openai/?$", "").replaceAll("/+$", "");
        if (!cleanBase.contains("/v1beta") && !cleanBase.contains("/v1")) {
            cleanBase += "/v1beta";
        }

        String endpoint = cleanBase + "/models/" + model.trim() + ":generateContent?key=" + apiKey.trim();

        JsonObject payload = new JsonObject();

        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            JsonObject sysInst = new JsonObject();
            JsonArray sysParts = new JsonArray();
            JsonObject sysPart = new JsonObject();
            sysPart.addProperty("text", systemPrompt);
            sysParts.add(sysPart);
            sysInst.add("parts", sysParts);
            payload.add("system_instruction", sysInst);
        }

        JsonArray contents = new JsonArray();
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", userMessage);
        parts.add(part);
        userContent.add("parts", parts);
        contents.add(userContent);
        payload.add("contents", contents);

        int maxRetries = 3;
        HttpResponse<String> response = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(AIConfig.getTimeoutSeconds()))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                break;
            }

            if ((response.statusCode() == 429 || response.statusCode() == 503 || response.statusCode() == 500
                    || (response.body() != null && (response.body().contains("high demand") || response.body().contains("quota"))))
                    && attempt < maxRetries) {
                Thread.sleep(attempt * 1500L);
                continue;
            }
            break;
        }

        if (response.statusCode() != 200) {
            String errorMsg = "Lỗi Gemini API (HTTP " + response.statusCode() + "): " + response.body();
            try {
                JsonObject errObj = JsonParser.parseString(response.body()).getAsJsonObject();
                if (errObj.has("error") && errObj.get("error").isJsonObject()) {
                    JsonObject errDetail = errObj.getAsJsonObject("error");
                    if (errDetail.has("message")) {
                        errorMsg = errDetail.get("message").getAsString();
                    }
                }
            } catch (Exception ignored) {}
            throw new RuntimeException(errorMsg);
        }

        JsonObject resJson = JsonParser.parseString(response.body()).getAsJsonObject();
        if (resJson.has("candidates") && resJson.getAsJsonArray("candidates").size() > 0) {
            JsonObject candidate = resJson.getAsJsonArray("candidates").get(0).getAsJsonObject();
            if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                JsonArray resParts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                if (resParts.size() > 0 && resParts.get(0).getAsJsonObject().has("text")) {
                    return resParts.get(0).getAsJsonObject().get("text").getAsString().trim();
                }
            }
        }

        throw new RuntimeException("Phản hồi từ Gemini API không chứa nội dung hợp lệ.");
    }

    /**
     * Gợi ý thông tin sách đầy đủ (Tác giả, NXB, Năm XB, Thể loại, Mô tả, Giá ước tính)
     * từ tên sách hoặc từ khóa
     */
    public static BookMetadataSuggestion suggestBookInfo(String titleOrKeyword) throws Exception {
        String systemPrompt = "Bạn là trợ lý cơ sở dữ liệu thư viện chuyên nghiệp. "
                + "Nhiệm vụ của bạn là tra cứu và trích xuất thông tin xuất bản chính xác cho cuốn sách hoặc chủ đề được cung cấp. "
                + "Bạn BẮT BUỘC phải trả về kết quả ĐÚNG ĐỊNH DẠNG JSON sau và KHÔNG thêm bất kỳ văn bản giải thích nào khác:\n"
                + "{\n"
                + "  \"title\": \"Tên sách chuẩn\",\n"
                + "  \"author\": \"Tên tác giả\",\n"
                + "  \"publisher\": \"Nhà xuất bản phổ biến tại VN hoặc quốc tế\",\n"
                + "  \"publicationYear\": 2023,\n"
                + "  \"categoryName\": \"Tên thể loại phù hợp (ví dụ: Công nghệ thông tin, Kinh tế, Văn học, Khoa học cơ bản, Kỹ năng sống, v.v.)\",\n"
                + "  \"estimatedPrice\": 120000,\n"
                + "  \"isbn\": \"978-xxxxxxxxxx\",\n"
                + "  \"description\": \"Tóm tắt nội dung ngắn gọn 2-3 câu về cuốn sách và đối tượng bạn đọc phù hợp.\"\n"
                + "}";

        String userPrompt = "Hãy gợi ý và chuẩn hóa thông tin chi tiết cho tài liệu/sách: " + titleOrKeyword;
        String rawResponse = chat(userPrompt, systemPrompt);

        // Trích xuất JSON từ phản hồi (nếu AI có kèm markdown ```json ... ```)
        String jsonContent = extractJson(rawResponse);

        try {
            JsonObject obj = JsonParser.parseString(jsonContent).getAsJsonObject();
            BookMetadataSuggestion suggestion = new BookMetadataSuggestion();

            if (obj.has("title") && !obj.get("title").isJsonNull()) {
                suggestion.setTitle(obj.get("title").getAsString());
            }
            if (obj.has("author") && !obj.get("author").isJsonNull()) {
                suggestion.setAuthor(obj.get("author").getAsString());
            }
            if (obj.has("publisher") && !obj.get("publisher").isJsonNull()) {
                suggestion.setPublisher(obj.get("publisher").getAsString());
            }
            if (obj.has("publicationYear") && !obj.get("publicationYear").isJsonNull()) {
                try {
                    suggestion.setPublicationYear(obj.get("publicationYear").getAsInt());
                } catch (Exception ignored) {}
            }
            if (obj.has("categoryName") && !obj.get("categoryName").isJsonNull()) {
                suggestion.setCategoryName(obj.get("categoryName").getAsString());
            }
            if (obj.has("estimatedPrice") && !obj.get("estimatedPrice").isJsonNull()) {
                try {
                    suggestion.setEstimatedPrice(obj.get("estimatedPrice").getAsDouble());
                } catch (Exception ignored) {}
            }
            if (obj.has("isbn") && !obj.get("isbn").isJsonNull()) {
                suggestion.setIsbn(obj.get("isbn").getAsString());
            }
            if (obj.has("description") && !obj.get("description").isJsonNull()) {
                suggestion.setDescription(obj.get("description").getAsString());
            }

            return suggestion;
        } catch (Exception e) {
            throw new RuntimeException("Không thể phân tích dữ liệu JSON trả về từ AI: " + e.getMessage() + "\nPhản hồi gốc: " + rawResponse);
        }
    }

    /**
     * Phân tích thông minh số liệu tài chính & gợi ý chiến lược mua sắm
     */
    public static String analyzeBudgetAndReport(String dataSummary) throws Exception {
        String systemPrompt = "Bạn là Chuyên gia Tư vấn Quản lý Ngân sách & Mua sắm Tài liệu Thư viện Đại học. "
                + "Hãy phân tích số liệu tài chính, ngân sách và tình hình nhập/xuất tài liệu thư viện dưới đây. "
                + "Nêu rõ:\n"
                + "1. Nhận xét tổng quan tình hình giải ngân ngân sách (Tỷ lệ đã chi, mức an toàn).\n"
                + "2. Cảnh báo rủi ro (vượt ngân sách, danh mục tồn kho quá thấp hoặc quá cao).\n"
                + "3. Đề xuất kế hoạch mua sắm ưu tiên cho kỳ tới để tối ưu chi phí và phục vụ bạn đọc tốt nhất.\n"
                + "Trình bày rõ ràng, súc tích bằng tiếng Việt có gạch đầu dòng và định dạng dễ đọc.";

        return chat("Dưới đây là số liệu thống kê hiện tại của hệ thống thư viện:\n" + dataSummary, systemPrompt);
    }

    private static String extractJson(String text) {
        if (text == null) return "{}";
        Pattern pattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1).trim();
        }
        return text.trim();
    }
}
