package com.tripboard.controller;

import com.tripboard.dto.ApiResponse;
import com.tripboard.dto.BoardPostRequest;
import com.tripboard.entity.BoardComment;
import com.tripboard.entity.BoardPost;
import com.tripboard.service.BoardService;
import com.tripboard.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시판 관련 REST API Controller
 * Spring Boot 표준 구조
 */
@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final SecurityUtil securityUtil;

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<BoardPost>>> getPublicPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String tripType,
            @RequestParam(required = false) String season) {
        try {
            Long userId = securityUtil.getCurrentUserId(); // 로그인한 사용자 ID 가져오기 (없으면 null)
            List<BoardPost> posts = boardService.getPublicPosts(category, regionCode, tripType, season, userId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<BoardPost>> getPost(@PathVariable Long id) {
        try {
            Long userId = securityUtil.getCurrentUserId(); // 로그인한 사용자 ID 가져오기 (없으면 null)
            BoardPost post = boardService.getPost(id, userId);
            if (post == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("게시글을 찾을 수 없습니다"));
            }
            return ResponseEntity.ok(ApiResponse.success(post));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<BoardPost>> createPost(@RequestBody BoardPostRequest request) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            BoardPost post = boardService.createPost(
                    userId,
                    request.getPlanId(),
                    request.getHotplaceId(),
                    request.getTitle(),
                    request.getContent(),
                    request.getRegionCode(),
                    request.getTripType(),
                    request.getSeason(),
                    request.getCategory());
            return ResponseEntity.ok(ApiResponse.success("게시글이 작성되었습니다", post));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<ApiResponse<?>> toggleLike(@PathVariable Long id) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            java.util.Map<String, Object> result = boardService.toggleLike(id, userId);
            return ResponseEntity.ok(ApiResponse.success("좋아요가 처리되었습니다", result));
        } catch (Exception e) {
            e.printStackTrace(); // 디버깅을 위한 스택 트레이스 출력
            String errorMessage = e.getMessage() != null ? e.getMessage() : "좋아요 처리 중 오류가 발생했습니다";
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<ApiResponse<BoardComment>> addComment(
            @PathVariable Long id,
            @RequestParam(required = false) String content,
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            String commentContent = null;
            Long parentCommentId = null;

            if (StringUtils.hasText(content)) {
                commentContent = content;
            } else if (body != null) {
                if (body.containsKey("content")) {
                    commentContent = body.get("content").toString();
                }
                if (body.containsKey("parentCommentId")) {
                    Object parentIdObj = body.get("parentCommentId");
                    if (parentIdObj != null) {
                        if (parentIdObj instanceof Number) {
                            parentCommentId = ((Number) parentIdObj).longValue();
                        } else {
                            parentCommentId = Long.parseLong(parentIdObj.toString());
                        }
                    }
                }
            }

            if (!StringUtils.hasText(commentContent)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("댓글 내용을 입력해주세요"));
            }

            BoardComment comment = boardService.addComment(id, userId, commentContent, parentCommentId);
            return ResponseEntity.ok(ApiResponse.success("댓글이 작성되었습니다", comment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<ApiResponse<List<BoardComment>>> getComments(@PathVariable Long id) {
        try {
            List<BoardComment> comments = boardService.getComments(id);
            return ResponseEntity.ok(ApiResponse.success(comments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<BoardPost>> updatePost(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            BoardPost post = boardService.getPost(id, userId);
            if (post == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("게시글을 찾을 수 없습니다"));
            }

            if (!post.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("본인이 작성한 글만 수정할 수 있습니다"));
            }

            String title = body.get("title");
            String content = body.get("content");

            if (!StringUtils.hasText(title)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("제목을 입력해주세요"));
            }

            if (!StringUtils.hasText(content)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("내용을 입력해주세요"));
            }

            boardService.updatePost(id, title, content);
            BoardPost updatedPost = boardService.getPost(id, userId);
            return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다", updatedPost));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<?>> deletePost(@PathVariable Long id) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            BoardPost post = boardService.getPost(id, userId);
            if (post == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("게시글을 찾을 수 없습니다"));
            }

            if (!post.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("본인이 작성한 글만 삭제할 수 있습니다"));
            }

            boardService.deletePost(id);
            return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/posts/my")
    public ResponseEntity<ApiResponse<List<BoardPost>>> getMyPosts() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            List<BoardPost> posts = boardService.getUserPosts(userId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/posts/liked")
    public ResponseEntity<ApiResponse<List<BoardPost>>> getLikedPosts() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            List<BoardPost> posts = boardService.getLikedPosts(userId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/posts/commented")
    public ResponseEntity<ApiResponse<List<BoardPost>>> getCommentedPosts() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            List<BoardPost> posts = boardService.getCommentedPosts(userId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<BoardComment>> updateComment(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            BoardComment comment = boardService.getComment(id);
            if (comment == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("댓글을 찾을 수 없습니다"));
            }

            if (!comment.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("본인이 작성한 댓글만 수정할 수 있습니다"));
            }

            String content = body.get("content");
            if (!StringUtils.hasText(content)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("댓글 내용을 입력해주세요"));
            }

            boardService.updateComment(id, content);
            BoardComment updatedComment = boardService.getComment(id);
            return ResponseEntity.ok(ApiResponse.success("댓글이 수정되었습니다", updatedComment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<?>> deleteComment(@PathVariable Long id) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            BoardComment comment = boardService.getComment(id);
            if (comment == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("댓글을 찾을 수 없습니다"));
            }

            if (!comment.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("본인이 작성한 댓글만 삭제할 수 있습니다"));
            }

            boardService.deleteComment(id);
            return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
