-- travel_records 테이블의 plan_id를 nullable로 변경하는 마이그레이션
-- 기존 테이블이 있는 경우 실행

USE travel_planner_korea;

-- 기존 외래 키 제약조건 제거
ALTER TABLE travel_records DROP FOREIGN KEY IF EXISTS travel_records_ibfk_1;

-- plan_id 컬럼을 nullable로 변경
ALTER TABLE travel_records MODIFY COLUMN plan_id BIGINT NULL;

-- 외래 키 제약조건 재생성 (ON DELETE SET NULL로)
ALTER TABLE travel_records 
ADD CONSTRAINT fk_travel_records_plan_id 
FOREIGN KEY (plan_id) REFERENCES travel_plans(plan_id) ON DELETE SET NULL;

