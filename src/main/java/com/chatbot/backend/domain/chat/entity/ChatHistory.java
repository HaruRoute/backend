package com.chatbot.backend.domain.chat.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {

    private Long id;
    private String userId;
    private String message;
    private String response;
    private LocalDateTime createdAt;
}
