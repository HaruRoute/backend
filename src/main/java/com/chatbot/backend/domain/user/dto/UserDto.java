package com.chatbot.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class UserDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinRequest {
        @NotBlank(message = "아이디를 입력해주세요.")
        @Size(max = 50, message = "아이디는 50자 이하여야 합니다.")
        private String id;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String pw;

        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "아이디를 입력해주세요.")
        private String id;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String pw;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindPwRequest {
        @NotBlank(message = "아이디를 입력해주세요.")
        private String id;

        @NotBlank(message = "이름을 입력해주세요.")
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String pw;
        private String name;
    }
}
