package com.chatbot.backend.domain.chatbot.controller;

import com.chatbot.backend.domain.chat.service.ChatHistoryService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @CircuitBreaker(name = "ai-server", fallbackMethod = "fallbackAsk")
    @PostMapping("/ask")
    public ResponseEntity<Map> askChatbot(@Valid @RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        String url = aiServerUrl + "/integrated-chat";
        ResponseEntity<Map> aiResponse = restTemplate.postForEntity(url, request, Map.class);

        String userId = (String) httpRequest.getAttribute("userId");
        if (userId != null && aiResponse.getBody() != null) {
            String answer = (String) aiResponse.getBody().get("answer");
            if (answer != null) {
                chatHistoryService.save(userId, request.getMessage(), answer);
            }
        }

        return ResponseEntity.ok(aiResponse.getBody());
    }

    // Circuit OPEN 상태 또는 예외 발생 시 즉시 반환 (AI 서버 timeout 대기 없음)
    public ResponseEntity<Map> fallbackAsk(ChatRequest request, HttpServletRequest httpRequest, Exception e) {
        String msg = (e.getMessage() != null && e.getMessage().contains("CircuitBreaker"))
                ? "AI 서버가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해주세요."
                : "AI 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.";
        return ResponseEntity.ok(Map.of("answer", msg, "source", "fallback"));
    }
}
