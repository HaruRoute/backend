package com.chatbot.backend.global.batch;

import com.chatbot.backend.domain.spot.entity.Spot;
import com.chatbot.backend.domain.spot.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpotSyncScheduler {

    private final SpotRepository spotRepository;
    private final RestTemplate restTemplate;

    @Value("${kto.service-key}")
    private String serviceKey;

    private static final String API_URL = "https://apis.data.go.kr/B551011/KorService2/areaBasedList2";

    // 서버 시작 시 DB가 비어있으면 최초 1회 즉시 실행
    @EventListener(ApplicationReadyEvent.class)
    public void initSyncJob() {
        if (spotRepository.countAll() == 0) {
            log.info("DB에 관광지 데이터가 없습니다. 초기 동기화를 즉시 시작합니다...");
            runSyncJob();
        }
    }

    // 매일 새벽 3시 실행
    @Scheduled(cron = "0 0 3 * * *")
    @SuppressWarnings("unchecked")
    public void runSyncJob() {
        log.info("관광지 데이터 검증 및 동기화 작업 시작...");
        try {
            // 1. 공공 API에서 전체 totalCount 가져오기
            String checkUrl = API_URL + "?serviceKey=" + serviceKey + "&numOfRows=1&pageNo=1&MobileOS=ETC&MobileApp=Test&_type=json";
            URI checkUri = URI.create(checkUrl);

            Map<String, Object> response = restTemplate.getForObject(checkUri, Map.class);
            if (response == null || !response.containsKey("response")) {
                log.error("공공 API 호출 응답 바디가 누락되었습니다.");
                return;
            }

            Map<String, Object> responseWrapper = (Map<String, Object>) response.get("response");
            Map<String, Object> bodyNode = (Map<String, Object>) responseWrapper.get("body");
            if (bodyNode == null) {
                log.error("공공 API 호출 응답 body가 누락되었습니다.");
                return;
            }

            int apiTotalCount = ((Number) bodyNode.get("totalCount")).intValue();
            int localCount = spotRepository.countAll();

            log.info("로컬 DB 데이터 개수: {}개 | 공공 API 전체 개수: {}개", localCount, apiTotalCount);

            if (localCount == apiTotalCount) {
                log.info("로컬 DB와 공공 API의 데이터 개수가 정확히 일치합니다. 검증을 종료합니다.");
                return;
            }

            log.warn("데이터 불일치 감지! 동기화를 실행합니다. (API: {}개, 로컬: {}개)", apiTotalCount, localCount);

            // 2. 전체 페이지를 동적 수집하여 DB에 적재 (Upsert)
            int page = 1;
            int numRows = 1000;
            int syncCount = 0;

            while (true) {
                String fetchUrl = API_URL + "?serviceKey=" + serviceKey + "&numOfRows=" + numRows + "&pageNo=" + page + "&MobileOS=ETC&MobileApp=Test&_type=json";
                URI fetchUri = URI.create(fetchUrl);

                Map<String, Object> pageResponse = restTemplate.getForObject(fetchUri, Map.class);
                if (pageResponse == null) break;

                Map<String, Object> pRespWrapper = (Map<String, Object>) pageResponse.get("response");
                if (pRespWrapper == null) break;
                
                Map<String, Object> pBody = (Map<String, Object>) pRespWrapper.get("body");
                if (pBody == null) break;

                Map<String, Object> pItemsWrapper = (Map<String, Object>) pBody.get("items");
                if (pItemsWrapper == null || pItemsWrapper.get("item") == null) {
                    log.info("페이지 {}에 동기화할 데이터가 없습니다. 동기화를 종료합니다.", page);
                    break;
                }

                Object itemObj = pItemsWrapper.get("item");
                List<Map<String, Object>> items;
                if (itemObj instanceof List) {
                    items = (List<Map<String, Object>>) itemObj;
                } else if (itemObj instanceof Map) {
                    items = List.of((Map<String, Object>) itemObj);
                } else {
                    break;
                }

                if (items.isEmpty()) {
                    log.info("페이지 {}에 동기화할 데이터가 없습니다. 동기화를 종료합니다.", page);
                    break;
                }

                List<Spot> spotList = new java.util.ArrayList<>();

                for (Map<String, Object> item : items) {
                    String contentId = item.get("contentid") != null ? String.valueOf(item.get("contentid")) : "";
                    String title = item.get("title") != null ? String.valueOf(item.get("title")) : "";
                    if (contentId.isEmpty() || title.isEmpty()) {
                        continue;
                    }

                    String addr1 = item.get("addr1") != null ? String.valueOf(item.get("addr1")) : null;
                    String addr2 = item.get("addr2") != null ? String.valueOf(item.get("addr2")) : null;
                    String firstImage = item.get("firstimage") != null ? String.valueOf(item.get("firstimage")) : null;
                    String firstImage2 = item.get("firstimage2") != null ? String.valueOf(item.get("firstimage2")) : null;
                    
                    Double mapx = null;
                    if (item.get("mapx") != null) {
                        try {
                            mapx = Double.parseDouble(String.valueOf(item.get("mapx")));
                        } catch (Exception ignored) {}
                    }
                    Double mapy = null;
                    if (item.get("mapy") != null) {
                        try {
                            mapy = Double.parseDouble(String.valueOf(item.get("mapy")));
                        } catch (Exception ignored) {}
                    }

                    String areaCode = item.get("areacode") != null ? String.valueOf(item.get("areacode")) : null;
                    String sigunguCode = item.get("sigungucode") != null ? String.valueOf(item.get("sigungucode")) : null;
                    String contentTypeId = item.get("contenttypeid") != null ? String.valueOf(item.get("contenttypeid")) : null;

                    Spot spot = Spot.builder()
                            .contentId(contentId)
                            .title(title)
                            .addr1(addr1)
                            .addr2(addr2)
                            .firstImage(firstImage)
                            .firstImage2(firstImage2)
                            .mapx(mapx)
                            .mapy(mapy)
                            .areaCode(areaCode)
                            .sigunguCode(sigunguCode)
                            .contentTypeId(contentTypeId)
                            .build();

                    spotList.add(spot);
                    syncCount++;
                }

                if (!spotList.isEmpty()) {
                    spotRepository.upsertBatch(spotList);
                }

                log.info("페이지 {} 동기화 완료 (누적 처리: {})", page, syncCount);
                if (items.size() < numRows) {
                    break;
                }
                page++;
                Thread.sleep(100); // API 과부하 방지
            }

            log.info("데이터 동기화 완료! 총 {}개 항목 검증 및 갱신되었습니다.", syncCount);

        } catch (Exception e) {
            log.error("데이터 동기화 중 에러 발생", e);
        }
    }
}
