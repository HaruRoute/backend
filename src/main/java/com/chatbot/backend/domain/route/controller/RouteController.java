package com.chatbot.backend.domain.route.controller;

import com.chatbot.backend.domain.route.dto.RouteLocationDto;
import com.chatbot.backend.domain.route.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping("/optimize")
    public ResponseEntity<RouteLocationDto.OptimizedRoute> optimizeRoute(
            @RequestBody List<RouteLocationDto.Request> requests) {
        return ResponseEntity.ok(routeService.optimizeRoute(requests));
    }
}
