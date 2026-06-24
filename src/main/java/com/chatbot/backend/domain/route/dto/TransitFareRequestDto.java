package com.chatbot.backend.domain.route.dto;

import lombok.Data;
import java.util.List;

@Data
public class TransitFareRequestDto {
    private List<Coord> coords;

    @Data
    public static class Coord {
        private double lng;
        private double lat;
    }
}
