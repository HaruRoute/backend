package com.chatbot.backend.domain.post.service;

import com.chatbot.backend.domain.post.dto.PostDto;
import com.chatbot.backend.domain.post.entity.Post;
import com.chatbot.backend.domain.post.repository.PostRepository;
import com.chatbot.backend.domain.user.entity.User;
import com.chatbot.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostDto.PageResponse getList(int page, int size, String category, boolean best) {
        int offset = page * size;
        List<PostDto.ListResponse> posts = postRepository.findAll(offset, size, category, best).stream()
                .map(PostDto.ListResponse::from).toList();
        int total = postRepository.countAll(category);
        return PostDto.PageResponse.builder()
                .posts(posts).totalCount(total).page(page).size(size)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }

    @Transactional
    public PostDto.DetailResponse getDetail(Long id) {
        Post post = findOrThrow(id);
        postRepository.incrementViewCount(id);
        post.setViewCount(post.getViewCount() + 1);
        return PostDto.DetailResponse.from(post);
    }

    @Transactional
    public PostDto.DetailResponse create(String userId, PostDto.Request request) {
        User user = userRepository.findById(userId);
        if (user == null) throw new NoSuchElementException("사용자를 찾을 수 없습니다.");
        String category = "QUESTION".equals(request.getCategory()) ? "QUESTION" : "FREE";
        Post post = Post.builder()
                .title(request.getTitle()).content(request.getContent())
                .authorId(userId).authorName(user.getName())
                .category(category)
                .routeData(request.getRouteData()).routeName(request.getRouteName())
                .createdAt(LocalDateTime.now()).build();
        postRepository.insert(post);
        return PostDto.DetailResponse.from(post);
    }

    @Transactional
    public PostDto.DetailResponse update(String userId, Long id, PostDto.Request request) {
        Post post = findOrThrow(id);
        checkOwnerOrAdmin(userId, post.getAuthorId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory("QUESTION".equals(request.getCategory()) ? "QUESTION" : "FREE");
        post.setRouteData(request.getRouteData());
        post.setRouteName(request.getRouteName());
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.update(post);
        return PostDto.DetailResponse.from(post);
    }

    @Transactional
    public void delete(String userId, Long id) {
        Post post = findOrThrow(id);
        checkOwnerOrAdmin(userId, post.getAuthorId());
        postRepository.delete(id);
    }

    private void checkOwnerOrAdmin(String userId, String authorId) {
        if (userId.equals(authorId)) return;
        User user = userRepository.findById(userId);
        if (user == null || !"ADMIN".equals(user.getRole()))
            throw new IllegalStateException("권한이 없습니다.");
    }

    private Post findOrThrow(Long id) {
        Post p = postRepository.findById(id);
        if (p == null) throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
        return p;
    }
}
