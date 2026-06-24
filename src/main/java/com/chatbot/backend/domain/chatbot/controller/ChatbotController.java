package com.chatbot.backend.domain.chatbot.controller;

import com.chatbot.backend.domain.chat.service.ChatHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final RestTemplate restTemplate;
    private final ChatHistoryService chatHistoryService;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public ChatbotController(
            @Qualifier("chatbotRestTemplate") RestTemplate restTemplate,
            ChatHistoryService chatHistoryService) {
        this.restTemplate = restTemplate;
        this.chatHistoryService = chatHistoryService;
    }

    @Data
    public static class ChatRequest {
        @NotBlank(message = "메시지를 입력해주세요.")
        private String message;
        private List<Map<String, String>> history = new ArrayList<>();
    }

    @PostMapping("/ask")
    public ResponseEntity<Map> askChatbot(@Valid @RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        String url = aiServerUrl + "/integrated-chat";

        ResponseEntity<Map> aiResponse;
        try {
            aiResponse = restTemplate.postForEntity(url, request, Map.class);
        } catch (ResourceAccessException e) {
            String msg = e.getMessage() != null && e.getMessage().contains("timed out")
                    ? "AI 서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."
                    : "AI 서버에 연결할 수 없습니다. 서버 상태를 확인해주세요.";
            return ResponseEntity.ok(Map.of("answer", msg, "source", "error"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("answer", "AI 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", "source", "error"));
        }

        String userId = (String) httpRequest.getAttribute("userId");
        if (userId != null && aiResponse.getBody() != null) {
            String answer = (String) aiResponse.getBody().get("answer");
            if (answer != null) {
                chatHistoryService.save(userId, request.getMessage(), answer);
            }
        }

        return ResponseEntity.ok(aiResponse.getBody());
    }
}
