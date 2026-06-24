package com.chatbot.backend.domain.qna.repository;

import com.chatbot.backend.domain.qna.entity.Qna;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface QnaRepository {
    void insert(Qna qna);
    List<Qna> findAll(@Param("offset") int offset, @Param("size") int size, @Param("best") boolean best);
    int countAll();
    Qna findById(Long id);
    void incrementViewCount(Long id);
    void update(Qna qna);
    void delete(Long id);
}
