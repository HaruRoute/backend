package com.chatbot.backend.domain.user.service;

import com.chatbot.backend.domain.user.dto.UserDto;
import com.chatbot.backend.domain.user.entity.User;
import com.chatbot.backend.domain.user.repository.UserRepository;
import com.chatbot.backend.global.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    // ==================== join ====================

    @Test
    @DisplayName("회원가입 - 정상")
    void join_성공() {
        UserDto.JoinRequest req = UserDto.JoinRequest.builder()
                .id("user1").pw("pw1234").name("홍길동").build();
        when(userRepository.existsById("user1")).thenReturn(false);
        when(passwordEncoder.encode("pw1234")).thenReturn("$2a$encoded");

        assertDoesNotThrow(() -> userService.join(req));
        verify(userRepository).insert(any(User.class));
    }

    @Test
    @DisplayName("회원가입 - 중복 아이디 예외")
    void join_중복아이디_예외() {
        UserDto.JoinRequest req = UserDto.JoinRequest.builder()
                .id("user1").pw("pw1234").name("홍길동").build();
        when(userRepository.existsById("user1")).thenReturn(true);

        assertThatThrownBy(() -> userService.join(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 아이디입니다.");
        verify(userRepository, never()).insert(any());
    }

    // ==================== login ====================

    @Test
    @DisplayName("로그인 - 정상")
    void login_성공() {
        UserDto.LoginRequest req = UserDto.LoginRequest.builder()
                .id("user1").pw("pw1234").build();
        User user = User.builder().id("user1").pw("$2a$encoded").name("홍길동").build();

        when(userRepository.findById("user1")).thenReturn(user);
        when(passwordEncoder.matches("pw1234", "$2a$encoded")).thenReturn(true);
        when(jwtUtil.generateToken("user1")).thenReturn("jwt-token");

        Map<String, String> result = userService.login(req);

        assertThat(result.get("token")).isEqualTo("jwt-token");
        assertThat(result.get("id")).isEqualTo("user1");
        assertThat(result.get("name")).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("로그인 - 존재하지 않는 아이디 예외")
    void login_없는아이디_예외() {
        UserDto.LoginRequest req = UserDto.LoginRequest.builder()
                .id("unknown").pw("pw1234").build();
        when(userRepository.findById("unknown")).thenReturn(null);

        assertThatThrownBy(() -> userService.login(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("로그인 - 비밀번호 불일치 예외")
    void login_비밀번호불일치_예외() {
        UserDto.LoginRequest req = UserDto.LoginRequest.builder()
                .id("user1").pw("wrong").build();
        User user = User.builder().id("user1").pw("$2a$encoded").name("홍길동").build();

        when(userRepository.findById("user1")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "$2a$encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 또는 비밀번호가 일치하지 않습니다.");
    }

    // ==================== findPassword ====================

    @Test
    @DisplayName("비밀번호 찾기 - 정상")
    void findPassword_성공() {
        UserDto.FindPwRequest req = UserDto.FindPwRequest.builder()
                .id("user1").name("홍길동").build();
        User user = User.builder().id("user1").pw("$2a$encoded").name("홍길동").build();
        when(userRepository.findByIdAndName("user1", "홍길동")).thenReturn(user);

        String pw = userService.findPassword(req);
        assertThat(pw).isEqualTo("$2a$encoded");
    }

    @Test
    @DisplayName("비밀번호 찾기 - 없는 회원 예외")
    void findPassword_없는회원_예외() {
        UserDto.FindPwRequest req = UserDto.FindPwRequest.builder()
                .id("user1").name("홍길동").build();
        when(userRepository.findByIdAndName("user1", "홍길동")).thenReturn(null);

        assertThatThrownBy(() -> userService.findPassword(req))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("일치하는 회원 정보가 없습니다.");
    }

    // ==================== getUser ====================

    @Test
    @DisplayName("회원 조회 - 정상")
    void getUser_성공() {
        User user = User.builder().id("user1").pw("$2a$encoded").name("홍길동").build();
        when(userRepository.findById("user1")).thenReturn(user);

        Map<String, String> result = userService.getUser("user1");
        assertThat(result.get("id")).isEqualTo("user1");
        assertThat(result.get("name")).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("회원 조회 - 없는 회원 예외")
    void getUser_없는회원_예외() {
        when(userRepository.findById("unknown")).thenReturn(null);

        assertThatThrownBy(() -> userService.getUser("unknown"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("회원 정보를 찾을 수 없습니다.");
    }

    // ==================== updateUser ====================

    @Test
    @DisplayName("회원 수정 - 정상")
    void updateUser_성공() {
        User user = User.builder().id("user1").pw("$2a$encoded").name("홍길동").build();
        UserDto.UpdateRequest req = UserDto.UpdateRequest.builder()
                .pw("newpw").name("새이름").build();
        when(userRepository.findById("user1")).thenReturn(user);

        assertDoesNotThrow(() -> userService.updateUser("user1", req));
        assertThat(user.getName()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("회원 수정 - 없는 회원 예외")
    void updateUser_없는회원_예외() {
        UserDto.UpdateRequest req = UserDto.UpdateRequest.builder()
                .pw("newpw").name("새이름").build();
        when(userRepository.findById("unknown")).thenReturn(null);

        assertThatThrownBy(() -> userService.updateUser("unknown", req))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("회원 정보를 찾을 수 없습니다.");
    }

    // ==================== isAdmin ====================

    @Test
    @DisplayName("관리자 여부 - 관리자 true")
    void isAdmin_관리자_true() {
        when(userRepository.findById("admin"))
                .thenReturn(User.builder().id("admin").pw("pw").name("관리자").role("ADMIN").build());

        assertThat(userService.isAdmin("admin")).isTrue();
    }

    @Test
    @DisplayName("관리자 여부 - 일반 사용자 false")
    void isAdmin_일반사용자_false() {
        when(userRepository.findById("user1"))
                .thenReturn(User.builder().id("user1").pw("pw").name("홍길동").build());

        assertThat(userService.isAdmin("user1")).isFalse();
    }

    @Test
    @DisplayName("관리자 여부 - 존재하지 않는 사용자 false")
    void isAdmin_없는사용자_false() {
        when(userRepository.findById("ghost")).thenReturn(null);

        assertThat(userService.isAdmin("ghost")).isFalse();
    }

    // ==================== deleteUser ====================

    @Test
    @DisplayName("회원 탈퇴 - 정상")
    void deleteUser_성공() {
        when(userRepository.existsById("user1")).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteUser("user1"));
        verify(userRepository).deleteById("user1");
    }

    @Test
    @DisplayName("회원 탈퇴 - 없는 회원 예외")
    void deleteUser_없는회원_예외() {
        when(userRepository.existsById("unknown")).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser("unknown"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("회원 정보를 찾을 수 없습니다.");
        verify(userRepository, never()).deleteById(any());
    }
}
