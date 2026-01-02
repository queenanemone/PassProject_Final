-- users 테이블의 profile_image 컬럼 타입 확인 및 수정
-- 이 스크립트는 현재 컬럼 타입을 확인하고 필요시 수정합니다

USE travel_planner_korea;

-- 현재 컬럼 타입 확인
SHOW COLUMNS FROM users WHERE Field = 'profile_image';

-- 컬럼 타입을 TEXT로 변경
ALTER TABLE users 
MODIFY COLUMN profile_image TEXT;

-- 변경 확인
SHOW COLUMNS FROM users WHERE Field = 'profile_image';

