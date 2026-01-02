package com.tripboard.mapper;

import com.tripboard.entity.HotPlaceImage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HotPlaceImageMapper {
    List<HotPlaceImage> findByHotplaceId(Long hotplaceId);
    void insert(HotPlaceImage image);
    void deleteByHotplaceId(Long hotplaceId);
}

