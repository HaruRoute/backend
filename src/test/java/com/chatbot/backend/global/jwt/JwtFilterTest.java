package com.chatbot.backend.global.jwt;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtFilterTest {

    private JwtUtil jwtUtil;
    private JwtFilter jwtFilter;

    private static final String SECRET =
            "VGhpcyBpcyBhIHZlcnkgbG9uZyBzZWNyZXQga2V5IHRoYXQgaXMgc2VjdXJlIGVub3VnaCBmb3IgSFMyNTY=";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        jwtFilter = new JwtFilter(jwtUtil);
    }

    private void doFilter(MockHttpServletRequest req, MockHttpServletResponse res, FilterChain chain) {
        try {
            jwtFilter.doFilter(req, res, chain);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== 공개 경로 ====================

    @Test
    @DisplayName("GET /api/posts - 토큰 없이 통과")
    void 공개경로_posts_GET_통과() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/posts");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/notices - 토큰 없이 통과")
    void 공개경로_notices_GET_통과() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/notices");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doFilter(req, res, chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("POST /api/users - 회원가입, 토큰 없이 통과")
    void 공개경로_users_POST_통과() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/users");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doFilter(req, res, chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("POST /api/users/login - 토큰 없이 통과")
    void 공개경로_login_통과() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/users/login");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doFilter(req, res, chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("OPTIONS preflight - 무조건 통과")
    void OPTIONS_preflight_통과() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("OPTIONS", "/api/favorites");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doFilter(req, res, chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    // ==================== 인증 실패 ====================

    @Test
    @DisplayName("GET /api/favorites - Authorization 헤더 없음 → 401")
    void 비공개경로_헤더없음_401() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/favorites");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
        assertThat(res.getContentAsString()).contains("로그인이 필요합니다.");
    }

    @Test
    @DisplayName("POST /api/favorites - Bearer 아닌 헤더 → 401")
    void 비공개경로_Bearer아닌헤더_401() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/favorites");
        req.addHeader("Authorization", "Basic dXNlcjE6cHc=");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    @DisplayName("DELETE /api/favorites/1 - 만료된 토큰 → 401")
    void 비공개경로_만료토큰_401() throws Exception {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String expiredToken = jwtUtil.generateToken("user1");

        MockHttpServletRequest req = new MockHttpServletRequest("DELETE", "/api/favorites/1");
        req.addHeader("Authorization", "Bearer " + expiredToken);
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentAsString()).contains("유효하지 않은 토큰입니다.");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    @DisplayName("GET /api/favorites - 형식이 잘못된 토큰 → 401")
    void 비공개경로_잘못된토큰_401() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/favorites");
        req.addHeader("Authorization", "Bearer invalid.token.value");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    // ==================== 인증 성공 ====================

    @Test
    @DisplayName("GET /api/favorites - 유효한 토큰 → 통과 + userId 속성 설정")
    void 비공개경로_유효한토큰_통과() throws Exception {
        String token = jwtUtil.generateToken("user1");

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/favorites");
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(req.getAttribute("userId")).isEqualTo("user1");
    }

    @Test
    @DisplayName("POST /api/qnas - 유효한 토큰 → userId 속성이 올바르게 설정됨")
    void 비공개경로_유효한토큰_userId설정() throws Exception {
        String token = jwtUtil.generateToken("user42");

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/qnas");
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doFilter(req, res, chain);

        assertThat(req.getAttribute("userId")).isEqualTo("user42");
    }
}
