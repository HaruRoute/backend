package com.chatbot.backend.domain.route.entity;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TravelPlan {
    private Long id;
    private String userId;
    private String title;
    private String placesJson;
    private Double totalDistance;
    private Integer transitFare;
    private Integer taxiFare;
    private LocalDateTime createdAt;
}
