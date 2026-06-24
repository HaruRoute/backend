package com.chatbot.backend.domain.favorite.controller;

import com.chatbot.backend.domain.favorite.dto.FavoriteDto;
import com.chatbot.backend.domain.favorite.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public ResponseEntity<FavoriteDto.Response> add(
            @Valid @RequestBody FavoriteDto.AddRequest request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(favoriteService.add(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<FavoriteDto.Response>> getList(HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(favoriteService.getList(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        favoriteService.delete(userId, id);
        return ResponseEntity.ok(Map.of("message", "즐겨찾기가 삭제되었습니다."));
    }
}
