package com.chatbot.backend.domain.post.repository;

import com.chatbot.backend.domain.post.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostRepository {
    void insert(Post post);
    List<Post> findAll(@Param("offset") int offset, @Param("size") int size,
                       @Param("category") String category, @Param("best") boolean best);
    int countAll(@Param("category") String category);
    Post findById(Long id);
    void incrementViewCount(Long id);
    void update(Post post);
    void delete(Long id);
}
