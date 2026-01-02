-- record_images 테이블의 image_url을 TEXT 타입으로 변경하는 마이그레이션
-- base64 이미지 데이터는 VARCHAR(500)으로는 저장할 수 없으므로 TEXT로 변경

USE travel_planner_korea;

-- image_url 컬럼을 TEXT 타입으로 변경
ALTER TABLE record_images MODIFY COLUMN image_url TEXT;

