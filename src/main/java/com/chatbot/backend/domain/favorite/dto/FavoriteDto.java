package com.chatbot.backend.domain.favorite.dto;

import com.chatbot.backend.domain.favorite.entity.Favorite;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class FavoriteDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddRequest {
        @NotBlank(message = "관광지 이름을 입력해주세요.")
        private String placeName;
        private String placeAddress;
        private Double lat;
        private Double lng;
        private String memo;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String placeName;
        private String placeAddress;
        private Double lat;
        private Double lng;
        private String memo;
        private LocalDateTime createdAt;

        public static Response from(Favorite f) {
            return Response.builder()
                    .id(f.getId())
                    .placeName(f.getPlaceName())
                    .placeAddress(f.getPlaceAddress())
                    .lat(f.getLat())
                    .lng(f.getLng())
                    .memo(f.getMemo())
                    .createdAt(f.getCreatedAt())
                    .build();
        }
    }
}
