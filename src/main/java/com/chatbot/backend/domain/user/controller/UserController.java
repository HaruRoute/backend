package com.chatbot.backend.domain.user.controller;

import com.chatbot.backend.domain.user.dto.UserDto;
import com.chatbot.backend.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Map<String, String>> join(@Valid @RequestBody UserDto.JoinRequest request) {
        userService.join(request);
        return ResponseEntity.ok(Map.of("message", "가입 성공"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserDto.LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    @PostMapping("/find-pw")
    public ResponseEntity<Map<String, String>> findPw(@Valid @RequestBody UserDto.FindPwRequest request) {
        return ResponseEntity.ok(Map.of("password", userService.findPassword(request)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, String>> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, String>> updateUser(
            @PathVariable String userId,
            @RequestBody UserDto.UpdateRequest request) {
        userService.updateUser(userId, request);
        return ResponseEntity.ok(Map.of("message", "수정 성공"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "탈퇴 성공"));
    }
}
