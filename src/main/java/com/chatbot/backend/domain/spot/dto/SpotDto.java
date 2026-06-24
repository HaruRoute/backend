package com.chatbot.backend.domain.spot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SpotDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String contentid;
        private String title;
        private String addr1;
        private String addr2;
        private String firstimage;
        private String firstimage2;
        private String mapx;
        private String mapy;
        private String areacode;
        private String sigungucode;
        private String contenttypeid;
    }
}
