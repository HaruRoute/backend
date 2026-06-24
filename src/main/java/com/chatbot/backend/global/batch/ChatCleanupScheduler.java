package com.chatbot.backend.global.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job cleanupChatHistoryJob;

    // 매일 새벽 2시 실행
    @Scheduled(cron = "0 0 2 * * *")
    public void runCleanupJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("runAt", System.currentTimeMillis()) // 매 실행마다 고유 파라미터
                    .toJobParameters();
            jobLauncher.run(cleanupChatHistoryJob, params);
            log.info("채팅 기록 정리 배치 완료");
        } catch (Exception e) {
            log.error("채팅 기록 정리 배치 실패", e);
        }
    }
}
