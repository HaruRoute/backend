package com.chatbot.backend.domain.chat.controller;

import com.chatbot.backend.domain.chat.entity.ChatHistory;
import com.chatbot.backend.domain.chat.service.ChatHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    @GetMapping("/history")
    public ResponseEntity<List<ChatHistory>> getHistory(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        return ResponseEntity.ok(chatHistoryService.getHistory(userId));
    }

    @DeleteMapping("/history")
    public ResponseEntity<Map<String, String>> clearHistory(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        chatHistoryService.clearHistory(userId);
        return ResponseEntity.ok(Map.of("message", "채팅 이력이 초기화되었습니다."));
    }
}
