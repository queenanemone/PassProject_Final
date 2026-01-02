-- 원래 있던 스키마에서는 아마 없는 컬럼일 겁니다
-- 이걸 실행하거나, 아니면 스키마를 완전히 drop하고 schema.sql을 실행해도 됩니다
ALTER TABLE ai_recommendations ADD COLUMN address VARCHAR(255);
ALTER TABLE ai_recommendations ADD COLUMN image_keyword VARCHAR(100);