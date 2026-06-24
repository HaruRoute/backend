package com.chatbot.backend.domain.post.controller;

import com.chatbot.backend.domain.post.dto.PostDto;
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

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PostService postService;
    @MockitoBean private JwtUtil jwtUtil;

    private static final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken(TOKEN)).thenReturn(true);
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("user1");
    }

    private PostDto.PageResponse emptyPage() {
        return PostDto.PageResponse.builder()
                .posts(List.of()).totalCount(0).page(0).size(10).totalPages(0).build();
    }

    private PostDto.DetailResponse detailResponse() {
        return PostDto.DetailResponse.builder()
                .id(1L).title("제목").content("내용")
                .authorId("user1").authorName("홍길동")
                .viewCount(1).category("FREE").build();
    }

    // ==================== getList ====================

    @Test
    @DisplayName("GET /api/posts - 공개 엔드포인트, 인증 없이 → 200")
    void getList_인증없이_200() throws Exception {
        when(postService.getList(anyInt(), anyInt(), any(), anyBoolean())).thenReturn(emptyPage());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray());
    }

    @Test
    @DisplayName("GET /api/posts?category=FREE - 카테고리 필터 → 200")
    void getList_카테고리필터_200() throws Exception {
        when(postService.getList(0, 10, "FREE", false))
                .thenReturn(emptyPage());

        mockMvc.perform(get("/api/posts").param("category", "FREE"))
                .andExpect(status().isOk());

        verify(postService).getList(0, 10, "FREE", false);
    }

    // ==================== getDetail ====================

    @Test
    @DisplayName("GET /api/posts/{id} - 공개 엔드포인트, 인증 없이 → 200")
    void getDetail_인증없이_200() throws Exception {
        when(postService.getDetail(1L)).thenReturn(detailResponse());

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.authorId").value("user1"));
    }

    @Test
    @DisplayName("GET /api/posts/{id} - 없는 게시글 → 404")
    void getDetail_없는게시글_404() throws Exception {
        when(postService.getDetail(anyLong()))
                .thenThrow(new NoSuchElementException("게시글을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."));
    }

    // ==================== create ====================

    @Test
    @DisplayName("POST /api/posts - 인증 없이 → 401")
    void create_인증없음_401() throws Exception {
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"제목\",\"content\":\"내용\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/posts - 인증 있을 때 성공 → 200")
    void create_성공_200() throws Exception {
        when(postService.create(anyString(), any())).thenReturn(detailResponse());

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"제목\",\"content\":\"내용\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("제목"));

        verify(postService).create(eq("user1"), any());
    }

    @Test
    @DisplayName("POST /api/posts - 빈 제목 → 400 (validation)")
    void create_빈제목_400() throws Exception {
        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"content\":\"내용\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/posts - 빈 내용 → 400 (validation)")
    void create_빈내용_400() throws Exception {
        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"제목\",\"content\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== update ====================

    @Test
    @DisplayName("PUT /api/posts/{id} - 인증 없이 → 401")
    void update_인증없음_401() throws Exception {
        mockMvc.perform(put("/api/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"수정\",\"content\":\"수정내용\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/posts/{id} - 인증 있을 때 성공 → 200")
    void update_성공_200() throws Exception {
        when(postService.update(anyString(), anyLong(), any())).thenReturn(detailResponse());

        mockMvc.perform(put("/api/posts/1")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"수정\",\"content\":\"수정내용\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/posts/{id} - 권한 없음 → 403")
    void update_권한없음_403() throws Exception {
        when(postService.update(anyString(), anyLong(), any()))
                .thenThrow(new IllegalStateException("권한이 없습니다."));

        mockMvc.perform(put("/api/posts/1")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"수정\",\"content\":\"내용\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    // ==================== delete ====================

    @Test
    @DisplayName("DELETE /api/posts/{id} - 인증 없이 → 401")
    void delete_인증없음_401() throws Exception {
        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/posts/{id} - 인증 있을 때 성공 → 204")
    void delete_성공_204() throws Exception {
        mockMvc.perform(delete("/api/posts/1")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());

        verify(postService).delete("user1", 1L);
    }

    @Test
    @DisplayName("DELETE /api/posts/{id} - 권한 없음 → 403")
    void delete_권한없음_403() throws Exception {
        doThrow(new IllegalStateException("권한이 없습니다."))
                .when(postService).delete(anyString(), anyLong());

        mockMvc.perform(delete("/api/posts/1")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/posts/{id} - 없는 게시글 → 404")
    void delete_없는게시글_404() throws Exception {
        doThrow(new NoSuchElementException("게시글을 찾을 수 없습니다."))
                .when(postService).delete(anyString(), anyLong());

        mockMvc.perform(delete("/api/posts/999")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }
}
