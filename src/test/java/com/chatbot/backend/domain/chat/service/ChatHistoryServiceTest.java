package com.chatbot.backend.domain.chat.service;

import com.chatbot.backend.domain.chat.entity.ChatHistory;
import com.chatbot.backend.domain.chat.repository.ChatHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatHistoryServiceTest {

    @Mock
    private ChatHistoryRepository chatHistoryRepository;

    @InjectMocks
    private ChatHistoryService chatHistoryService;

    @Test
    @DisplayName("채팅 이력 저장 - 정상")
    void save_정상저장() {
        chatHistoryService.save("user1", "서울 여행 추천해줘", "서울에는 경복궁, 남산타워...");
        verify(chatHistoryRepository).insert(any(ChatHistory.class));
    }

    @Test
    @DisplayName("채팅 이력 저장 - repository에 올바른 데이터 전달")
    void save_올바른데이터_전달() {
        chatHistoryService.save("user1", "부산 추천", "부산에는 해운대...");

        verify(chatHistoryRepository).insert(argThat(history ->
                history.getUserId().equals("user1") &&
                history.getMessage().equals("부산 추천") &&
                history.getResponse().equals("부산에는 해운대...")
        ));
    }

    @Test
    @DisplayName("채팅 이력 조회 - 정상")
    void getHistory_정상조회() {
        ChatHistory h1 = ChatHistory.builder()
                .userId("user1").message("질문1").response("답변1").build();
        ChatHistory h2 = ChatHistory.builder()
                .userId("user1").message("질문2").response("답변2").build();
        when(chatHistoryRepository.findByUserIdOrderByCreatedAtAsc("user1"))
                .thenReturn(List.of(h1, h2));

        List<ChatHistory> result = chatHistoryService.getHistory("user1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMessage()).isEqualTo("질문1");
        assertThat(result.get(1).getMessage()).isEqualTo("질문2");
    }

    @Test
    @DisplayName("채팅 이력 조회 - 이력 없음")
    void getHistory_이력없음() {
        when(chatHistoryRepository.findByUserIdOrderByCreatedAtAsc("user1"))
                .thenReturn(List.of());

        List<ChatHistory> result = chatHistoryService.getHistory("user1");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("채팅 이력 조회 - 다른 유저 이력은 조회 안 됨")
    void getHistory_다른유저_이력분리() {
        ChatHistory h = ChatHistory.builder()
                .userId("user2").message("질문").response("답변").build();
        when(chatHistoryRepository.findByUserIdOrderByCreatedAtAsc("user1"))
                .thenReturn(List.of());
        when(chatHistoryRepository.findByUserIdOrderByCreatedAtAsc("user2"))
                .thenReturn(List.of(h));

        assertThat(chatHistoryService.getHistory("user1")).isEmpty();
        assertThat(chatHistoryService.getHistory("user2")).hasSize(1);
    }
}
