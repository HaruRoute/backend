package com.chatbot.backend.domain.user.service;

import com.chatbot.backend.domain.user.dto.UserDto;
import com.chatbot.backend.domain.user.entity.User;
import com.chatbot.backend.domain.user.repository.UserRepository;
import com.chatbot.backend.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void join(UserDto.JoinRequest request) {
        if (userRepository.existsById(request.getId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        User user = User.builder()
                .id(request.getId())
                .pw(passwordEncoder.encode(request.getPw()))
                .name(request.getName())
                .build();
        userRepository.insert(user);
        log.info("회원가입 완료: {}", request.getId());
    }

    public Map<String, String> login(UserDto.LoginRequest request) {
        User user = Optional.ofNullable(userRepository.findById(request.getId()))
                .filter(u -> passwordEncoder.matches(request.getPw(), u.getPw()))
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));
        log.info("로그인 성공: {}", user.getId());
        return Map.of(
                "token", jwtUtil.generateToken(user.getId()),
                "message", "로그인 성공",
                "id", user.getId(),
                "name", user.getName(),
                "role", user.getRole()
        );
    }

    public String findPassword(UserDto.FindPwRequest request) {
        return Optional.ofNullable(userRepository.findByIdAndName(request.getId(), request.getName()))
                .map(User::getPw)
                .orElseThrow(() -> new NoSuchElementException("일치하는 회원 정보가 없습니다."));
    }

    public Map<String, String> getUser(String userId) {
        User user = Optional.ofNullable(userRepository.findById(userId))
                .orElseThrow(() -> new NoSuchElementException("회원 정보를 찾을 수 없습니다."));
        return Map.of("id", user.getId(), "name", user.getName(), "role", user.getRole());
    }

    @Transactional
    public void updateUser(String userId, UserDto.UpdateRequest request) {
        User user = Optional.ofNullable(userRepository.findById(userId))
                .orElseThrow(() -> new NoSuchElementException("회원 정보를 찾을 수 없습니다."));
        user.update(request.getPw(), request.getName());
        userRepository.update(user);
        log.info("회원정보 수정: {}", userId);
    }

    public boolean isAdmin(String userId) {
        User user = userRepository.findById(userId);
        return user != null && "ADMIN".equals(user.getRole());
    }

    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("회원 정보를 찾을 수 없습니다.");
        }
        userRepository.deleteById(userId);
        log.info("회원탈퇴: {}", userId);
    }
}
