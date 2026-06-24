package com.chatbot.backend.domain.chat.service;

import com.chatbot.backend.domain.chat.entity.ChatHistory;
import com.chatbot.backend.domain.chat.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;

    @Transactional
    public void save(String userId, String message, String response) {
        chatHistoryRepository.insert(ChatHistory.builder()
                .userId(userId)
                .message(message)
                .response(response)
                .createdAt(LocalDateTime.now())
                .build());
    }

    public List<ChatHistory> getHistory(String userId) {
        return chatHistoryRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    @Transactional
    public void clearHistory(String userId) {
        chatHistoryRepository.deleteByUserId(userId);
    }
}
