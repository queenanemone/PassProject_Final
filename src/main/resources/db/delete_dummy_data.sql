-- 더미 데이터 삭제 스크립트
USE travel_planner_korea;

-- ⚠️ 주의: 이 스크립트는 user_id 11~18의 게시물과 관련 댓글을 모두 삭제합니다.

-- 방법 1: Safe Update Mode 비활성화 후 삭제 (추천)
SET SQL_SAFE_UPDATES = 0;

-- 댓글 삭제 (게시물 삭제 전에 먼저 삭제)
DELETE FROM board_comments 
WHERE post_id IN (
    SELECT post_id FROM (
        SELECT post_id FROM board_posts WHERE user_id BETWEEN 11 AND 18
    ) AS temp
);

-- 게시물 삭제
DELETE FROM board_posts WHERE user_id BETWEEN 11 AND 18;

-- Safe Update Mode 다시 활성화
SET SQL_SAFE_UPDATES = 1;

-- 삭제 확인
SELECT '=== 삭제 후 게시물 확인 ===' AS 'Status';
SELECT COUNT(*) AS remaining_posts FROM board_posts WHERE user_id BETWEEN 11 AND 18;

SELECT '=== 삭제 후 댓글 확인 ===' AS 'Status';
SELECT COUNT(*) AS remaining_comments FROM board_comments;

