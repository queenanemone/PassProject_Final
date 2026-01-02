package com.tripboard.mapper;

import com.tripboard.entity.PlanTransport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PlanTransportMapper {
    List<PlanTransport> findByPlanId(Long planId);

    void insert(PlanTransport transportation);

    int findMaxTransportOrderIndex(Long planId);

    void updateOrderAndDate(
            @Param("planId") Long planId,
            @Param("oldOrder") int oldOrder,
            @Param("newOrder") int newOrder,
            @Param("newDate") LocalDate newDate);

    void deleteTransport(@Param("planId") Long planId, @Param("transportOrder") int transportOrder);

    void deleteByPlanId(Long planId);
}