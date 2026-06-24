package com.chatbot.backend.domain.chat.repository;

import com.chatbot.backend.domain.chat.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatHistoryRepository {

    void insert(ChatHistory chatHistory);
    List<ChatHistory> findByUserIdOrderByCreatedAtAsc(String userId);
    void deleteByUserId(String userId);
}
