package com.chatbot.backend.domain.post.controller;

import com.chatbot.backend.domain.post.dto.PostDto;
import com.chatbot.backend.domain.post.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<PostDto.PageResponse> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "false") boolean best) {
        return ResponseEntity.ok(postService.getList(page, size, category, best));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto.DetailResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getDetail(id));
    }

    @PostMapping
    public ResponseEntity<PostDto.DetailResponse> create(
            @Valid @RequestBody PostDto.Request request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(postService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto.DetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PostDto.Request request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(postService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        postService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
