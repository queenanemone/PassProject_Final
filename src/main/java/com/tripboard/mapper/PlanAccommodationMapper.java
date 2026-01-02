package com.tripboard.mapper;

import com.tripboard.entity.PlanAccommodation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PlanAccommodationMapper {
    List<PlanAccommodation> findByPlanId(Long planId);

    void insert(PlanAccommodation accommodation);

    void updateOrderAndDate(
            @Param("planId") Long planId,
            @Param("oldCheckInDate") LocalDate oldCheckInDate,
            @Param("oldAccommodationOrder") int oldAccommodationOrder,
            @Param("newCheckInDate") LocalDate newCheckInDate,
            @Param("newAccommodationOrder") int newAccommodationOrder);

    void deleteAccommodation(
            @Param("planId") Long planId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("accommodationOrder") int accommodationOrder);

    void deleteByPlanId(Long planId);
}