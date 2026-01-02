package com.tripboard.mapper;

import com.tripboard.entity.BoardComment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BoardCommentMapper {
    List<BoardComment> findByPostId(Long postId);
    List<BoardComment> findByParentCommentId(Long parentCommentId);
    BoardComment findById(Long commentId);
    int countByPostId(Long postId);
    void insert(BoardComment comment);
    void update(BoardComment comment);
    void delete(Long commentId);
}
