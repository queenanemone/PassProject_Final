package com.tripboard.mapper;

import com.tripboard.entity.AiRecommendation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiRecommendationMapper {
    List<AiRecommendation> findByPlanId(Long planId);
    void insert(AiRecommendation recommendation);
    void delete(Long recommendationId);
    void deleteByPlanId(Long planId);
}

