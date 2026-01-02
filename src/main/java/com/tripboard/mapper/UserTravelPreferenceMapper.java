package com.tripboard.mapper;

import com.tripboard.entity.UserTravelPreference;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserTravelPreferenceMapper {
    UserTravelPreference findByUserId(Long userId);
    void insert(UserTravelPreference preference);
    void update(UserTravelPreference preference);
    void delete(Long userId);
}

