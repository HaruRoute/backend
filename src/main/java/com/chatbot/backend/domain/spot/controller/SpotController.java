package com.chatbot.backend.domain.spot.controller;

import com.chatbot.backend.domain.spot.dto.SpotDto;
import com.chatbot.backend.domain.spot.service.SpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spots")
@RequiredArgsConstructor
public class SpotController {

    private final SpotService spotService;

    @GetMapping
    public ResponseEntity<List<SpotDto.Response>> getSpots(
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) String sigunguCode,
            @RequestParam(required = false) String contentTypeId,
            @RequestParam(required = false) Double minX,
            @RequestParam(required = false) Double maxX,
            @RequestParam(required = false) Double minY,
            @RequestParam(required = false) Double maxY,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(spotService.getSpots(areaCode, sigunguCode, contentTypeId, minX, maxX, minY, maxY, limit, offset, keyword));
    }
}
