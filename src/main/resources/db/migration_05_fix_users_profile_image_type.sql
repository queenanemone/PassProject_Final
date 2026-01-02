-- users 테이블의 profile_image 컬럼을 VARCHAR(500)에서 TEXT로 변경
-- base64 인코딩된 이미지 URL을 저장하기 위해 필요

USE travel_planner_korea;

ALTER TABLE users 
MODIFY COLUMN profile_image TEXT;

