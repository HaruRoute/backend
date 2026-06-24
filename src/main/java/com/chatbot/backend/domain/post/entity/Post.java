package com.chatbot.backend.domain.post.entity;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Post {
    private Long id;
    private String title;
    private String content;
    private String authorId;
    private String authorName;
    private int viewCount;
    private String category;
    private String routeData;
    private String routeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
