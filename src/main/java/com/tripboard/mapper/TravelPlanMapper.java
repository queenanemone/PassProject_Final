package com.tripboard.mapper;

import com.tripboard.entity.TravelPlan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TravelPlanMapper {
    TravelPlan findById(Long planId);
    List<TravelPlan> findByUserId(Long userId);
    List<TravelPlan> findPublicPlans();
    void insert(TravelPlan plan);
    void update(TravelPlan plan);
    void delete(Long planId);
}

