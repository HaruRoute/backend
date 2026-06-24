package com.chatbot.backend.domain.qna.service;

import com.chatbot.backend.domain.qna.dto.QnaDto;
import com.chatbot.backend.domain.qna.entity.Qna;
import com.chatbot.backend.domain.qna.repository.QnaRepository;
import com.chatbot.backend.domain.user.entity.User;
import com.chatbot.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QnaServiceTest {

    @Mock private QnaRepository qnaRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private QnaService qnaService;

    private User user(String id, String role) {
        return User.builder().id(id).name("홍길동").pw("pw").role(role).build();
    }

    private Qna qna(Long id, String authorId) {
        return Qna.builder().id(id).title("질문 제목").content("질문 내용")
                .authorId(authorId).authorName("홍길동").viewCount(0)
                .createdAt(LocalDateTime.now()).build();
    }

    private QnaDto.Request request() {
        return QnaDto.Request.builder().title("질문 제목").content("질문 내용").build();
    }

    // ==================== getList ====================

    @Test
    @DisplayName("QnA 목록 조회 - 정상")
    void getList_정상조회() {
        when(qnaRepository.findAll(0, 10, false)).thenReturn(List.of(qna(1L, "user1")));
        when(qnaRepository.countAll()).thenReturn(1);

        QnaDto.PageResponse result = qnaService.getList(0, 10, false);

        assertThat(result.getQnas()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("QnA 목록 조회 - totalPages 계산")
    void getList_totalPages_계산() {
        when(qnaRepository.findAll(0, 10, false)).thenReturn(List.of());
        when(qnaRepository.countAll()).thenReturn(21);

        QnaDto.PageResponse result = qnaService.getList(0, 10, false);

        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("QnA 목록 조회 - 빈 목록")
    void getList_빈목록() {
        when(qnaRepository.findAll(0, 10, false)).thenReturn(List.of());
        when(qnaRepository.countAll()).thenReturn(0);

        QnaDto.PageResponse result = qnaService.getList(0, 10, false);

        assertThat(result.getQnas()).isEmpty();
        assertThat(result.getTotalPages()).isEqualTo(0);
    }

    // ==================== getDetail ====================

    @Test
    @DisplayName("QnA 상세 조회 - 정상 + 조회수 증가")
    void getDetail_성공_조회수증가() {
        when(qnaRepository.findById(1L)).thenReturn(qna(1L, "user1"));

        QnaDto.DetailResponse result = qnaService.getDetail(1L);

        verify(qnaRepository).incrementViewCount(1L);
        assertThat(result.getTitle()).isEqualTo("질문 제목");
        assertThat(result.getViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("QnA 상세 조회 - 존재하지 않는 질문 예외")
    void getDetail_없는질문_예외() {
        when(qnaRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> qnaService.getDetail(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("질문 게시글을 찾을 수 없습니다.");
    }

    // ==================== create ====================

    @Test
    @DisplayName("QnA 작성 - 정상")
    void create_성공() {
        when(userRepository.findById("user1")).thenReturn(user("user1", "USER"));

        QnaDto.DetailResponse result = qnaService.create("user1", request());

        verify(qnaRepository).insert(any(Qna.class));
        assertThat(result.getTitle()).isEqualTo("질문 제목");
        assertThat(result.getAuthorId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("QnA 작성 - 존재하지 않는 사용자 예외")
    void create_없는사용자_예외() {
        when(userRepository.findById("ghost")).thenReturn(null);

        assertThatThrownBy(() -> qnaService.create("ghost", request()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
        verify(qnaRepository, never()).insert(any());
    }

    // ==================== update ====================

    @Test
    @DisplayName("QnA 수정 - 작성자 본인 정상 수정")
    void update_본인_성공() {
        when(qnaRepository.findById(1L)).thenReturn(qna(1L, "user1"));

        QnaDto.DetailResponse result = qnaService.update("user1", 1L, request());

        verify(qnaRepository).update(any(Qna.class));
        assertThat(result.getTitle()).isEqualTo("질문 제목");
    }

    @Test
    @DisplayName("QnA 수정 - 관리자 타인 질문 수정 가능")
    void update_관리자_타인질문_수정가능() {
        when(qnaRepository.findById(1L)).thenReturn(qna(1L, "user1"));
        when(userRepository.findById("admin")).thenReturn(user("admin", "ADMIN"));

        qnaService.update("admin", 1L, request());

        verify(qnaRepository).update(any(Qna.class));
    }

    @Test
    @DisplayName("QnA 수정 - 권한 없는 사용자 예외")
    void update_권한없음_예외() {
        when(qnaRepository.findById(1L)).thenReturn(qna(1L, "user1"));
        when(userRepository.findById("user2")).thenReturn(user("user2", "USER"));

        assertThatThrownBy(() -> qnaService.update("user2", 1L, request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("권한이 없습니다.");
        verify(qnaRepository, never()).update(any());
    }

    @Test
    @DisplayName("QnA 수정 - 존재하지 않는 질문 예외")
    void update_없는질문_예외() {
        when(qnaRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> qnaService.update("user1", 99L, request()))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ==================== delete ====================

    @Test
    @DisplayName("QnA 삭제 - 작성자 본인 정상 삭제")
    void delete_본인_성공() {
        when(qnaRepository.findById(1L)).thenReturn(qna(1L, "user1"));

        qnaService.delete("user1", 1L);

        verify(qnaRepository).delete(1L);
    }

    @Test
    @DisplayName("QnA 삭제 - 관리자 타인 질문 삭제 가능")
    void delete_관리자_타인질문_삭제가능() {
        when(qnaRepository.findById(1L)).thenReturn(qna(1L, "user1"));
        when(userRepository.findById("admin")).thenReturn(user("admin", "ADMIN"));

        qnaService.delete("admin", 1L);

        verify(qnaRepository).delete(1L);
    }

    @Test
    @DisplayName("QnA 삭제 - 권한 없는 사용자 예외")
    void delete_권한없음_예외() {
        when(qnaRepository.findById(1L)).thenReturn(qna(1L, "user1"));
        when(userRepository.findById("user2")).thenReturn(user("user2", "USER"));

        assertThatThrownBy(() -> qnaService.delete("user2", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("권한이 없습니다.");
        verify(qnaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("QnA 삭제 - 존재하지 않는 질문 예외")
    void delete_없는질문_예외() {
        when(qnaRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> qnaService.delete("user1", 99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
