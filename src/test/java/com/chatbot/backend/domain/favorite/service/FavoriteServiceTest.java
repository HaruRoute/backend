package com.chatbot.backend.domain.favorite.service;

import com.chatbot.backend.domain.favorite.dto.FavoriteDto;
import com.chatbot.backend.domain.favorite.entity.Favorite;
import com.chatbot.backend.domain.favorite.repository.FavoriteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock private FavoriteRepository favoriteRepository;
    @InjectMocks private FavoriteService favoriteService;

    // ==================== add ====================

    @Test
    @DisplayName("즐겨찾기 추가 - 정상")
    void add_성공() {
        FavoriteDto.AddRequest req = FavoriteDto.AddRequest.builder()
                .placeName("경복궁").placeAddress("서울 종로구").lat(37.579).lng(126.977).build();
        when(favoriteRepository.existsByUserIdAndPlaceName("user1", "경복궁")).thenReturn(false);

        FavoriteDto.Response result = favoriteService.add("user1", req);

        verify(favoriteRepository).insert(any(Favorite.class));
        assertThat(result.getPlaceName()).isEqualTo("경복궁");
        assertThat(result.getLat()).isEqualTo(37.579);
    }

    @Test
    @DisplayName("즐겨찾기 추가 - 중복 장소 예외")
    void add_중복장소_예외() {
        FavoriteDto.AddRequest req = FavoriteDto.AddRequest.builder()
                .placeName("경복궁").build();
        when(favoriteRepository.existsByUserIdAndPlaceName("user1", "경복궁")).thenReturn(true);

        assertThatThrownBy(() -> favoriteService.add("user1", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 즐겨찾기에 추가된 관광지입니다.");
        verify(favoriteRepository, never()).insert(any());
    }

    @Test
    @DisplayName("즐겨찾기 추가 - userId가 올바르게 저장됨")
    void add_userId_저장확인() {
        FavoriteDto.AddRequest req = FavoriteDto.AddRequest.builder()
                .placeName("남산타워").placeAddress("서울 용산구").lat(37.551).lng(126.988).build();
        when(favoriteRepository.existsByUserIdAndPlaceName("user1", "남산타워")).thenReturn(false);

        favoriteService.add("user1", req);

        ArgumentCaptor<Favorite> captor = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteRepository).insert(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("user1");
        assertThat(captor.getValue().getPlaceName()).isEqualTo("남산타워");
    }

    // ==================== getList ====================

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 정상")
    void getList_정상조회() {
        Favorite f1 = Favorite.builder().id(1L).userId("user1").placeName("경복궁")
                .lat(37.579).lng(126.977).createdAt(LocalDateTime.now()).build();
        Favorite f2 = Favorite.builder().id(2L).userId("user1").placeName("남산타워")
                .lat(37.551).lng(126.988).createdAt(LocalDateTime.now()).build();
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc("user1")).thenReturn(List.of(f1, f2));

        List<FavoriteDto.Response> result = favoriteService.getList("user1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPlaceName()).isEqualTo("경복궁");
        assertThat(result.get(1).getPlaceName()).isEqualTo("남산타워");
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 빈 목록")
    void getList_빈목록() {
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc("user1")).thenReturn(List.of());

        List<FavoriteDto.Response> result = favoriteService.getList("user1");

        assertThat(result).isEmpty();
    }

    // ==================== delete ====================

    @Test
    @DisplayName("즐겨찾기 삭제 - 정상")
    void delete_성공() {
        Favorite fav = Favorite.builder().id(1L).userId("user1").placeName("경복궁").build();
        when(favoriteRepository.findByIdAndUserId(1L, "user1")).thenReturn(fav);

        favoriteService.delete("user1", 1L);

        verify(favoriteRepository).delete(1L);
    }

    @Test
    @DisplayName("즐겨찾기 삭제 - 존재하지 않는 즐겨찾기 예외")
    void delete_존재안함_예외() {
        when(favoriteRepository.findByIdAndUserId(99L, "user1")).thenReturn(null);

        assertThatThrownBy(() -> favoriteService.delete("user1", 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("즐겨찾기를 찾을 수 없습니다.");
        verify(favoriteRepository, never()).delete(any());
    }

    @Test
    @DisplayName("즐겨찾기 삭제 - 다른 유저의 즐겨찾기 삭제 불가")
    void delete_다른유저_접근불가() {
        when(favoriteRepository.findByIdAndUserId(1L, "user2")).thenReturn(null);

        assertThatThrownBy(() -> favoriteService.delete("user2", 1L))
                .isInstanceOf(NoSuchElementException.class);
        verify(favoriteRepository, never()).delete(any());
    }
}
