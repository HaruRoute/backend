package com.chatbot.backend.domain.route.service;

import com.chatbot.backend.domain.route.dto.TravelPlanDto;
import com.chatbot.backend.domain.route.entity.TravelPlan;
import com.chatbot.backend.domain.route.repository.TravelPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelPlanServiceTest {

    @Mock private TravelPlanRepository travelPlanRepository;
    @InjectMocks private TravelPlanService travelPlanService;

    private TravelPlanDto.SaveRequest saveRequest() {
        return TravelPlanDto.SaveRequest.builder()
                .title("서울 여행").placesJson("[{\"title\":\"경복궁\"}]")
                .totalDistance(5.2).transitFare(1400).taxiFare(8000).build();
    }

    private TravelPlan existingPlan(Long id, String userId) {
        return TravelPlan.builder().id(id).userId(userId).title("서울 여행")
                .placesJson("[{\"title\":\"경복궁\"}]")
                .totalDistance(5.2).transitFare(1400).taxiFare(8000)
                .createdAt(LocalDateTime.now()).build();
    }

    // ==================== savePlan ====================

    @Test
    @DisplayName("여행 계획 저장 - 정상")
    void savePlan_성공() {
        TravelPlanDto.Response result = travelPlanService.savePlan("user1", saveRequest());

        verify(travelPlanRepository).insert(any(TravelPlan.class));
        assertThat(result.getTitle()).isEqualTo("서울 여행");
        assertThat(result.getUserId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("여행 계획 저장 - userId와 데이터가 올바르게 저장됨")
    void savePlan_저장데이터_확인() {
        travelPlanService.savePlan("user1", saveRequest());

        ArgumentCaptor<TravelPlan> captor = ArgumentCaptor.forClass(TravelPlan.class);
        verify(travelPlanRepository).insert(captor.capture());
        TravelPlan saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("user1");
        assertThat(saved.getTitle()).isEqualTo("서울 여행");
        assertThat(saved.getTransitFare()).isEqualTo(1400);
        assertThat(saved.getTaxiFare()).isEqualTo(8000);
    }

    // ==================== getPlans ====================

    @Test
    @DisplayName("여행 계획 목록 조회 - 정상")
    void getPlans_정상조회() {
        when(travelPlanRepository.findByUserId("user1"))
                .thenReturn(List.of(existingPlan(1L, "user1"), existingPlan(2L, "user1")));

        List<TravelPlanDto.Response> result = travelPlanService.getPlans("user1");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("여행 계획 목록 조회 - 빈 목록")
    void getPlans_빈목록() {
        when(travelPlanRepository.findByUserId("user1")).thenReturn(List.of());

        assertThat(travelPlanService.getPlans("user1")).isEmpty();
    }

    // ==================== getPlan ====================

    @Test
    @DisplayName("여행 계획 단건 조회 - 정상")
    void getPlan_성공() {
        when(travelPlanRepository.findById(1L)).thenReturn(existingPlan(1L, "user1"));

        TravelPlanDto.Response result = travelPlanService.getPlan(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("서울 여행");
    }

    @Test
    @DisplayName("여행 계획 단건 조회 - 존재하지 않는 계획 예외")
    void getPlan_없는계획_예외() {
        when(travelPlanRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> travelPlanService.getPlan(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 여행 계획입니다.");
    }

    // ==================== updatePlan ====================

    @Test
    @DisplayName("여행 계획 수정 - 정상")
    void updatePlan_성공() {
        when(travelPlanRepository.findByIdAndUserId(1L, "user1")).thenReturn(existingPlan(1L, "user1"));

        TravelPlanDto.SaveRequest updateReq = TravelPlanDto.SaveRequest.builder()
                .title("수정된 서울 여행").placesJson("[{\"title\":\"남산\"}]")
                .totalDistance(3.0).transitFare(1400).taxiFare(5000).build();

        TravelPlanDto.Response result = travelPlanService.updatePlan("user1", 1L, updateReq);

        verify(travelPlanRepository).update(any(TravelPlan.class));
        assertThat(result.getTitle()).isEqualTo("수정된 서울 여행");
    }

    @Test
    @DisplayName("여행 계획 수정 - 권한 없는 계획 예외")
    void updatePlan_권한없음_예외() {
        when(travelPlanRepository.findByIdAndUserId(1L, "user2")).thenReturn(null);

        assertThatThrownBy(() -> travelPlanService.updatePlan("user2", 1L, saveRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수정할 수 있는 여행 계획이 없습니다.");
        verify(travelPlanRepository, never()).update(any());
    }

    // ==================== deletePlan ====================

    @Test
    @DisplayName("여행 계획 삭제 - 정상")
    void deletePlan_성공() {
        travelPlanService.deletePlan("user1", 1L);

        verify(travelPlanRepository).deleteById(1L, "user1");
    }
}
