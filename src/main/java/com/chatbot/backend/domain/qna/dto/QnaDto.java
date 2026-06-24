package com.chatbot.backend.domain.qna.dto;

import com.chatbot.backend.domain.qna.entity.Qna;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class QnaDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        @NotBlank private String title;
        @NotBlank private String content;
        private String routeData;
        private String routeName;
    }

    @Getter @Builder
    public static class ListResponse {
        private Long id;
        private String title;
        private String authorName;
        private int viewCount;
        private boolean hasRoute;
        private String coverImage;
        private LocalDateTime createdAt;

        public static ListResponse from(Qna q) {
            return ListResponse.builder()
                    .id(q.getId()).title(q.getTitle()).authorName(q.getAuthorName())
                    .viewCount(q.getViewCount())
                    .hasRoute(q.getRouteData() != null && !q.getRouteData().isBlank())
                    .coverImage(extractFirstimage(q.getRouteData()))
                    .createdAt(q.getCreatedAt()).build();
        }

        private static String extractFirstimage(String routeData) {
            if (routeData == null || routeData.isBlank()) return null;
            String key = "\"firstimage\":\"";
            int idx = routeData.indexOf(key);
            if (idx < 0) return null;
            int start = idx + key.length();
            int end = routeData.indexOf('"', start);
            if (end < 0) return null;
            String img = routeData.substring(start, end).trim();
            return img.isEmpty() ? null : img;
        }
    }

    @Getter @Builder
    public static class DetailResponse {
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

        public static DetailResponse from(Qna q) {
            return DetailResponse.builder()
                    .id(q.getId()).title(q.getTitle()).content(q.getContent())
                    .authorId(q.getAuthorId()).authorName(q.getAuthorName())
                    .viewCount(q.getViewCount())
                    .routeData(q.getRouteData()).routeName(q.getRouteName())
                    .createdAt(q.getCreatedAt()).updatedAt(q.getUpdatedAt())
                    .build();
        }
    }

    @Getter @Builder
    public static class PageResponse {
        private List<ListResponse> qnas;
        private int totalCount;
        private int page;
        private int size;
        private int totalPages;
    }
}
