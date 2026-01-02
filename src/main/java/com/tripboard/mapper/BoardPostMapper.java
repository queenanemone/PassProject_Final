package com.tripboard.mapper;

import com.tripboard.entity.BoardPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardPostMapper {
    BoardPost findById(Long postId);
    List<BoardPost> findPublicPosts(@Param("category") String category,
                                     @Param("regionCodeList") java.util.List<String> regionCodeList,
                                     @Param("tripTypeList") java.util.List<String> tripTypeList,
                                     @Param("seasonList") java.util.List<String> seasonList);
    List<BoardPost> findByUserId(@Param("userId") Long userId);
    List<BoardPost> findLikedPosts(@Param("userId") Long userId);
    List<BoardPost> findCommentedPosts(@Param("userId") Long userId);
    void insert(BoardPost post);
    void update(BoardPost post);
    void delete(Long postId);
}

