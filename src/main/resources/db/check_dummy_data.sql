-- 더미 데이터 확인 및 재생성 스크립트
USE travel_planner_korea;

-- 1단계: 현재 상태 확인
SELECT '=== 게시물 확인 ===' AS 'Status';
SELECT post_id, title, user_id FROM board_posts WHERE user_id BETWEEN 11 AND 18 ORDER BY post_id;

SELECT '=== 댓글 확인 ===' AS 'Status';
SELECT COUNT(*) AS comment_count FROM board_comments;

-- 2단계: 기존 더미 데이터 삭제 (필요시 실행)
-- Safe Update Mode를 비활성화하거나 아래 방법을 사용하세요:

-- 방법 1: Safe Update Mode 비활성화 후 실행
-- SET SQL_SAFE_UPDATES = 0;
-- DELETE FROM board_comments WHERE post_id IN (SELECT post_id FROM (SELECT post_id FROM board_posts WHERE user_id BETWEEN 11 AND 18) AS temp);
-- DELETE FROM board_posts WHERE user_id BETWEEN 11 AND 18;
-- SET SQL_SAFE_UPDATES = 1;

-- 방법 2: post_id를 직접 지정하여 삭제 (더 안전)
-- DELETE FROM board_comments WHERE post_id BETWEEN 1 AND 18;
-- DELETE FROM board_posts WHERE post_id BETWEEN 1 AND 18 AND user_id BETWEEN 11 AND 18;

-- 3단계: 실행 순서
-- 1. dummy_users.sql 실행 (user_id 11~18 생성)
-- 2. dummy_board_posts.sql 실행 (post_id 1~18 생성)
-- 3. dummy_board_comments.sql 실행 (댓글 생성)

