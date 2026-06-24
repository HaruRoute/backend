package com.chatbot.backend.domain.notice.service;

import com.chatbot.backend.domain.notice.dto.NoticeDto;
import com.chatbot.backend.domain.notice.entity.Notice;
import com.chatbot.backend.domain.notice.repository.NoticeRepository;
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
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    public List<NoticeDto.Response> getAll() {
        return noticeRepository.findAllOrderByCreatedAtDesc().stream()
                .map(NoticeDto.Response::from).toList();
    }

    public List<NoticeDto.Response> getPinned() {
        return noticeRepository.findPinnedOrderByCreatedAtDesc().stream()
                .map(NoticeDto.Response::from).toList();
    }

    public NoticeDto.Response getById(Long id) {
        return NoticeDto.Response.from(findOrThrow(id));
    }

    @Transactional
    public NoticeDto.Response create(String userId, NoticeDto.Request request) {
        checkAdmin(userId);
        User user = userRepository.findById(userId);
        Notice notice = Notice.builder()
                .title(request.getTitle()).content(request.getContent())
                .authorId(userId).authorName(user.getName())
                .isPinned(request.isPinned()).createdAt(LocalDateTime.now())
                .build();
        noticeRepository.insert(notice);
        return NoticeDto.Response.from(notice);
    }

    @Transactional
    public NoticeDto.Response update(String userId, Long id, NoticeDto.Request request) {
        checkAdmin(userId);
        Notice notice = findOrThrow(id);
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setPinned(request.isPinned());
        notice.setUpdatedAt(LocalDateTime.now());
        noticeRepository.update(notice);
        return NoticeDto.Response.from(notice);
    }

    @Transactional
    public void delete(String userId, Long id) {
        checkAdmin(userId);
        findOrThrow(id);
        noticeRepository.delete(id);
    }

    private void checkAdmin(String userId) {
        User user = userRepository.findById(userId);
        if (user == null || !"ADMIN".equals(user.getRole()))
            throw new IllegalStateException("관리자만 접근 가능합니다.");
    }

    private Notice findOrThrow(Long id) {
        Notice n = noticeRepository.findById(id);
        if (n == null) throw new NoSuchElementException("공지사항을 찾을 수 없습니다.");
        return n;
    }
}
