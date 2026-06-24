package com.chatbot.backend.domain.route.service;

import com.chatbot.backend.domain.route.dto.RouteLocationDto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RouteServiceTest {

    private final RouteService routeService = new RouteService();

    @Test
    public void testDistanceCalculation() {
        // 서울과 부산 좌표
        double seoulLat = 37.5665;
        double seoulLng = 126.9780;
        double busanLat = 35.1796;
        double busanLng = 129.0756;

        double distance = routeService.calculateDistance(seoulLat, seoulLng, busanLat, busanLng);
        
        // 대략 325km 부근이어야 함
        assertTrue(distance > 300 && distance < 350);
    }

    @Test
    public void testRouteOptimization() {
        List<RouteLocationDto.Request> places = new ArrayList<>();
        // 1. 서울 (출발지)
        places.add(new RouteLocationDto.Request("서울", 37.5665, 126.9780, "서울 주소", null));
        // 2. 부산 (먼 곳)
        places.add(new RouteLocationDto.Request("부산", 35.1796, 129.0756, "부산 주소", null));
        // 3. 대전 (중간 지점)
        places.add(new RouteLocationDto.Request("대전", 36.3504, 127.3845, "대전 주소", null));

        RouteLocationDto.OptimizedRoute optimized = routeService.optimizeRoute(places);

        assertNotNull(optimized);
        assertEquals(3, optimized.getRoute().size());

        // 기대 방문 순서: 서울 -> 대전 -> 부산 (지그재그가 아닌 순차 방문)
        assertEquals("서울", optimized.getRoute().get(0).getTitle());
        assertEquals("대전", optimized.getRoute().get(1).getTitle());
        assertEquals("부산", optimized.getRoute().get(2).getTitle());

        assertTrue(optimized.getTotalDistance() > 300);
    }
}
