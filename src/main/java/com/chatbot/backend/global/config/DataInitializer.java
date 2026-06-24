package com.chatbot.backend.global.config;

import com.chatbot.backend.domain.user.entity.User;
import com.chatbot.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByRole("ADMIN")) {
            User admin = User.builder()
                    .id("111")
                    .pw(passwordEncoder.encode("111"))
                    .name("관리자")
                    .role("ADMIN")
                    .build();
            userRepository.insert(admin);
            log.info("기본 관리자 계정 생성 완료 (id=111)");
        }
    }
}
