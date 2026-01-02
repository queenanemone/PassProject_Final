package com.tripboard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardComment {
    private Long commentId;
    private Long parentCommentId; // 대댓글인 경우 부모 댓글 ID
    private Long postId;
    private Integer commentOrder; // 댓글 순서
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 작성자 정보 (JOIN으로 가져옴)
    private String authorNickname;
    private String authorName;
    private String authorProfileImage;
    // 대댓글 리스트
    private java.util.List<BoardComment> replies;
}

