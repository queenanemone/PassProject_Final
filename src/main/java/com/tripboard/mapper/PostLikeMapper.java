package com.tripboard.mapper;

import com.tripboard.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeMapper {
    PostLike findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    void insert(PostLike like);
    void delete(@Param("postId") Long postId, @Param("userId") Long userId);
}

