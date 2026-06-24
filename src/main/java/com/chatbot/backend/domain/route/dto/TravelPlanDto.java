package com.chatbot.backend.domain.route.dto;

import lombok.*;
import java.time.LocalDateTime;

public class TravelPlanDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SaveRequest {
        private String title;
        private String placesJson;
        private Double totalDistance;
        private Integer transitFare;
        private Integer taxiFare;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Response {
        private Long id;
        private String userId;
        private String title;
        private String placesJson;
        private Double totalDistance;
        private Integer transitFare;
        private Integer taxiFare;
        private LocalDateTime createdAt;
    }
}
