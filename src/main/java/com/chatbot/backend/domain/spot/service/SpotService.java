package com.chatbot.backend.domain.spot.service;

import com.chatbot.backend.domain.spot.dto.SpotDto;
import com.chatbot.backend.domain.spot.entity.Spot;
import com.chatbot.backend.domain.spot.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotService {

    private final SpotRepository spotRepository;

    @Cacheable(value = "spots", key = "#areaCode + '_' + #sigunguCode + '_' + #contentTypeId + '_' + " +
            "(#minX != null ? T(Math).round(#minX * 10000) : 'N') + '_' + " +
            "(#maxX != null ? T(Math).round(#maxX * 10000) : 'N') + '_' + " +
            "(#minY != null ? T(Math).round(#minY * 10000) : 'N') + '_' + " +
            "(#maxY != null ? T(Math).round(#maxY * 10000) : 'N') + '_' + " +
            "#limit + '_' + #offset + '_' + #keyword")
    public List<SpotDto.Response> getSpots(String areaCode, String sigunguCode, String contentTypeId, Double minX, Double maxX, Double minY, Double maxY, Integer limit, Integer offset, String keyword) {
        int effectiveLimit = (limit != null) ? Math.min(limit, 200) : 100;
        List<Spot> spots = spotRepository.findSpots(areaCode, sigunguCode, contentTypeId, minX, maxX, minY, maxY, effectiveLimit, offset, keyword);
        return spots.stream().map(spot -> SpotDto.Response.builder()
                .contentid(spot.getContentId())
                .title(spot.getTitle())
                .addr1(spot.getAddr1())
                .addr2(spot.getAddr2())
                .firstimage(spot.getFirstImage())
                .firstimage2(spot.getFirstImage2())
                .mapx(spot.getMapx() != null ? String.valueOf(spot.getMapx()) : null)
                .mapy(spot.getMapy() != null ? String.valueOf(spot.getMapy()) : null)
                .areacode(spot.getAreaCode())
                .sigungucode(spot.getSigunguCode())
                .contenttypeid(spot.getContentTypeId())
                .build()
        ).collect(Collectors.toList());
    }
}
