-- Trip-Board 데이터베이스 스키마 (최종 배포 버전)
CREATE DATABASE IF NOT EXISTS travel_planner_korea CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE travel_planner_korea;

-- 기존 테이블 삭제 (순서 중요: Foreign Key 의존성 역순)
DROP TABLE IF EXISTS ai_recommendations;
DROP TABLE IF EXISTS bookmarks;
DROP TABLE IF EXISTS post_likes;
DROP TABLE IF EXISTS board_comments;
DROP TABLE IF EXISTS user_travel_preferences;
DROP TABLE IF EXISTS board_posts;
DROP TABLE IF EXISTS hotplace_images;
DROP TABLE IF EXISTS hotplaces;
DROP TABLE IF EXISTS record_images;
DROP TABLE IF EXISTS travel_records;
DROP TABLE IF EXISTS plan_transports;
DROP TABLE IF EXISTS plan_accommodations;
DROP TABLE IF EXISTS plan_spots;
DROP TABLE IF EXISTS cache_railway_station;
DROP TABLE IF EXISTS cache_tour_api;
DROP TABLE IF EXISTS travel_plans;
DROP TABLE IF EXISTS templates;
DROP TABLE IF EXISTS users;

-- 1. 사용자 테이블 (users)
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    nickname VARCHAR(100),
    phone VARCHAR(20),
    profile_image TEXT,
    bio TEXT,
    role VARCHAR(20) DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 템플릿 테이블 (templates)
