package com.chatbot.backend.domain.route.repository;

import com.chatbot.backend.domain.route.entity.TravelPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TravelPlanRepository {
    void insert(TravelPlan travelPlan);
    List<TravelPlan> findByUserId(String userId);
    TravelPlan findById(Long id);
    TravelPlan findByIdAndUserId(@Param("id") Long id, @Param("userId") String userId);
    void update(TravelPlan travelPlan);
    void deleteById(@Param("id") Long id, @Param("userId") String userId);
}
