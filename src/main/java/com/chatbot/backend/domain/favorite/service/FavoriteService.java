package com.chatbot.backend.domain.favorite.service;

import com.chatbot.backend.domain.favorite.dto.FavoriteDto;
import com.chatbot.backend.domain.favorite.entity.Favorite;
import com.chatbot.backend.domain.favorite.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    @Transactional
    public FavoriteDto.Response add(String userId, FavoriteDto.AddRequest request) {
        if (favoriteRepository.existsByUserIdAndPlaceName(userId, request.getPlaceName())) {
            throw new IllegalArgumentException("이미 즐겨찾기에 추가된 관광지입니다.");
        }
        Favorite favorite = Favorite.builder()
                .userId(userId)
                .placeName(request.getPlaceName())
                .placeAddress(request.getPlaceAddress())
                .lat(request.getLat())
                .lng(request.getLng())
                .memo(request.getMemo())
                .createdAt(LocalDateTime.now())
                .build();
        favoriteRepository.insert(favorite);
        log.info("즐겨찾기 추가: userId={}, place={}", userId, request.getPlaceName());
        return FavoriteDto.Response.from(favorite);
    }

    public List<FavoriteDto.Response> getList(String userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(FavoriteDto.Response::from)
                .toList();
    }

    @Transactional
    public void delete(String userId, Long favoriteId) {
        Optional.ofNullable(favoriteRepository.findByIdAndUserId(favoriteId, userId))
                .orElseThrow(() -> new NoSuchElementException("즐겨찾기를 찾을 수 없습니다."));
        favoriteRepository.delete(favoriteId);
        log.info("즐겨찾기 삭제: userId={}, favoriteId={}", userId, favoriteId);
    }
}
