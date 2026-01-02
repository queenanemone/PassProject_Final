-- 더미 댓글 데이터 수정 스크립트
-- 이 스크립트는 실제 존재하는 post_id를 사용하여 댓글을 추가합니다.

USE travel_planner_korea;

-- 1단계: 현재 게시물 확인
SELECT '=== 현재 게시물 확인 ===' AS 'Status';
SELECT post_id, title, user_id, category 
FROM board_posts 
WHERE user_id BETWEEN 11 AND 18 
ORDER BY post_id;

-- 2단계: 위 결과를 확인한 후, 아래 SQL을 실제 post_id에 맞게 수정하여 실행하세요.
-- 예를 들어, post_id가 21부터 시작한다면 모든 post_id 값에 20을 더하세요.

-- 만약 게시물이 전혀 없다면:
-- 1. dummy_board_posts.sql을 먼저 실행하세요
-- 2. 그 다음 이 스크립트의 1단계를 다시 실행하여 실제 post_id를 확인하세요
-- 3. dummy_board_comments.sql의 모든 post_id 값을 실제 값으로 수정하세요

-- 또는 간단하게: 
-- dummy_board_posts.sql 실행 → 생성된 post_id 확인 → dummy_board_comments.sql의 post_id 값 수정 → dummy_board_comments.sql 실행

