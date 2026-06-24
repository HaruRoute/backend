package com.chatbot.backend.global.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET =
            "VGhpcyBpcyBhIHZlcnkgbG9uZyBzZWNyZXQga2V5IHRoYXQgaXMgc2VjdXJlIGVub3VnaCBmb3IgSFMyNTY=";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24시간
    }

    @Test
    @DisplayName("토큰 생성 - 정상")
    void generateToken_정상생성() {
        String token = jwtUtil.generateToken("user1");
        assertThat(token).isNotNull().isNotBlank();
        // JWT 형식: header.payload.signature
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("userId 추출 - 정상")
    void extractUserId_정상추출() {
        String token = jwtUtil.generateToken("user1");
        String userId = jwtUtil.extractUserId(token);
        assertThat(userId).isEqualTo("user1");
    }

    @Test
    @DisplayName("토큰 검증 - 유효한 토큰")
    void validateToken_유효한토큰() {
        String token = jwtUtil.generateToken("user1");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("토큰 검증 - 형식이 잘못된 토큰")
    void validateToken_잘못된형식_토큰() {
        assertThat(jwtUtil.validateToken("invalid.token.string")).isFalse();
    }

    @Test
    @DisplayName("토큰 검증 - 빈 문자열")
    void validateToken_빈문자열() {
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("토큰 검증 - 만료된 토큰")
    void validateToken_만료된토큰() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken("user1");
        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("서로 다른 userId는 다른 토큰 생성")
    void generateToken_다른유저_다른토큰() {
        String token1 = jwtUtil.generateToken("user1");
        String token2 = jwtUtil.generateToken("user2");
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtil.extractUserId(token1)).isEqualTo("user1");
        assertThat(jwtUtil.extractUserId(token2)).isEqualTo("user2");
    }
}
