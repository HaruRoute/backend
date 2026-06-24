package com.chatbot.backend.domain.favorite.repository;

import com.chatbot.backend.domain.favorite.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteRepository {

    void insert(Favorite favorite);
    List<Favorite> findByUserIdOrderByCreatedAtDesc(String userId);
    boolean existsByUserIdAndPlaceName(@Param("userId") String userId, @Param("placeName") String placeName);
    Favorite findByIdAndUserId(@Param("id") Long id, @Param("userId") String userId);
    void delete(Long id);
}
