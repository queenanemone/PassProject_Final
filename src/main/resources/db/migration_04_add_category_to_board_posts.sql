-- board_posts 테이블에 category 컬럼 추가 마이그레이션

USE travel_planner_korea;

-- category 컬럼 추가
ALTER TABLE board_posts 
ADD COLUMN category VARCHAR(50) DEFAULT 'TRAVEL_PLAN' AFTER is_public;

-- 기존 데이터는 모두 TRAVEL_PLAN으로 설정
UPDATE board_posts SET category = 'TRAVEL_PLAN' WHERE category IS NULL;

-- 인덱스 추가 (선택사항)
ALTER TABLE board_posts ADD INDEX idx_category (category);

