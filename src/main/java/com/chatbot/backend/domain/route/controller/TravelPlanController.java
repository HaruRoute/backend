package com.chatbot.backend.domain.route.controller;

import com.chatbot.backend.domain.route.dto.TravelPlanDto;
import com.chatbot.backend.domain.route.service.TravelPlanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    @PostMapping
    public ResponseEntity<TravelPlanDto.Response> savePlan(
            @RequestBody TravelPlanDto.SaveRequest request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(travelPlanService.savePlan(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<TravelPlanDto.Response>> getPlans(HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(travelPlanService.getPlans(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPlanDto.Response> getPlan(@PathVariable Long id) {
        return ResponseEntity.ok(travelPlanService.getPlan(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TravelPlanDto.Response> updatePlan(
            @PathVariable Long id,
            @RequestBody TravelPlanDto.SaveRequest request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(travelPlanService.updatePlan(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePlan(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        travelPlanService.deletePlan(userId, id);
        return ResponseEntity.ok(Map.of("message", "여행 계획이 삭제되었습니다."));
    }
}
