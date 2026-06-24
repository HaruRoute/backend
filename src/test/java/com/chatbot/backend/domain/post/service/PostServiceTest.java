package com.chatbot.backend.domain.post.service;

import com.chatbot.backend.domain.post.dto.PostDto;
import com.chatbot.backend.domain.post.entity.Post;
import com.chatbot.backend.domain.post.repository.PostRepository;
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
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private PostService postService;

    private User user(String id, String role) {
        return User.builder().id(id).name("홍길동").pw("pw").role(role).build();
    }

    private Post post(Long id, String authorId) {
        return Post.builder().id(id).title("제목").content("내용")
                .authorId(authorId).authorName("홍길동").viewCount(0)
                .category("FREE").createdAt(LocalDateTime.now()).build();
    }

    private PostDto.Request request(String category) {
        return PostDto.Request.builder().title("제목").content("내용").category(category).build();
    }

    // ==================== getList ====================

    @Test
    @DisplayName("게시글 목록 조회 - 정상")
    void getList_정상조회() {
        when(postRepository.findAll(0, 10, "FREE", false)).thenReturn(List.of(post(1L, "user1")));
        when(postRepository.countAll("FREE")).thenReturn(1);

        PostDto.PageResponse result = postService.getList(0, 10, "FREE", false);

        assertThat(result.getPosts()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시글 목록 조회 - totalPages 계산")
    void getList_totalPages_계산() {
        when(postRepository.findAll(0, 10, "FREE", false)).thenReturn(List.of());
        when(postRepository.countAll("FREE")).thenReturn(25);

        PostDto.PageResponse result = postService.getList(0, 10, "FREE", false);

        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    // ==================== getDetail ====================

    @Test
    @DisplayName("게시글 상세 조회 - 정상 + 조회수 증가")
    void getDetail_성공_조회수증가() {
        when(postRepository.findById(1L)).thenReturn(post(1L, "user1"));

        PostDto.DetailResponse result = postService.getDetail(1L);

        verify(postRepository).incrementViewCount(1L);
        assertThat(result.getTitle()).isEqualTo("제목");
        assertThat(result.getViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시글 상세 조회 - 존재하지 않는 게시글 예외")
    void getDetail_없는게시글_예외() {
        when(postRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> postService.getDetail(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");
    }

    // ==================== create ====================

    @Test
    @DisplayName("게시글 작성 - 정상")
    void create_성공() {
        when(userRepository.findById("user1")).thenReturn(user("user1", "USER"));

        PostDto.DetailResponse result = postService.create("user1", request("FREE"));

        verify(postRepository).insert(any(Post.class));
        assertThat(result.getTitle()).isEqualTo("제목");
        assertThat(result.getCategory()).isEqualTo("FREE");
    }

    @Test
    @DisplayName("게시글 작성 - QUESTION 카테고리 정상 저장")
    void create_QUESTION_카테고리() {
        when(userRepository.findById("user1")).thenReturn(user("user1", "USER"));

        PostDto.DetailResponse result = postService.create("user1", request("QUESTION"));

        assertThat(result.getCategory()).isEqualTo("QUESTION");
    }

    @Test
    @DisplayName("게시글 작성 - 존재하지 않는 사용자 예외")
    void create_없는사용자_예외() {
        when(userRepository.findById("unknown")).thenReturn(null);

        assertThatThrownBy(() -> postService.create("unknown", request("FREE")))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
        verify(postRepository, never()).insert(any());
    }

    // ==================== update ====================

    @Test
    @DisplayName("게시글 수정 - 작성자 본인 정상 수정")
    void update_본인_성공() {
        when(postRepository.findById(1L)).thenReturn(post(1L, "user1"));

        PostDto.DetailResponse result = postService.update("user1", 1L, request("FREE"));

        verify(postRepository).update(any(Post.class));
        assertThat(result.getTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("게시글 수정 - 관리자 타인 게시글 수정 가능")
    void update_관리자_타인게시글_수정가능() {
        when(postRepository.findById(1L)).thenReturn(post(1L, "user1"));
        when(userRepository.findById("admin")).thenReturn(user("admin", "ADMIN"));

        postService.update("admin", 1L, request("FREE"));

        verify(postRepository).update(any(Post.class));
    }

    @Test
    @DisplayName("게시글 수정 - 권한 없는 사용자 예외")
    void update_권한없음_예외() {
        when(postRepository.findById(1L)).thenReturn(post(1L, "user1"));
        when(userRepository.findById("user2")).thenReturn(user("user2", "USER"));

        assertThatThrownBy(() -> postService.update("user2", 1L, request("FREE")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("권한이 없습니다.");
        verify(postRepository, never()).update(any());
    }

    @Test
    @DisplayName("게시글 수정 - 존재하지 않는 게시글 예외")
    void update_없는게시글_예외() {
        when(postRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> postService.update("user1", 99L, request("FREE")))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ==================== delete ====================

    @Test
    @DisplayName("게시글 삭제 - 작성자 본인 정상 삭제")
    void delete_본인_성공() {
        when(postRepository.findById(1L)).thenReturn(post(1L, "user1"));

        postService.delete("user1", 1L);

        verify(postRepository).delete(1L);
    }

    @Test
    @DisplayName("게시글 삭제 - 관리자 타인 게시글 삭제 가능")
    void delete_관리자_타인게시글_삭제가능() {
        when(postRepository.findById(1L)).thenReturn(post(1L, "user1"));
        when(userRepository.findById("admin")).thenReturn(user("admin", "ADMIN"));

        postService.delete("admin", 1L);

        verify(postRepository).delete(1L);
    }

    @Test
    @DisplayName("게시글 삭제 - 권한 없는 사용자 예외")
    void delete_권한없음_예외() {
        when(postRepository.findById(1L)).thenReturn(post(1L, "user1"));
        when(userRepository.findById("user2")).thenReturn(user("user2", "USER"));

        assertThatThrownBy(() -> postService.delete("user2", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("권한이 없습니다.");
        verify(postRepository, never()).delete(any());
    }

    @Test
    @DisplayName("게시글 삭제 - 존재하지 않는 게시글 예외")
    void delete_없는게시글_예외() {
        when(postRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> postService.delete("user1", 99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
