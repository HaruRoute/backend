package com.chatbot.backend.domain.user.repository;

import com.chatbot.backend.domain.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRepository {

    void insert(User user);
    User findById(String id);
    User findByIdAndName(@Param("id") String id, @Param("name") String name);
    boolean existsById(String id);
    boolean existsByRole(String role);
    void update(User user);
    void deleteById(String id);
}
