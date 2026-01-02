package com.tripboard.mapper;

import com.tripboard.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByEmail(String email);
    User findById(Long userId);
    void insert(User user);
    void update(User user);
    void updatePassword(User user);
    void delete(Long userId);
}

