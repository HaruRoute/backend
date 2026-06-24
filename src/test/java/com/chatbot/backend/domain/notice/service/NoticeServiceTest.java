package com.chatbot.backend.domain.notice.service;

import com.chatbot.backend.domain.notice.dto.NoticeDto;
import com.chatbot.backend.domain.notice.entity.Notice;
import com.chatbot.backend.domain.notice.repository.NoticeRepository;
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
class NoticeServiceTest {

    @Mock private NoticeRepository noticeRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private NoticeService noticeService;

    private User admin() {
        return User.builder().id("admin").name("관리자").pw("pw").role("ADMIN").build();
    }

    private User normalUser() {
        return User.builder().id("user1").name("홍길동").pw("pw").role("USER").build();
    }

    private Notice notice(Long id) {
        return Notice.builder().id(id).title("공지 제목").content("공지 내용")
                .authorId("admin").authorName("관리자").isPinned(false)
                .createdAt(LocalDateTime.now()).build();
    }

    private NoticeDto.Request request() {
        return new NoticeDto.Request("공지 제목", "공지 내용", false);
    }

    // ==================== getAll ====================

    @Test
    @DisplayName("공지사항 전체 조회 - 정상")
    void getAll_정상조회() {
        when(noticeRepository.findAllOrderByCreatedAtDesc())
                .thenReturn(List.of(notice(1L), notice(2L)));

        List<NoticeDto.Response> result = noticeService.getAll();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("공지사항 전체 조회 - 빈 목록")
    void getAll_빈목록() {
        when(noticeRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of());

        assertThat(noticeService.getAll()).isEmpty();
    }

    // ==================== getPinned ====================

    @Test
    @DisplayName("고정 공지 조회 - 정상")
    void getPinned_정상조회() {
        Notice pinned = Notice.builder().id(1L).title("중요 공지").isPinned(true)
                .createdAt(LocalDateTime.now()).build();
        when(noticeRepository.findPinnedOrderByCreatedAtDesc()).thenReturn(List.of(pinned));

        List<NoticeDto.Response> result = noticeService.getPinned();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("중요 공지");
    }

    // ==================== create ====================

    @Test
    @DisplayName("공지사항 작성 - 관리자 정상 작성")
    void create_관리자_성공() {
        when(userRepository.findById("admin")).thenReturn(admin());

        NoticeDto.Response result = noticeService.create("admin", request());

        verify(noticeRepository).insert(any(Notice.class));
        assertThat(result.getTitle()).isEqualTo("공지 제목");
    }

    @Test
    @DisplayName("공지사항 작성 - 일반 사용자 예외")
    void create_일반사용자_예외() {
        when(userRepository.findById("user1")).thenReturn(normalUser());

        assertThatThrownBy(() -> noticeService.create("user1", request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("관리자만 접근 가능합니다.");
        verify(noticeRepository, never()).insert(any());
    }

    @Test
    @DisplayName("공지사항 작성 - 존재하지 않는 사용자 예외")
    void create_없는사용자_예외() {
        when(userRepository.findById("ghost")).thenReturn(null);

        assertThatThrownBy(() -> noticeService.create("ghost", request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("관리자만 접근 가능합니다.");
    }

    // ==================== update ====================

    @Test
    @DisplayName("공지사항 수정 - 관리자 정상 수정")
    void update_관리자_성공() {
        when(userRepository.findById("admin")).thenReturn(admin());
        when(noticeRepository.findById(1L)).thenReturn(notice(1L));

        NoticeDto.Response result = noticeService.update("admin", 1L, request());

        verify(noticeRepository).update(any(Notice.class));
        assertThat(result.getTitle()).isEqualTo("공지 제목");
    }

    @Test
    @DisplayName("공지사항 수정 - 일반 사용자 예외")
    void update_일반사용자_예외() {
        when(userRepository.findById("user1")).thenReturn(normalUser());

        assertThatThrownBy(() -> noticeService.update("user1", 1L, request()))
                .isInstanceOf(IllegalStateException.class);
        verify(noticeRepository, never()).update(any());
    }

    @Test
    @DisplayName("공지사항 수정 - 존재하지 않는 공지 예외")
    void update_없는공지_예외() {
        when(userRepository.findById("admin")).thenReturn(admin());
        when(noticeRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> noticeService.update("admin", 99L, request()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("공지사항을 찾을 수 없습니다.");
    }

    // ==================== delete ====================

    @Test
    @DisplayName("공지사항 삭제 - 관리자 정상 삭제")
    void delete_관리자_성공() {
        when(userRepository.findById("admin")).thenReturn(admin());
        when(noticeRepository.findById(1L)).thenReturn(notice(1L));

        noticeService.delete("admin", 1L);

        verify(noticeRepository).delete(1L);
    }

    @Test
    @DisplayName("공지사항 삭제 - 일반 사용자 예외")
    void delete_일반사용자_예외() {
        when(userRepository.findById("user1")).thenReturn(normalUser());

        assertThatThrownBy(() -> noticeService.delete("user1", 1L))
                .isInstanceOf(IllegalStateException.class);
        verify(noticeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("공지사항 삭제 - 존재하지 않는 공지 예외")
    void delete_없는공지_예외() {
        when(userRepository.findById("admin")).thenReturn(admin());
        when(noticeRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> noticeService.delete("admin", 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("공지사항을 찾을 수 없습니다.");
        verify(noticeRepository, never()).delete(any());
    }
}
