package com.chatbot.backend.domain.qna.service;

import com.chatbot.backend.domain.qna.dto.QnaDto;
import com.chatbot.backend.domain.qna.entity.Qna;
import com.chatbot.backend.domain.qna.repository.QnaRepository;
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
public class QnaService {

    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;

    public QnaDto.PageResponse getList(int page, int size, boolean best) {
        int offset = page * size;
        List<QnaDto.ListResponse> qnas = qnaRepository.findAll(offset, size, best).stream()
                .map(QnaDto.ListResponse::from).toList();
        int total = qnaRepository.countAll();
        return QnaDto.PageResponse.builder()
                .qnas(qnas).totalCount(total).page(page).size(size)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }

    @Transactional
    public QnaDto.DetailResponse getDetail(Long id) {
        Qna qna = findOrThrow(id);
        qnaRepository.incrementViewCount(id);
        qna.setViewCount(qna.getViewCount() + 1);
        return QnaDto.DetailResponse.from(qna);
    }

    @Transactional
    public QnaDto.DetailResponse create(String userId, QnaDto.Request request) {
        User user = userRepository.findById(userId);
        if (user == null) throw new NoSuchElementException("사용자를 찾을 수 없습니다.");
        Qna qna = Qna.builder()
                .title(request.getTitle()).content(request.getContent())
                .authorId(userId).authorName(user.getName())
                .routeData(request.getRouteData()).routeName(request.getRouteName())
                .createdAt(LocalDateTime.now()).build();
        qnaRepository.insert(qna);
        return QnaDto.DetailResponse.from(qna);
    }

    @Transactional
    public QnaDto.DetailResponse update(String userId, Long id, QnaDto.Request request) {
        Qna qna = findOrThrow(id);
        checkOwnerOrAdmin(userId, qna.getAuthorId());
        qna.setTitle(request.getTitle());
        qna.setContent(request.getContent());
        qna.setRouteData(request.getRouteData());
        qna.setRouteName(request.getRouteName());
        qna.setUpdatedAt(LocalDateTime.now());
        qnaRepository.update(qna);
        return QnaDto.DetailResponse.from(qna);
    }

    @Transactional
    public void delete(String userId, Long id) {
        Qna qna = findOrThrow(id);
        checkOwnerOrAdmin(userId, qna.getAuthorId());
        qnaRepository.delete(id);
    }

    private void checkOwnerOrAdmin(String userId, String authorId) {
        if (userId.equals(authorId)) return;
        User user = userRepository.findById(userId);
        if (user == null || !"ADMIN".equals(user.getRole()))
            throw new IllegalStateException("권한이 없습니다.");
    }

    private Qna findOrThrow(Long id) {
        Qna q = qnaRepository.findById(id);
        if (q == null) throw new NoSuchElementException("질문 게시글을 찾을 수 없습니다.");
        return q;
    }
}
