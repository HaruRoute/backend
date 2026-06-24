package com.chatbot.backend.domain.qna.controller;

import com.chatbot.backend.domain.qna.dto.QnaDto;
import com.chatbot.backend.domain.qna.service.QnaService;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QnaController.class)
class QnaControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private QnaService qnaService;
    @MockitoBean private JwtUtil jwtUtil;

    private static final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken(TOKEN)).thenReturn(true);
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("user1");
    }

    private QnaDto.PageResponse emptyPage() {
        return QnaDto.PageResponse.builder()
                .qnas(List.of()).totalCount(0).page(0).size(10).totalPages(0).build();
    }

    private QnaDto.DetailResponse detailResponse() {
        return QnaDto.DetailResponse.builder()
                .id(1L).title("질문 제목").content("질문 내용")
                .authorId("user1").authorName("홍길동").viewCount(1).build();
    }

    // ==================== getList ====================

    @Test
    @DisplayName("GET /api/qnas - 인증 없이 → 401 (QnA는 공개 경로 아님)")
    void getList_인증없음_401() throws Exception {
        mockMvc.perform(get("/api/qnas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/qnas - 인증 있을 때 → 200")
    void getList_성공_200() throws Exception {
        when(qnaService.getList(anyInt(), anyInt(), anyBoolean())).thenReturn(emptyPage());

        mockMvc.perform(get("/api/qnas")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qnas").isArray());
    }

    // ==================== getDetail ====================

    @Test
    @DisplayName("GET /api/qnas/{id} - 인증 없이 → 401")
    void getDetail_인증없음_401() throws Exception {
        mockMvc.perform(get("/api/qnas/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/qnas/{id} - 정상 조회 → 200")
    void getDetail_성공_200() throws Exception {
        when(qnaService.getDetail(1L)).thenReturn(detailResponse());

        mockMvc.perform(get("/api/qnas/1")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("질문 제목"))
                .andExpect(jsonPath("$.authorId").value("user1"));
    }

    @Test
    @DisplayName("GET /api/qnas/{id} - 없는 질문 → 404")
    void getDetail_없는질문_404() throws Exception {
        when(qnaService.getDetail(anyLong()))
                .thenThrow(new NoSuchElementException("질문 게시글을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/qnas/999")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("질문 게시글을 찾을 수 없습니다."));
    }

    // ==================== create ====================

    @Test
    @DisplayName("POST /api/qnas - 인증 없이 → 401")
    void create_인증없음_401() throws Exception {
        mockMvc.perform(post("/api/qnas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"질문\",\"content\":\"내용\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/qnas - 성공 → 200")
    void create_성공_200() throws Exception {
        when(qnaService.create(anyString(), any())).thenReturn(detailResponse());

        mockMvc.perform(post("/api/qnas")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"질문 제목\",\"content\":\"질문 내용\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("질문 제목"));

        verify(qnaService).create(eq("user1"), any());
    }

    @Test
    @DisplayName("POST /api/qnas - 빈 제목 → 400 (validation)")
    void create_빈제목_400() throws Exception {
        mockMvc.perform(post("/api/qnas")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"content\":\"내용\"}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== update ====================

    @Test
    @DisplayName("PUT /api/qnas/{id} - 인증 없이 → 401")
    void update_인증없음_401() throws Exception {
        mockMvc.perform(put("/api/qnas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"수정\",\"content\":\"내용\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/qnas/{id} - 성공 → 200")
    void update_성공_200() throws Exception {
        when(qnaService.update(anyString(), anyLong(), any())).thenReturn(detailResponse());

        mockMvc.perform(put("/api/qnas/1")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"질문 제목\",\"content\":\"질문 내용\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/qnas/{id} - 권한 없음 → 403")
    void update_권한없음_403() throws Exception {
        when(qnaService.update(anyString(), anyLong(), any()))
                .thenThrow(new IllegalStateException("권한이 없습니다."));

        mockMvc.perform(put("/api/qnas/1")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"질문\",\"content\":\"내용\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    // ==================== delete ====================

    @Test
    @DisplayName("DELETE /api/qnas/{id} - 인증 없이 → 401")
    void delete_인증없음_401() throws Exception {
        mockMvc.perform(delete("/api/qnas/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/qnas/{id} - 성공 → 204")
    void delete_성공_204() throws Exception {
        mockMvc.perform(delete("/api/qnas/1")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());

        verify(qnaService).delete("user1", 1L);
    }

    @Test
    @DisplayName("DELETE /api/qnas/{id} - 없는 질문 → 404")
    void delete_없는질문_404() throws Exception {
        doThrow(new NoSuchElementException("질문 게시글을 찾을 수 없습니다."))
                .when(qnaService).delete(anyString(), anyLong());

        mockMvc.perform(delete("/api/qnas/999")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }
}
