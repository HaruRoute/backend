package com.chatbot.backend.domain.route.service;

import com.chatbot.backend.domain.route.dto.TravelPlanDto;
import com.chatbot.backend.domain.route.entity.TravelPlan;
import com.chatbot.backend.domain.route.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;

    public TravelPlanDto.Response savePlan(String userId, TravelPlanDto.SaveRequest request) {
        TravelPlan plan = TravelPlan.builder()
                .userId(userId)
                .title(request.getTitle())
                .placesJson(request.getPlacesJson())
                .totalDistance(request.getTotalDistance())
                .transitFare(request.getTransitFare())
                .taxiFare(request.getTaxiFare())
                .createdAt(LocalDateTime.now())
                .build();
        travelPlanRepository.insert(plan);

        return toResponse(plan);
    }

    public List<TravelPlanDto.Response> getPlans(String userId) {
        return travelPlanRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TravelPlanDto.Response getPlan(Long id) {
        TravelPlan plan = travelPlanRepository.findById(id);
        if (plan == null) {
            throw new IllegalArgumentException("존재하지 않는 여행 계획입니다.");
        }
        return toResponse(plan);
    }

    public TravelPlanDto.Response updatePlan(String userId, Long id, TravelPlanDto.SaveRequest request) {
        TravelPlan existingPlan = travelPlanRepository.findByIdAndUserId(id, userId);
        if (existingPlan == null) {
            throw new IllegalArgumentException("수정할 수 있는 여행 계획이 없습니다.");
        }

        TravelPlan plan = TravelPlan.builder()
                .id(id)
                .userId(userId)
                .title(request.getTitle())
                .placesJson(request.getPlacesJson())
                .totalDistance(request.getTotalDistance())
                .transitFare(request.getTransitFare())
                .taxiFare(request.getTaxiFare())
                .createdAt(existingPlan.getCreatedAt())
                .build();
        travelPlanRepository.update(plan);

        return toResponse(plan);
    }

    public void deletePlan(String userId, Long id) {
        travelPlanRepository.deleteById(id, userId);
    }

    private TravelPlanDto.Response toResponse(TravelPlan plan) {
        return TravelPlanDto.Response.builder()
                .id(plan.getId())
                .userId(plan.getUserId())
                .title(plan.getTitle())
                .placesJson(plan.getPlacesJson())
                .totalDistance(plan.getTotalDistance())
                .transitFare(plan.getTransitFare())
                .taxiFare(plan.getTaxiFare())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
