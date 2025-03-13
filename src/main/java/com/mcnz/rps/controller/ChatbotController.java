package com.mcnz.rps.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/chatbot") // Đổi đúng endpoint để khớp với request từ frontend
@CrossOrigin("*") // Cho phép frontend truy cập API
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);
    private static final String GEMINI_API_KEY = ""; // API Key của Google Gemini AI

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        if (request == null || !request.containsKey("message")) {
            logger.error("Lỗi: Request không chứa 'message'.");
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu dữ liệu 'message'"));
        }

        String userMessage = request.get("message").trim().toLowerCase();
        logger.info("Người dùng: " + userMessage);

        // Đọc dữ liệu từ responses.json
        Map<String, Object> responseData = readJsonData();
        if (responseData != null) {
            String reply = processMessage(userMessage, responseData);
            if (reply != null) {
                logger.info("Trả lời từ hệ thống: " + reply);
                return ResponseEntity.ok(Map.of("reply", reply));
            }
        }

        // Gọi Google Gemini AI API nếu không có dữ liệu phù hợp
        String geminiReply = callGeminiApi(userMessage);
        logger.info("Trả lời từ Gemini AI: " + geminiReply);

        return ResponseEntity.ok(Map.of("reply", geminiReply));
    }

    // Đọc dữ liệu từ responses.json
    private Map<String, Object> readJsonData() {
        try {
            String json = new String(Files.readAllBytes(Paths.get(new ClassPathResource("static/responses.json").getURI())));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, Map.class);
        } catch (IOException e) {
            logger.error("Lỗi khi đọc responses.json: ", e);
            return null;
        }
    }

    private String processMessage(String message, Map<String, Object> data) {
        // Chuẩn hóa tin nhắn của người dùng (chuyển về chữ thường, loại bỏ khoảng trắng thừa)
        String normalizedMessage = message.toLowerCase().trim();

        // Kiểm tra xem tin nhắn có khớp chính xác với một từ khóa trong JSON không
        if (data.containsKey(normalizedMessage)) {
            return data.get(normalizedMessage).toString();
        }

        // Tìm kiếm từ khóa gần đúng nhất trong danh sách key của responses.json
        String bestMatch = findClosestMatch(normalizedMessage, data.keySet());
        if (bestMatch != null) {
            return data.get(bestMatch).toString();
        }

        // Nếu không tìm thấy phản hồi phù hợp
        return null;
    }

    // Hàm tìm kiếm từ khóa gần đúng dựa trên độ tương đồng
    private String findClosestMatch(String input, Set<String> keys) {
        int minDistance = Integer.MAX_VALUE;
        String closestMatch = null;

        for (String key : keys) {
            int distance = levenshteinDistance(input, key);
            if (distance < minDistance && distance <= 2) { // Giới hạn lỗi sai tối đa 2 ký tự
                minDistance = distance;
                closestMatch = key;
            }
        }
        return closestMatch;
    }

    // Thuật toán tính khoảng cách Levenshtein giữa hai chuỗi
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }
        return dp[a.length()][b.length()];
    }


    // Gọi Google Gemini AI API
    private String callGeminiApi(String userMessage) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=" + GEMINI_API_KEY;
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", userMessage)))));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);
            if (responseEntity.getBody() != null && responseEntity.getBody().containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseEntity.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    return parts.get(0).get("text").toString();
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi gọi Google Gemini API: ", e);
        }

        return "Xin lỗi, tôi chưa thể trả lời ngay lúc này.";
    }
}
