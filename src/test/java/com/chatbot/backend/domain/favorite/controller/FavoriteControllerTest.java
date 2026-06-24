package com.chatbot.backend.domain.favorite.controller;

import com.chatbot.backend.domain.favorite.dto.FavoriteDto;
import com.chatbot.backend.domain.favorite.service.FavoriteService;
import com.chatbot.backend.global.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteController.class)
class FavoriteControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private FavoriteService favoriteService;
    @MockitoBean private JwtUtil jwtUtil;

    private static final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken(TOKEN)).thenReturn(true);
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("user1");
    }

    private FavoriteDto.Response favResponse() {
        return FavoriteDto.Response.builder()
                .id(1L).placeName("경복궁").placeAddress("서울 종로구")
                .lat(37.579).lng(126.977).build();
    }

    // ==================== add ====================

    @Test
    @DisplayName("POST /api/favorites - 인증 없이 → 401")
    void add_인증없음_401() throws Exception {
        mockMvc.perform(post("/api/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"placeName\":\"경복궁\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/favorites - 정상 추가 → 200")
    void add_성공_200() throws Exception {
        when(favoriteService.add(anyString(), any())).thenReturn(favResponse());

        mockMvc.perform(post("/api/favorites")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"placeName\":\"경복궁\",\"placeAddress\":\"서울 종로구\",\"lat\":37.579,\"lng\":126.977}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placeName").value("경복궁"))
                .andExpect(jsonPath("$.lat").value(37.579));

        verify(favoriteService).add(eq("user1"), any());
    }

    @Test
    @DisplayName("POST /api/favorites - 빈 장소명 → 400 (validation)")
    void add_빈장소명_400() throws Exception {
        mockMvc.perform(post("/api/favorites")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"placeName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("관광지 이름을 입력해주세요."));
    }

    @Test
    @DisplayName("POST /api/favorites - 중복 장소 → 400")
    void add_중복장소_400() throws Exception {
        when(favoriteService.add(anyString(), any()))
                .thenThrow(new IllegalArgumentException("이미 즐겨찾기에 추가된 관광지입니다."));

        mockMvc.perform(post("/api/favorites")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"placeName\":\"경복궁\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 즐겨찾기에 추가된 관광지입니다."));
    }

    // ==================== getList ====================

    @Test
    @DisplayName("GET /api/favorites - 인증 없이 → 401")
    void getList_인증없음_401() throws Exception {
        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/favorites - 정상 조회 → 200")
    void getList_성공_200() throws Exception {
        when(favoriteService.getList("user1")).thenReturn(List.of(favResponse()));

        mockMvc.perform(get("/api/favorites")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].placeName").value("경복궁"));
    }

    @Test
    @DisplayName("GET /api/favorites - 빈 목록 → 200")
    void getList_빈목록_200() throws Exception {
        when(favoriteService.getList("user1")).thenReturn(List.of());

        mockMvc.perform(get("/api/favorites")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ==================== delete ====================

    @Test
    @DisplayName("DELETE /api/favorites/{id} - 인증 없이 → 401")
    void delete_인증없음_401() throws Exception {
        mockMvc.perform(delete("/api/favorites/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/favorites/{id} - 정상 삭제 → 200")
    void delete_성공_200() throws Exception {
        mockMvc.perform(delete("/api/favorites/1")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("즐겨찾기가 삭제되었습니다."));

        verify(favoriteService).delete("user1", 1L);
    }

    @Test
    @DisplayName("DELETE /api/favorites/{id} - 없는 즐겨찾기 → 404")
    void delete_없는즐겨찾기_404() throws Exception {
        doThrow(new NoSuchElementException("즐겨찾기를 찾을 수 없습니다."))
                .when(favoriteService).delete(anyString(), anyLong());

        mockMvc.perform(delete("/api/favorites/999")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("즐겨찾기를 찾을 수 없습니다."));
    }
}
