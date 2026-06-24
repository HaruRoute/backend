package com.chatbot.backend.domain.notice.controller;

import com.chatbot.backend.domain.notice.dto.NoticeDto;
import com.chatbot.backend.domain.notice.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<List<NoticeDto.Response>> getAll() {
        return ResponseEntity.ok(noticeService.getAll());
    }

    @GetMapping("/pinned")
    public ResponseEntity<List<NoticeDto.Response>> getPinned() {
        return ResponseEntity.ok(noticeService.getPinned());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<NoticeDto.Response> create(
            @Valid @RequestBody NoticeDto.Request request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(noticeService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticeDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody NoticeDto.Request request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(noticeService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        noticeService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
