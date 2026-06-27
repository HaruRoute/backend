package com.chatbot.backend.domain.route.controller;

import com.chatbot.backend.domain.route.dto.TransitFareRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/route")
public class OdsayController {

    @Value("${odsay.api-key}")
    private String odsayApiKey;

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate;
    private final Executor odsayExecutor;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private String encodedApiKey;

    public OdsayController(RestTemplate restTemplate, @Qualifier("odsayExecutor") Executor odsayExecutor) {
        this.restTemplate = restTemplate;
        this.odsayExecutor = odsayExecutor;
    }

    @jakarta.annotation.PostConstruct
    private void init() {
        this.encodedApiKey = java.net.URLEncoder.encode(odsayApiKey, java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/transit-fares")
    public Map<String, Object> calculateFares(@RequestBody TransitFareRequestDto dto) {
        List<TransitFareRequestDto.Coord> coords = dto.getCoords();

        List<Map<String, Object>> emptySegments = new java.util.ArrayList<>();
        Map<String, Object> response = new HashMap<>();

        if (coords == null || coords.size() < 2) {
            response.put("segments", emptySegments);
            return response;
        }

        List<java.util.concurrent.CompletableFuture<Map<String, Object>>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < coords.size() - 1; i++) {
            final int segmentIdx = i;
            TransitFareRequestDto.Coord start = coords.get(i);
            TransitFareRequestDto.Coord end = coords.get(i + 1);

            futures.add(java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                int segTransitFare = 0;
                int segTaxiFare = 0;

                URI uri = UriComponentsBuilder.fromUriString("https://api.odsay.com/v1/api/searchPubTransPathT")
                        .queryParam("apiKey", encodedApiKey)
                        .queryParam("SX", start.getLng())
                        .queryParam("SY", start.getLat())
                        .queryParam("EX", end.getLng())
                        .queryParam("EY", end.getLat())
                        .build(true) // true to indicate that parameters are already encoded
                        .toUri();

                try {
                    String rawResponse = restTemplate.getForObject(uri, String.class);
                    log.debug("ODsay response for segment {}: {}", segmentIdx, rawResponse);

                    Map<String, Object> result = objectMapper.readValue(rawResponse,
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                            });

                    if (result != null && result.containsKey("result")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> resultObj = (Map<String, Object>) result.get("result");
                        if (resultObj.containsKey("path")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> paths = (List<Map<String, Object>>) resultObj.get("path");
                            if (paths != null && !paths.isEmpty()) {
                                Map<String, Object> firstPath = paths.get(0);
                                @SuppressWarnings("unchecked")
                                Map<String, Object> info = (Map<String, Object>) firstPath.get("info");

                                if (info != null) {
                                    if (info.get("payment") != null) {
                                        segTransitFare = Integer.parseInt(info.get("payment").toString());
                                    }
                                    if (info.get("taxiFare") != null) {
                                        segTaxiFare = Integer.parseInt(info.get("taxiFare").toString());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("ODsay API error for segment {}: {}", segmentIdx, e.getMessage());
                }

                try {
                    URI kakaoUri = UriComponentsBuilder.fromUriString("https://apis-navi.kakaomobility.com/v1/directions")
                            .queryParam("origin", start.getLng() + "," + start.getLat())
                            .queryParam("destination", end.getLng() + "," + end.getLat())
                            .build()
                            .toUri();
                    
                    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                    headers.set("Authorization", "KakaoAK " + kakaoApiKey);
                    org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
                    
                    org.springframework.http.ResponseEntity<String> kakaoResponse = restTemplate.exchange(kakaoUri, org.springframework.http.HttpMethod.GET, entity, String.class);
                    
                    Map<String, Object> kakaoResult = objectMapper.readValue(kakaoResponse.getBody(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    
                    if (kakaoResult != null && kakaoResult.containsKey("routes")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> routes = (List<Map<String, Object>>) kakaoResult.get("routes");
                        if (routes != null && !routes.isEmpty()) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> summary = (Map<String, Object>) routes.get(0).get("summary");
                            if (summary != null && summary.containsKey("fare")) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> fare = (Map<String, Object>) summary.get("fare");
                                if (fare != null && fare.containsKey("taxi")) {
                                    segTaxiFare = Integer.parseInt(fare.get("taxi").toString());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Kakao Mobility API error for segment {}: {}", segmentIdx, e.getMessage());
                }

                Map<String, Object> segData = new HashMap<>();
                segData.put("transitFare", segTransitFare);
                segData.put("taxiFare", segTaxiFare);
                return segData;
            }, odsayExecutor));
        }

        // 각 구간에 10초 타임아웃 적용 — 초과 시 0원 fallback 반환
        List<Map<String, Object>> segments = futures.stream()
                .map(f -> {
                    try {
                        return f.orTimeout(10, TimeUnit.SECONDS).join();
                    } catch (Exception e) {
                        log.warn("구간 요금 조회 타임아웃/실패 — fallback 0원 반환: {}", e.getMessage());
                        Map<String, Object> fallback = new HashMap<>();
                        fallback.put("transitFare", 0);
                        fallback.put("taxiFare", 0);
                        return fallback;
                    }
                })
                .collect(java.util.stream.Collectors.toList());

        response.put("segments", segments);
        return response;
    }
}
