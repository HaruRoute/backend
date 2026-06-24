package com.chatbot.backend.domain.route.dto;

import lombok.*;

import java.util.List;

public class RouteLocationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Request {
        private String title;
        private Double lat;
        private Double lng;
        private String addr1;
        private String firstimage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Response {
        private String title;
        private Double lat;
        private Double lng;
        private String addr1;
        private String firstimage;
        private Double distanceFromPrevious; // 이전 지점으로부터의 거리 (km)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class OptimizedRoute {
        private List<Response> route;
        private Double totalDistance; // 총 이동 거리 (km)
    }
}
