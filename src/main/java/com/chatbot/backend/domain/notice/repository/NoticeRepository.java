package com.chatbot.backend.domain.notice.repository;

import com.chatbot.backend.domain.notice.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface NoticeRepository {
    void insert(Notice notice);
    List<Notice> findAllOrderByCreatedAtDesc();
    List<Notice> findPinnedOrderByCreatedAtDesc();
    Notice findById(Long id);
    void update(Notice notice);
    void delete(Long id);
}
