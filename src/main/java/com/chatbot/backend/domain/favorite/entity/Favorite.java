package com.chatbot.backend.domain.favorite.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    private Long id;
    private String userId;
    private String placeName;
    private String placeAddress;
    private Double lat;
    private Double lng;
    private String memo;
    private LocalDateTime createdAt;
}
