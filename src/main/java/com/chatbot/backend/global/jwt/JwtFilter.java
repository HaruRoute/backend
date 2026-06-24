package com.chatbot.backend.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/users/login",
            "/api/users/logout",
            "/api/users/find-pw",
            "/api/spots",
            "/api/route/optimize",
            "/api/route/transit-fares",
            "/api/chatbot/ask"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        // OPTIONS preflight 요청은 무조건 통과
        if (method.equals("OPTIONS")) {
            chain.doFilter(request, response);
            return;
        }

        // 회원가입은 POST /api/users 만 허용
        boolean isPublic = PUBLIC_PATHS.contains(path)
                || (path.equals("/api/users") && method.equals("POST"))
                || (method.equals("GET") && (path.startsWith("/api/notices") || path.startsWith("/api/posts")));

        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "로그인이 필요합니다.");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            sendUnauthorized(response, "유효하지 않은 토큰입니다.");
            return;
        }

        request.setAttribute("userId", jwtUtil.extractUserId(token));
        chain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
