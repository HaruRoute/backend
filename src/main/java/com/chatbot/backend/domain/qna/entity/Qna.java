package com.chatbot.backend.domain.qna.entity;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Qna {
    private Long id;
    private String title;
    private String content;
    private String authorId;
    private String authorName;
    private int viewCount;
    private String routeData;
    private String routeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
