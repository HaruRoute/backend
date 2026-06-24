package com.chatbot.backend.domain.notice.entity;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Notice {
    private Long id;
    private String title;
    private String content;
    private String authorId;
    private String authorName;
    private boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
