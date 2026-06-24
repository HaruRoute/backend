package com.chatbot.backend.domain.notice.dto;

import com.chatbot.backend.domain.notice.entity.Notice;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

public class NoticeDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        @NotBlank private String title;
        @NotBlank private String content;
        private boolean isPinned;
    }

    @Getter @Builder
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String authorId;
        private String authorName;
        @JsonProperty("isPinned")
        private boolean isPinned;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(Notice n) {
            return Response.builder()
                    .id(n.getId()).title(n.getTitle()).content(n.getContent())
                    .authorId(n.getAuthorId()).authorName(n.getAuthorName())
                    .isPinned(n.isPinned()).createdAt(n.getCreatedAt()).updatedAt(n.getUpdatedAt())
                    .build();
        }
    }
}
