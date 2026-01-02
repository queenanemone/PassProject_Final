-- board_posts 테이블에 hotplace_id 컬럼 추가 마이그레이션

USE travel_planner_korea;

-- hotplace_id 컬럼 추가
ALTER TABLE board_posts 
ADD COLUMN hotplace_id BIGINT AFTER plan_id;

-- Foreign Key 추가
ALTER TABLE board_posts 
ADD CONSTRAINT fk_board_posts_hotplace 
FOREIGN KEY (hotplace_id) REFERENCES hotplaces(hotplace_id) ON DELETE SET NULL;

-- 인덱스 추가
ALTER TABLE board_posts ADD INDEX idx_hotplace_id (hotplace_id);

