-- 사용자 더미 데이터 (user_id 11번부터)
USE travel_planner_korea;

-- 참고: 비밀번호는 모두 'password123'으로 설정되어 있으며, BCrypt로 해시화된 값입니다.
-- 로그인 시 이메일과 비밀번호 'password123'을 사용하시면 됩니다.
-- 비밀번호 해시: password123 -> $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO users (user_id, email, password, name, nickname, phone, profile_image, bio, role, is_active, created_at, updated_at) VALUES
(11, 'user11@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '김민수', '민수', '010-1111-1111', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=400&fit=crop', '여행을 좋아하는 사람입니다. 국내 여행 전문가!', 'USER', TRUE, '2024-01-15 10:00:00', '2024-01-15 10:00:00'),
(12, 'user12@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '이지영', '지영', '010-2222-2222', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&h=400&fit=crop', '맛집 탐방과 카페 투어를 좋아해요 ☕', 'USER', TRUE, '2024-02-20 14:30:00', '2024-02-20 14:30:00'),
(13, 'user13@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '박준호', '준호', '010-3333-3333', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&h=400&fit=crop', '자연을 사랑하는 백패커입니다 🌲', 'USER', TRUE, '2024-03-10 09:15:00', '2024-03-10 09:15:00'),
(14, 'user14@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '최수진', '수진', '010-4444-4444', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400&h=400&fit=crop', '사진 찍는 것을 좋아하는 여행러 📸', 'USER', TRUE, '2024-04-05 16:45:00', '2024-04-05 16:45:00'),
(15, 'user15@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '정태영', '태영', '010-5555-5555', 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400&h=400&fit=crop', '해외 여행보다 국내 여행이 더 좋아요!', 'USER', TRUE, '2024-05-12 11:20:00', '2024-05-12 11:20:00'),
(16, 'user16@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '한소희', '소희', '010-6666-6666', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&h=400&fit=crop', '커플 여행 전문가 💕 로맨틱한 여행지를 추천합니다', 'USER', TRUE, '2024-06-18 13:50:00', '2024-06-18 13:50:00'),
(17, 'user17@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '윤도현', '도현', '010-7777-7777', 'https://images.unsplash.com/photo-1507591064344-4c6cef03d54d?w=400&h=400&fit=crop', '가족 여행을 계획하는 아빠입니다 👨‍👩‍👧‍👦', 'USER', TRUE, '2024-07-25 15:30:00', '2024-07-25 15:30:00'),
(18, 'user18@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '강미라', '미라', '010-8888-8888', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&h=400&fit=crop', '혼자 떠나는 여행도 좋아해요 ✈️', 'USER', TRUE, '2024-08-30 10:00:00', '2024-08-30 10:00:00');

