package com.tripboard.mapper;

import com.tripboard.entity.PlanSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // @Param 사용을 위해 추가
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PlanSpotMapper {
    List<PlanSpot> findByPlanId(Long planId);

    void insert(PlanSpot spot);

    void updateOrderAndDate(
            @Param("planId") Long planId,
            @Param("oldSpotOrder") int oldSpotOrder,
            @Param("newSpotOrder") int newSpotOrder,
            @Param("newVisitDate") LocalDate newVisitDate);

    void deleteSpot(@Param("planId") Long planId, @Param("spotOrder") int spotOrder);

    void deleteByPlanId(Long planId);
}