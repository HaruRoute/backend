package com.chatbot.backend.domain.post.dto;

import com.chatbot.backend.domain.post.entity.Post;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class PostDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        @NotBlank private String title;
        @NotBlank private String content;
        private String category;
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
        private String category;
        private String coverImage;
        private LocalDateTime createdAt;

        public static ListResponse from(Post p) {
            return ListResponse.builder()
                    .id(p.getId()).title(p.getTitle()).authorName(p.getAuthorName())
                    .viewCount(p.getViewCount())
                    .hasRoute(p.getRouteData() != null && !p.getRouteData().isBlank())
                    .category(normalizeCategory(p.getCategory()))
                    .coverImage(extractFirstimage(p.getRouteData()))
                    .createdAt(p.getCreatedAt()).build();
        }

        private static String normalizeCategory(String raw) {
            if ("QUESTION".equals(raw)) return "QUESTION";
            return "FREE";
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
        private String category;
        private String routeData;
        private String routeName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static DetailResponse from(Post p) {
            String cat = "QUESTION".equals(p.getCategory()) ? "QUESTION" : "FREE";
            return DetailResponse.builder()
                    .id(p.getId()).title(p.getTitle()).content(p.getContent())
                    .authorId(p.getAuthorId()).authorName(p.getAuthorName())
                    .viewCount(p.getViewCount()).category(cat)
                    .routeData(p.getRouteData()).routeName(p.getRouteName())
                    .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                    .build();
        }
    }

    @Getter @Builder
    public static class PageResponse {
        private List<ListResponse> posts;
        private int totalCount;
        private int page;
        private int size;
        private int totalPages;
    }
}
