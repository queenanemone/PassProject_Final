package com.tripboard.mapper;

import com.tripboard.entity.TravelRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TravelRecordMapper {
    TravelRecord findById(Long recordId);
    List<TravelRecord> findByPlanId(Long planId);
    List<TravelRecord> findByUserId(Long userId);
    void insert(TravelRecord record);
    void update(TravelRecord record);
    void delete(Long recordId);
}

