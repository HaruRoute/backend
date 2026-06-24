package com.chatbot.backend.domain.qna.controller;

import com.chatbot.backend.domain.qna.dto.QnaDto;
import com.chatbot.backend.domain.qna.service.QnaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qnas")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    @GetMapping
    public ResponseEntity<QnaDto.PageResponse> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean best) {
        return ResponseEntity.ok(qnaService.getList(page, size, best));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QnaDto.DetailResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(qnaService.getDetail(id));
    }

    @PostMapping
    public ResponseEntity<QnaDto.DetailResponse> create(
            @Valid @RequestBody QnaDto.Request request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(qnaService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QnaDto.DetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody QnaDto.Request request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(qnaService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        qnaService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
