-- hotplaces 테이블 및 hotplace_images 테이블 생성 마이그레이션

USE travel_planner_korea;

-- hotplaces 테이블 생성
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

-- hotplace_images 테이블 생성
CREATE TABLE IF NOT EXISTS hotplace_images (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotplace_id BIGINT NOT NULL,
    image_order INT NOT NULL,
    image_url TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hotplace_id) REFERENCES hotplaces(hotplace_id) ON DELETE CASCADE,
    INDEX idx_hotplace_id (hotplace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

