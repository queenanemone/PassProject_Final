package com.tripboard.mapper;

import com.tripboard.entity.HotPlace;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HotPlaceMapper {
    HotPlace findById(Long hotplaceId);
    List<HotPlace> findByUserId(Long userId);
    List<HotPlace> findAll();
    void insert(HotPlace hotPlace);
    void update(HotPlace hotPlace);
    void delete(Long hotplaceId);
}

