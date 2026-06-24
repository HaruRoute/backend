package com.chatbot.backend.domain.spot.service;

import com.chatbot.backend.domain.spot.dto.SpotDto;
import com.chatbot.backend.domain.spot.entity.Spot;
import com.chatbot.backend.domain.spot.repository.SpotRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotServiceTest {

    @Mock private SpotRepository spotRepository;
    @InjectMocks private SpotService spotService;

    private Spot spot(String title) {
        return Spot.builder()
                .contentId("12345").title(title)
                .addr1("서울 종로구").addr2("")
                .firstImage("img.jpg").firstImage2("img2.jpg")
                .mapx(126.977).mapy(37.579)
                .areaCode("1").sigunguCode("11").contentTypeId("12")
                .build();
    }

    // ==================== getSpots ====================

    @Test
    @DisplayName("관광지 조회 - 정상")
    void getSpots_정상조회() {
        when(spotRepository.findSpots("1", null, null, null, null, null, null, 10, null))
                .thenReturn(List.of(spot("경복궁"), spot("창덕궁")));

        List<SpotDto.Response> result = spotService.getSpots("1", null, null, null, null, null, null, 10, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("경복궁");
        assertThat(result.get(1).getTitle()).isEqualTo("창덕궁");
    }

    @Test
    @DisplayName("관광지 조회 - 빈 결과")
    void getSpots_빈결과() {
        when(spotRepository.findSpots(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        List<SpotDto.Response> result = spotService.getSpots("99", null, null, null, null, null, null, 10, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("관광지 조회 - Spot 엔티티가 SpotDto.Response로 변환됨")
    void getSpots_엔티티_DTO_변환() {
        when(spotRepository.findSpots(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(spot("경복궁")));

        List<SpotDto.Response> result = spotService.getSpots(null, null, null, null, null, null, null, 10, null);

        SpotDto.Response dto = result.get(0);
        assertThat(dto.getContentid()).isEqualTo("12345");
        assertThat(dto.getTitle()).isEqualTo("경복궁");
        assertThat(dto.getAddr1()).isEqualTo("서울 종로구");
        assertThat(dto.getMapx()).isEqualTo("126.977");
        assertThat(dto.getMapy()).isEqualTo("37.579");
        assertThat(dto.getAreacode()).isEqualTo("1");
        assertThat(dto.getSigungucode()).isEqualTo("11");
        assertThat(dto.getContenttypeid()).isEqualTo("12");
    }

    @Test
    @DisplayName("관광지 조회 - mapx/mapy null일 때 null 반환")
    void getSpots_좌표_null처리() {
        Spot noCoord = Spot.builder().contentId("99999").title("좌표없음")
                .mapx(null).mapy(null).build();
        when(spotRepository.findSpots(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(noCoord));

        List<SpotDto.Response> result = spotService.getSpots(null, null, null, null, null, null, null, 10, null);

        assertThat(result.get(0).getMapx()).isNull();
        assertThat(result.get(0).getMapy()).isNull();
    }

    @Test
    @DisplayName("관광지 조회 - 좌표 범위 필터 파라미터가 전달됨")
    void getSpots_좌표범위_파라미터전달() {
        when(spotRepository.findSpots(null, null, null, 126.9, 127.1, 37.5, 37.7, 20, 0))
                .thenReturn(List.of());

        spotService.getSpots(null, null, null, 126.9, 127.1, 37.5, 37.7, 20, 0);

        verify(spotRepository).findSpots(null, null, null, 126.9, 127.1, 37.5, 37.7, 20, 0);
    }

    @Test
    @DisplayName("관광지 조회 - 시군구 코드 필터 파라미터가 전달됨")
    void getSpots_시군구코드_파라미터전달() {
        when(spotRepository.findSpots("1", "11", "12", null, null, null, null, 10, null))
                .thenReturn(List.of(spot("경복궁")));

        List<SpotDto.Response> result = spotService.getSpots("1", "11", "12", null, null, null, null, 10, null);

        verify(spotRepository).findSpots("1", "11", "12", null, null, null, null, 10, null);
        assertThat(result).hasSize(1);
    }
}
