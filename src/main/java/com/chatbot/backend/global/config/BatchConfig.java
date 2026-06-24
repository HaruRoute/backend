package com.chatbot.backend.global.config;

import com.chatbot.backend.domain.chat.entity.ChatHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final DataSource dataSource;

    @Value("${chat.cleanup.retention-days:90}")
    private int retentionDays;

    @Bean
    public Job cleanupChatHistoryJob(JobRepository jobRepository, Step deleteOldChatsStep) {
        return new JobBuilder("cleanupChatHistoryJob", jobRepository)
                .start(deleteOldChatsStep)
                .build();
    }

    @Bean
    public Step deleteOldChatsStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("deleteOldChatsStep", jobRepository)
                .<ChatHistory, ChatHistory>chunk(100, tx)
                .reader(oldChatReader(null))
                .processor(loggingProcessor())
                .writer(deleteChatWriter())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<ChatHistory> oldChatReader(
            @Value("#{jobParameters['runAt']}") Long runAt) {
        LocalDateTime cutoff = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(runAt != null ? runAt : System.currentTimeMillis()),
                java.time.ZoneId.systemDefault()
        ).minusDays(retentionDays);

        return new JdbcCursorItemReaderBuilder<ChatHistory>()
                .name("oldChatReader")
                .dataSource(dataSource)
                .sql("SELECT id, user_id, message, response, created_at FROM chat_history WHERE created_at < ?")
                .preparedStatementSetter(ps -> ps.setObject(1, cutoff))
                .rowMapper((rs, rowNum) -> ChatHistory.builder()
                        .id(rs.getLong("id"))
                        .userId(rs.getString("user_id"))
                        .message(rs.getString("message"))
                        .response(rs.getString("response"))
                        .createdAt(rs.getObject("created_at", LocalDateTime.class))
                        .build())
                .build();
    }

    @Bean
    public ItemProcessor<ChatHistory, ChatHistory> loggingProcessor() {
        return chat -> {
            log.debug("삭제 대상 - id: {}, userId: {}, createdAt: {}", chat.getId(), chat.getUserId(), chat.getCreatedAt());
            return chat;
        };
    }

    @Bean
    public JdbcBatchItemWriter<ChatHistory> deleteChatWriter() {
        return new JdbcBatchItemWriterBuilder<ChatHistory>()
                .dataSource(dataSource)
                .sql("DELETE FROM chat_history WHERE id = :id")
                .beanMapped()
                .build();
    }
}
