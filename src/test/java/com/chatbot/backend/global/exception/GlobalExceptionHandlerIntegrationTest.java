package com.chatbot.backend.global.exception;

import com.chatbot.backend.domain.post.controller.PostController;
import com.chatbot.backend.domain.post.service.PostService;
import com.chatbot.backend.global.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GlobalExceptionHandler가 실제 HTTP 레이어에서 올바르게 동작하는지 검증.
 * PostController를 진입점으로 활용하여 실제 요청-응답 흐름을 테스트.
 */
@WebMvcTest(PostController.class)
class GlobalExceptionHandlerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PostService postService;
    @MockitoBean private JwtUtil jwtUtil;

    private static final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken(TOKEN)).thenReturn(true);
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("user1");
    }

    @Test
    @DisplayName("잘못된 JSON 바디 → 400 (HttpMessageNotReadableException)")
    void 잘못된JSON_400() throws Exception {
        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 본문을 읽을 수 없습니다."));
    }

    @Test
    @DisplayName("경로 변수 타입 불일치 /api/posts/abc → 400 (MethodArgumentTypeMismatchException)")
    void 경로변수타입불일치_400() throws Exception {
        mockMvc.perform(get("/api/posts/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("id")));
    }

    @Test
    @DisplayName("게시글 없음 → 404 (NoSuchElementException)")
    void 게시글없음_404() throws Exception {
        when(postService.getDetail(anyLong()))
                .thenThrow(new NoSuchElementException("게시글을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("권한 없음 → 403 (IllegalStateException)")
    void 권한없음_403() throws Exception {
        when(postService.update(anyString(), anyLong(), any()))
                .thenThrow(new IllegalStateException("권한이 없습니다."));

        mockMvc.perform(put("/api/posts/1")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"수정\",\"content\":\"내용\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드 → 405 (HttpRequestMethodNotSupportedException)")
    void 지원안하는메서드_405() throws Exception {
        mockMvc.perform(patch("/api/posts/1")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("PATCH")));
    }

    @Test
    @DisplayName("@Valid 빈 제목 → 400 (MethodArgumentNotValidException)")
    void valid_빈제목_400() throws Exception {
        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"content\":\"내용\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("모든 오류 응답이 'message' 키를 포함함")
    void 오류응답_message키_포함() throws Exception {
        // 404 케이스
        when(postService.getDetail(anyLong()))
                .thenThrow(new NoSuchElementException("없음"));
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(jsonPath("$.message").exists());

        // 400 케이스
        mockMvc.perform(get("/api/posts/abc"))
                .andExpect(jsonPath("$.message").exists());
    }
}
