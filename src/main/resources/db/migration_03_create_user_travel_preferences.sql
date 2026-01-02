-- user_travel_preferences 테이블 생성 마이그레이션

USE travel_planner_korea;

CREATE TABLE IF NOT EXISTS user_travel_preferences (
    preference_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    preferred_trip_type VARCHAR(50),
    preferred_activities TEXT,
    budget_preference VARCHAR(50),
    accommodation_preference VARCHAR(50),
    season_preference VARCHAR(50),
    transportation_preference VARCHAR(50),
    food_preference VARCHAR(50),
    travel_style VARCHAR(50),
    additional_info TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

