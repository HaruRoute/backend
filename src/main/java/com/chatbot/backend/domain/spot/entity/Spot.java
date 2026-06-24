package com.chatbot.backend.domain.spot.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Spot {
    private String contentId;
    private String title;
    private String addr1;
    private String addr2;
    private String firstImage;
    private String firstImage2;
    private Double mapx;
    private Double mapy;
    private String areaCode;
    private String sigunguCode;
    private String contentTypeId;
}
