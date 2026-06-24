package com.chatbot.backend.domain.notice.controller;

import com.chatbot.backend.domain.notice.dto.NoticeDto;
import com.chatbot.backend.domain.notice.service.NoticeService;
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

@WebMvcTest(NoticeController.class)
class NoticeControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private NoticeService noticeService;
    @MockitoBean private JwtUtil jwtUtil;

    private static final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken(TOKEN)).thenReturn(true);
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("admin");
    }

    private NoticeDto.Response noticeResponse() {
        return NoticeDto.Response.builder()
                .id(1L).title("공지 제목").content("공지 내용")
                .authorId("admin").authorName("관리자").isPinned(false).build();
    }

    // ==================== getAll ====================

    @Test
    @DisplayName("GET /api/notices - 공개 엔드포인트, 인증 없이 → 200")
    void getAll_인증없이_200() throws Exception {
        when(noticeService.getAll()).thenReturn(List.of(noticeResponse()));

        mockMvc.perform(get("/api/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("공지 제목"));
    }

    @Test
    @DisplayName("GET /api/notices - 빈 목록 → 200")
    void getAll_빈목록_200() throws Exception {
        when(noticeService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ==================== getPinned ====================

    @Test
    @DisplayName("GET /api/notices/pinned - 공개 엔드포인트, 인증 없이 → 200")
    void getPinned_인증없이_200() throws Exception {
        when(noticeService.getPinned()).thenReturn(List.of());

        mockMvc.perform(get("/api/notices/pinned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ==================== create ====================

    @Test
    @DisplayName("POST /api/notices - 인증 없이 → 401")
    void create_인증없음_401() throws Exception {
        mockMvc.perform(post("/api/notices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"공지\",\"content\":\"내용\",\"isPinned\":false}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/notices - 관리자 성공 → 200")
    void create_관리자_성공_200() throws Exception {
        when(noticeService.create(anyString(), any())).thenReturn(noticeResponse());

        mockMvc.perform(post("/api/notices")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"공지 제목\",\"content\":\"공지 내용\",\"isPinned\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("공지 제목"));

        verify(noticeService).create(eq("admin"), any());
    }

    @Test
    @DisplayName("POST /api/notices - 빈 제목 → 400 (validation)")
    void create_빈제목_400() throws Exception {
        mockMvc.perform(post("/api/notices")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"content\":\"내용\",\"isPinned\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/notices - 관리자 아닌 사용자 → 403")
    void create_일반사용자_403() throws Exception {
        when(noticeService.create(anyString(), any()))
                .thenThrow(new IllegalStateException("관리자만 접근 가능합니다."));

        mockMvc.perform(post("/api/notices")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"공지\",\"content\":\"내용\",\"isPinned\":false}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("관리자만 접근 가능합니다."));
    }

    // ==================== update ====================

    @Test
    @DisplayName("PUT /api/notices/{id} - 인증 없이 → 401")
    void update_인증없음_401() throws Exception {
        mockMvc.perform(put("/api/notices/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"수정\",\"content\":\"수정내용\",\"isPinned\":false}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/notices/{id} - 성공 → 200")
    void update_성공_200() throws Exception {
        when(noticeService.update(anyString(), anyLong(), any())).thenReturn(noticeResponse());

        mockMvc.perform(put("/api/notices/1")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"공지 제목\",\"content\":\"공지 내용\",\"isPinned\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/notices/{id} - 없는 공지 → 404")
    void update_없는공지_404() throws Exception {
        when(noticeService.update(anyString(), anyLong(), any()))
                .thenThrow(new NoSuchElementException("공지사항을 찾을 수 없습니다."));

        mockMvc.perform(put("/api/notices/999")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"공지\",\"content\":\"내용\",\"isPinned\":false}"))
                .andExpect(status().isNotFound());
    }

    // ==================== delete ====================

    @Test
    @DisplayName("DELETE /api/notices/{id} - 인증 없이 → 401")
    void delete_인증없음_401() throws Exception {
        mockMvc.perform(delete("/api/notices/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/notices/{id} - 성공 → 204")
    void delete_성공_204() throws Exception {
        mockMvc.perform(delete("/api/notices/1")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());

        verify(noticeService).delete("admin", 1L);
    }
}
