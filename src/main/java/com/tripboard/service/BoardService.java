package com.tripboard.service;

import com.tripboard.entity.BoardPost;
import com.tripboard.entity.BoardComment;
import com.tripboard.entity.PostLike;
import com.tripboard.entity.TravelPlan;
import com.tripboard.mapper.BoardPostMapper;
import com.tripboard.mapper.BoardCommentMapper;
import com.tripboard.mapper.PostLikeMapper;
import com.tripboard.service.TravelPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardPostMapper boardPostMapper;
    private final BoardCommentMapper boardCommentMapper;
    private final PostLikeMapper postLikeMapper;
    private final TravelPlanService travelPlanService;

    /**
     * 게시글 목록 조회
     */
    public List<BoardPost> getPublicPosts(String category, String regionCode, String tripType, String season, Long userId) {
        // 쉼표로 구분된 문자열을 리스트로 변환
        // 예: "32,33,34" -> ["32", "33", "34"]
        // 예: "33,34" -> ["33", "34"] (충청의 경우)
        java.util.Set<String> regionCodeSet = new java.util.HashSet<>();
        if (regionCode != null && !regionCode.isEmpty()) {
            String[] codes = regionCode.split(",");
            for (String code : codes) {
                String trimmed = code.trim();
                if (!trimmed.isEmpty()) {
                    regionCodeSet.add(trimmed);
                }
            }
        }
        java.util.List<String> regionCodeList = regionCodeSet.isEmpty() ? new java.util.ArrayList<>() : new java.util.ArrayList<>(regionCodeSet);
        
        java.util.Set<String> tripTypeSet = new java.util.HashSet<>();
        if (tripType != null && !tripType.isEmpty()) {
            String[] types = tripType.split(",");
            for (String type : types) {
                String trimmed = type.trim();
                if (!trimmed.isEmpty()) {
                    tripTypeSet.add(trimmed);
                }
            }
        }
        java.util.List<String> tripTypeList = tripTypeSet.isEmpty() ? new java.util.ArrayList<>() : new java.util.ArrayList<>(tripTypeSet);
        
        java.util.Set<String> seasonSet = new java.util.HashSet<>();
        if (season != null && !season.isEmpty()) {
            String[] seasons = season.split(",");
            for (String s : seasons) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    seasonSet.add(trimmed);
                }
            }
        }
        java.util.List<String> seasonList = seasonSet.isEmpty() ? new java.util.ArrayList<>() : new java.util.ArrayList<>(seasonSet);
        
        // 디버깅용 로그
        System.out.println("필터 파라미터 - category: " + category);
        System.out.println("  regionCode (원본): " + regionCode);
        System.out.println("  regionCodeList: " + regionCodeList);
        System.out.println("  regionCodeList size: " + (regionCodeList != null ? regionCodeList.size() : "null"));
        if (regionCodeList != null && !regionCodeList.isEmpty()) {
            System.out.println("  regionCodeList 내용: " + java.util.Arrays.toString(regionCodeList.toArray()));
        }
        
        List<BoardPost> posts = boardPostMapper.findPublicPosts(category, regionCodeList, tripTypeList, seasonList);
        
        // 현재 사용자가 각 게시글에 좋아요를 눌렀는지 확인
        if (userId != null && posts != null) {
            for (BoardPost post : posts) {
                PostLike like = postLikeMapper.findByPostIdAndUserId(post.getPostId(), userId);
                post.setLiked(like != null);
            }
        } else if (posts != null) {
            for (BoardPost post : posts) {
                post.setLiked(false);
            }
        }
        
        return posts;
    }

    /**
     * 사용자가 작성한 게시글 조회
     */
    public List<BoardPost> getUserPosts(Long userId) {
        return boardPostMapper.findByUserId(userId);
    }

    /**
     * 사용자가 좋아요한 게시글 조회
     */
    public List<BoardPost> getLikedPosts(Long userId) {
        return boardPostMapper.findLikedPosts(userId);
    }

    /**
     * 사용자가 댓글을 단 게시글 조회
     */
    public List<BoardPost> getCommentedPosts(Long userId) {
        return boardPostMapper.findCommentedPosts(userId);
    }

    /**
     * 게시글 상세 조회
     */
    public BoardPost getPost(Long postId, Long userId) {
        BoardPost post = boardPostMapper.findById(postId);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            boardPostMapper.update(post);
            
            // 현재 사용자가 좋아요를 눌렀는지 확인
            if (userId != null) {
                PostLike like = postLikeMapper.findByPostIdAndUserId(postId, userId);
                post.setLiked(like != null);
            } else {
                post.setLiked(false);
            }
        }
        return post;
    }

    /**
     * 게시글 작성
     */
    @Transactional
    public BoardPost createPost(Long userId, Long planId, Long hotplaceId, String title, String content,
            String regionCode, String tripType, String season, String category) {
        // 여행 계획이 연결된 경우, 공유용 스냅샷 생성
        Long sharedPlanId = planId;
        if (planId != null && planId > 0 && "TRAVEL_PLAN".equals(category)) {
            try {
                TravelPlan sharedPlan = travelPlanService.copyPlanForShare(planId);
                sharedPlanId = sharedPlan.getPlanId();
            } catch (Exception e) {
                // 복사 실패 시 원본 planId 사용
                System.err.println("계획 복사 실패, 원본 사용: " + e.getMessage());
            }
        }
        
        BoardPost post = BoardPost.builder()
                .userId(userId)
                .planId(sharedPlanId != null && sharedPlanId > 0 ? sharedPlanId : null)
                .hotplaceId(hotplaceId != null && hotplaceId > 0 ? hotplaceId : null)
                .title(title)
                .content(content)
                .regionCode(regionCode)
                .tripType(tripType)
                .season(season)
                .category(category)
                .viewCount(0)
                .likeCount(0)
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        boardPostMapper.insert(post);
        return post;
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public void updatePost(Long postId, String title, String content) {
        BoardPost post = boardPostMapper.findById(postId);
        if (post != null) {
            post.setTitle(title);
            post.setContent(content);
            post.setUpdatedAt(LocalDateTime.now());
            boardPostMapper.update(post);
        }
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long postId) {
        boardPostMapper.delete(postId);
    }

    /**
     * 좋아요 토글
     * @return 좋아요 후 업데이트된 정보 [likeCount, liked]를 담은 Map
     */
    @Transactional
    public java.util.Map<String, Object> toggleLike(Long postId, Long userId) {
        BoardPost post = boardPostMapper.findById(postId);
        if (post == null) {
            throw new RuntimeException("게시글을 찾을 수 없습니다");
        }
        
        PostLike existingLike = postLikeMapper.findByPostIdAndUserId(postId, userId);
        
        // likeCount가 null일 수 있으므로 0으로 초기화
        Integer currentLikeCount = post.getLikeCount();
        if (currentLikeCount == null) {
            currentLikeCount = 0;
        }

        boolean isLiked;
        if (existingLike != null) {
            postLikeMapper.delete(postId, userId);
            currentLikeCount = Math.max(0, currentLikeCount - 1);
            post.setLikeCount(currentLikeCount);
            isLiked = false;
        } else {
            PostLike like = PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();
            postLikeMapper.insert(like);
            currentLikeCount = currentLikeCount + 1;
            post.setLikeCount(currentLikeCount);
            isLiked = true;
        }

        boardPostMapper.update(post);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("likeCount", currentLikeCount);
        result.put("liked", isLiked);
        return result;
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public BoardComment addComment(Long postId, Long userId, String content, Long parentCommentId) {
        // 댓글 순서 계산: 해당 게시글의 댓글 개수 + 1
        int commentCount = boardCommentMapper.countByPostId(postId);
        int commentOrder = commentCount + 1;
        
        BoardComment comment = BoardComment.builder()
                .postId(postId)
                .userId(userId)
                .content(content)
                .parentCommentId(parentCommentId)
                .commentOrder(commentOrder)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        boardCommentMapper.insert(comment);
        return boardCommentMapper.findById(comment.getCommentId());
    }

    /**
     * 댓글 목록 조회
     */
    public List<BoardComment> getComments(Long postId) {
        List<BoardComment> parentComments = boardCommentMapper.findByPostId(postId);

        for (BoardComment parentComment : parentComments) {
            List<BoardComment> replies = boardCommentMapper.findByParentCommentId(parentComment.getCommentId());
            parentComment.setReplies(replies != null ? replies : new java.util.ArrayList<>());
        }

        return parentComments;
    }

    /**
     * 댓글 단건 조회
     */
    public BoardComment getComment(Long commentId) {
        return boardCommentMapper.findById(commentId);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public void updateComment(Long commentId, String content) {
        BoardComment comment = boardCommentMapper.findById(commentId);
        if (comment != null) {
            comment.setContent(content);
            comment.setUpdatedAt(LocalDateTime.now());
            boardCommentMapper.update(comment);
        }
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId) {
        boardCommentMapper.delete(commentId);
    }
}
