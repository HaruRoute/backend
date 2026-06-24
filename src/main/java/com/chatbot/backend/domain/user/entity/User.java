package com.chatbot.backend.domain.user.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String id;
    private String pw;
    private String name;

    @Builder.Default
    private String role = "USER";

    public void update(String pw, String name) {
        if (pw != null && !pw.isBlank()) this.pw = pw;
        if (name != null && !name.isBlank()) this.name = name;
    }
}
