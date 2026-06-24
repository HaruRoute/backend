package com.chatbot.backend.domain.spot.repository;

import com.chatbot.backend.domain.spot.entity.Spot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SpotRepository {
    List<Spot> findSpots(
            @Param("areaCode") String areaCode,
            @Param("sigunguCode") String sigunguCode,
            @Param("contentTypeId") String contentTypeId,
            @Param("minX") Double minX,
            @Param("maxX") Double maxX,
            @Param("minY") Double minY,
            @Param("maxY") Double maxY,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset,
            @Param("keyword") String keyword
    );
    int countAll();
    void upsert(Spot spot);
    void upsertBatch(List<Spot> spots);
}
