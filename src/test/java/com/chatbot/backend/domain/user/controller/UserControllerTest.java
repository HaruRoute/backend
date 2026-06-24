package com.chatbot.backend.domain.user.controller;

import com.chatbot.backend.domain.user.service.UserService;
import com.chatbot.backend.global.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private JwtUtil jwtUtil;

    private static final String TOKEN = "test-token";
    private static final String USER_ID = "user1";

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken(TOKEN)).thenReturn(true);
        when(jwtUtil.extractUserId(TOKEN)).thenReturn(USER_ID);
    }

    // ==================== join ====================

    @Test
    @DisplayName("POST /api/users - 회원가입 성공 → 200")
    void join_성공() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"user1\",\"pw\":\"pw1234\",\"name\":\"홍길동\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("가입 성공"));

        verify(userService).join(any());
    }

    @Test
    @DisplayName("POST /api/users - 빈 아이디 → 400")
    void join_빈아이디_400() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"\",\"pw\":\"pw1234\",\"name\":\"홍길동\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/users - 빈 이름 → 400")
    void join_빈이름_400() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"user1\",\"pw\":\"pw1234\",\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users - 중복 아이디 → 400")
    void join_중복아이디_400() throws Exception {
        doThrow(new IllegalArgumentException("이미 존재하는 아이디입니다."))
                .when(userService).join(any());

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"user1\",\"pw\":\"pw1234\",\"name\":\"홍길동\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 존재하는 아이디입니다."));
    }

    // ==================== login ====================

    @Test
    @DisplayName("POST /api/users/login - 로그인 성공 → 200")
    void login_성공() throws Exception {
        when(userService.login(any())).thenReturn(
                Map.of("token", "jwt-token", "id", "user1", "name", "홍길동", "role", "USER", "message", "로그인 성공"));

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"user1\",\"pw\":\"pw1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.id").value("user1"));
    }

    @Test
    @DisplayName("POST /api/users/login - 비밀번호 불일치 → 400")
    void login_비밀번호불일치_400() throws Exception {
        when(userService.login(any()))
                .thenThrow(new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"user1\",\"pw\":\"wrong\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    // ==================== findPw ====================

    @Test
    @DisplayName("POST /api/users/find-pw - 정상 → 200")
    void findPw_성공() throws Exception {
        when(userService.findPassword(any())).thenReturn("encoded_pw");

        mockMvc.perform(post("/api/users/find-pw")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"user1\",\"name\":\"홍길동\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value("encoded_pw"));
    }

    @Test
    @DisplayName("POST /api/users/find-pw - 없는 회원 → 404")
    void findPw_없는회원_404() throws Exception {
        when(userService.findPassword(any()))
                .thenThrow(new NoSuchElementException("일치하는 회원 정보가 없습니다."));

        mockMvc.perform(post("/api/users/find-pw")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"ghost\",\"name\":\"없는사람\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("일치하는 회원 정보가 없습니다."));
    }

    // ==================== getUser ====================

    @Test
    @DisplayName("GET /api/users/{userId} - 인증 없이 → 401")
    void getUser_인증없음_401() throws Exception {
        mockMvc.perform(get("/api/users/user1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/{userId} - 인증 있을 때 → 200")
    void getUser_성공_200() throws Exception {
        when(userService.getUser("user1"))
                .thenReturn(Map.of("id", "user1", "name", "홍길동", "role", "USER"));

        mockMvc.perform(get("/api/users/user1")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user1"))
                .andExpect(jsonPath("$.name").value("홍길동"));
    }

    @Test
    @DisplayName("GET /api/users/{userId} - 없는 회원 → 404")
    void getUser_없는회원_404() throws Exception {
        when(userService.getUser(anyString()))
                .thenThrow(new NoSuchElementException("회원 정보를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/users/ghost")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }

    // ==================== updateUser ====================

    @Test
    @DisplayName("PUT /api/users/{userId} - 인증 없이 → 401")
    void updateUser_인증없음_401() throws Exception {
        mockMvc.perform(put("/api/users/user1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"새이름\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/users/{userId} - 정상 수정 → 200")
    void updateUser_성공_200() throws Exception {
        mockMvc.perform(put("/api/users/user1")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"새이름\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("수정 성공"));

        verify(userService).updateUser(eq("user1"), any());
    }

    // ==================== deleteUser ====================

    @Test
    @DisplayName("DELETE /api/users/{userId} - 인증 없이 → 401")
    void deleteUser_인증없음_401() throws Exception {
        mockMvc.perform(delete("/api/users/user1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/users/{userId} - 정상 탈퇴 → 200")
    void deleteUser_성공_200() throws Exception {
        mockMvc.perform(delete("/api/users/user1")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("탈퇴 성공"));

        verify(userService).deleteUser("user1");
    }

    @Test
    @DisplayName("DELETE /api/users/{userId} - 없는 회원 → 404")
    void deleteUser_없는회원_404() throws Exception {
        doThrow(new NoSuchElementException("회원 정보를 찾을 수 없습니다."))
                .when(userService).deleteUser(anyString());

        mockMvc.perform(delete("/api/users/ghost")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }
}