CREATE TABLE IF NOT EXISTS templates (
    template_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    structure TEXT,
    is_default BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 여행 계획 테이블 (travel_plans)
CREATE TABLE IF NOT EXISTS travel_plans (
    plan_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200),
    departure_region_code VARCHAR(10),
    arrival_region_code VARCHAR(10),
    departure_date DATE,
    arrival_date DATE,
    adult_count INT DEFAULT 1,
    child_count INT DEFAULT 0,
    has_pet BOOLEAN DEFAULT FALSE,
    template_id BIGINT,
    is_public BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES templates(template_id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_is_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 한국관광공사 API 캐시 테이블 (cache_tour_api)
CREATE TABLE IF NOT EXISTS cache_tour_api (
    content_id VARCHAR(50) PRIMARY KEY,
    content_type_id VARCHAR(10),
    title VARCHAR(500),
    area_code VARCHAR(10),
    sigungu_code VARCHAR(10),
    cat1 VARCHAR(10),
    cat2 VARCHAR(10),
    cat3 VARCHAR(10),
    addr1 VARCHAR(500),
    addr2 VARCHAR(500),
    map_x VARCHAR(50),
    map_y VARCHAR(50),
    first_image VARCHAR(500),
    first_image2 VARCHAR(500),
    overview TEXT,
    is_pet_friendly BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_area_code (area_code),
    INDEX idx_content_type (content_type_id),
    INDEX idx_pet_friendly (is_pet_friendly)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 기차역 목록 API 캐시 테이블 (cache_railway_station)
CREATE TABLE IF NOT EXISTS cache_railway_station (
    station_code VARCHAR(10) PRIMARY KEY,
    station_name VARCHAR(100) NOT NULL UNIQUE,
    city_code VARCHAR(10),
    city_name VARCHAR(100),
    line_name VARCHAR(100),
    region_code VARCHAR(10),
    cached_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_station_name (station_name),
    INDEX idx_city_code (city_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 계획에 포함된 관광지 (plan_spots) - [이름 통일]
CREATE TABLE IF NOT EXISTS plan_spots (
    plan_id BIGINT NOT NULL,
    spot_order INT NOT NULL, -- DnD 순서 및 식별자
    content_id VARCHAR(50),
    visit_date DATE,
    visit_time TIME,
    duration INT,
    is_selected BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (plan_id, spot_order), 
    FOREIGN KEY (plan_id) REFERENCES travel_plans(plan_id) ON DELETE CASCADE,
    FOREIGN KEY (content_id) REFERENCES cache_tour_api(content_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 계획에 포함된 숙소 (plan_accommodations)
CREATE TABLE IF NOT EXISTS plan_accommodations (
    plan_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    accommodation_order INT NOT NULL,
    content_id VARCHAR(50),
    check_out_date DATE,
    is_selected BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (plan_id, check_in_date, accommodation_order),
    FOREIGN KEY (plan_id) REFERENCES travel_plans(plan_id) ON DELETE CASCADE,
    FOREIGN KEY (content_id) REFERENCES cache_tour_api(content_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 교통 정보 (plan_transports) - [이름 통일]
CREATE TABLE IF NOT EXISTS plan_transports (
    plan_id BIGINT NOT NULL,
    transport_order INT NOT NULL, -- DnD 순서 및 식별자
    transport_type VARCHAR(50),
    departure_station_code VARCHAR(10),
    arrival_station_code VARCHAR(10),
    departure_time DATETIME,
    arrival_time DATETIME,
    price INT,
    reservation_url VARCHAR(500),
    is_selected BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (plan_id, transport_order),
    FOREIGN KEY (plan_id) REFERENCES travel_plans(plan_id) ON DELETE CASCADE,
    FOREIGN KEY (departure_station_code) REFERENCES cache_railway_station(station_code) ON DELETE SET NULL,
    FOREIGN KEY (arrival_station_code) REFERENCES cache_railway_station(station_code) ON DELETE SET NULL,
    INDEX idx_departure_time (departure_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 여행 기록 (travel_records)
CREATE TABLE IF NOT EXISTS travel_records (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT,
    title VARCHAR(200),
    content TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (plan_id) REFERENCES travel_plans(plan_id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 여행 기록 이미지 (record_images)
CREATE TABLE IF NOT EXISTS record_images (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL,
    image_order INT NOT NULL,
    image_url LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (record_id) REFERENCES travel_records(record_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. 핫플레이스 (hotplaces)
CREATE TABLE IF NOT EXISTS hotplaces (
    hotplace_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    address VARCHAR(500),
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    content_id VARCHAR(50), -- 한국관광공사 API content_id (선택)
    content_type_id VARCHAR(10),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_latitude_longitude (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. 핫플레이스 이미지 (hotplace_images)
CREATE TABLE IF NOT EXISTS hotplace_images (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotplace_id BIGINT NOT NULL,
    image_order INT NOT NULL,
    image_url TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hotplace_id) REFERENCES hotplaces(hotplace_id) ON DELETE CASCADE,
    INDEX idx_hotplace_id (hotplace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. 게시판 게시글 (board_posts)
CREATE TABLE IF NOT EXISTS board_posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT,
    hotplace_id BIGINT,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT,
    region_code VARCHAR(10),
    trip_type VARCHAR(50),
    season VARCHAR(20),
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    is_public BOOLEAN DEFAULT TRUE,
    category VARCHAR(50) DEFAULT 'TRAVEL_PLAN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES travel_plans(plan_id) ON DELETE SET NULL,
    FOREIGN KEY (hotplace_id) REFERENCES hotplaces(hotplace_id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_region_code (region_code),
    INDEX idx_is_public (is_public),
    INDEX idx_hotplace_id (hotplace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. 사용자 여행 성향 (user_travel_preferences)
CREATE TABLE IF NOT EXISTS user_travel_preferences (
    user_id BIGINT PRIMARY KEY,
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
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. 게시판 댓글 (board_comments) - [구조 단순화]
CREATE TABLE IF NOT EXISTS board_comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 단일 PK로 변경
    post_id BIGINT NOT NULL,
    comment_order INT NOT NULL, 
    user_id BIGINT NOT NULL,
    
    parent_comment_id BIGINT,  -- 대댓글 참조 (단일 컬럼)
    
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES board_posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- 대댓글 구현을 위한 단순 Self-Reference
    FOREIGN KEY (parent_comment_id) REFERENCES board_comments(comment_id) ON DELETE CASCADE,
    INDEX idx_post_id_order (post_id, comment_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. 좋아요 (post_likes)
CREATE TABLE IF NOT EXISTS post_likes (
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (post_id, user_id),
    FOREIGN KEY (post_id) REFERENCES board_posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. 북마크 (bookmarks)
CREATE TABLE IF NOT EXISTS bookmarks (
    user_id BIGINT NOT NULL,
    bookmark_id INT NOT NULL,
    plan_id BIGINT,
    post_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, bookmark_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES travel_plans(plan_id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES board_posts(post_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. AI 추천 (ai_recommendations)
CREATE TABLE IF NOT EXISTS ai_recommendations (
    recommendation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    recommendation_type VARCHAR(50),
    content_id VARCHAR(50),
    title VARCHAR(200),
    description TEXT,
    reason TEXT,
    address VARCHAR(255),
    map_x VARCHAR(50),
    map_y VARCHAR(50),
    image_keyword VARCHAR(100),
    is_preference_updated BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES travel_plans(plan_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_plan_id (plan_id),
    INDEX idx_type (recommendation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 기본 템플릿 데이터 삽입
INSERT INTO templates (name, description, category, structure, is_default) VALUES
('기본 템플릿', '기차, 숙소, 관광지가 포함된 기본 템플릿', 'default', '{"sections":["transportation","accommodation","destination"]}', TRUE),
('가족 여행', '가족 여행에 최적화된 템플릿', 'family', '{"sections":["transportation","accommodation","destination","family"]}', FALSE),
('반려동물 동반', '반려동물과 함께하는 여행 템플릿', 'pet', '{"sections":["transportation","accommodation","destination","pet"]}', FALSE);