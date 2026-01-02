-- 게시판 댓글 더미 데이터
USE travel_planner_korea;

-- ⚠️ 중요: 이 스크립트를 실행하기 전에 반드시 아래 SQL을 먼저 실행하세요!
-- 1. dummy_users.sql 실행 (user_id 11~18 생성)
-- 2. dummy_board_posts.sql 실행 (게시물 생성)
-- 3. 이 파일(dummy_board_comments.sql) 실행

-- 게시물이 존재하는지 확인 (실행 전에 확인하세요)
-- SELECT post_id, title FROM board_posts WHERE user_id BETWEEN 11 AND 18 ORDER BY post_id;
-- 위 쿼리 결과가 없으면 dummy_board_posts.sql을 먼저 실행하세요!

-- 참고: board_posts의 실제 post_id 값들을 사용합니다.
-- post_id 매핑 (게시물 생성 순서 기준):
-- 1: 부산 3박 4일 여행 계획 공유
-- 13: 제주도 4박 5일 로맨틱 여행
-- 11: 서울 한 달 살기 완벽 가이드
-- 7: 전주 한옥마을 2박 3일 여행
-- 2: 강릉 커피거리와 안목해변
-- 12: 제주도 여행 후기
-- 5: 부산 해운대에서의 특별한 하루
-- 18: 가을 단풍 여행 - 설악산 후기
-- 10: 전주 한옥마을에서 보낸 전통문화 체험
-- 6: 강릉 커피거리 투어
-- 14: 서울 성수동 카페거리 핫플
-- 17: 제주 협재해수욕장
-- 3: 부산 감천문화마을
-- 15: 강릉 안목해변 카페거리
-- 4: 전주 한옥마을 - 전통과 현대의 만남
-- 16: 서울 이태원
-- 9: 제주 성산일출봉
-- 8: 부산 광안리 해수욕장

-- post_id 1 (부산 3박 4일 여행 계획 공유) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(1, 1, 12, NULL, '정말 좋은 계획이네요! 부산 가면 꼭 따라해볼게요 😊', '2024-03-15 11:00:00', '2024-03-15 11:00:00'),
(1, 2, 13, NULL, '감천문화마을 정말 추천합니다! 사진 찍기 좋아요 📸', '2024-03-15 12:30:00', '2024-03-15 12:30:00'),
(1, 3, 12, NULL, '네! 감천문화마을도 계획에 포함시켰어요. 기대되네요!', '2024-03-15 13:00:00', '2024-03-15 13:00:00'),
(1, 4, 14, NULL, '해운대 맛집 추천 부탁드려요!', '2024-03-16 09:20:00', '2024-03-16 09:20:00');

-- post_id 13 (제주도 4박 5일 로맨틱 여행) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(13, 1, 16, NULL, '커플 여행에 완벽한 코스네요! 다음 달에 가볼 예정이에요 💕', '2024-06-20 15:00:00', '2024-06-20 15:00:00'),
(13, 2, 11, NULL, '성산일출봉 일출 정말 장관이에요! 일찍 일어나세요!', '2024-06-20 16:30:00', '2024-06-20 16:30:00'),
(13, 3, 17, NULL, '협재해수욕장 물이 정말 맑아요. 수영하기 좋아요!', '2024-06-21 10:15:00', '2024-06-21 10:15:00');

-- post_id 11 (서울 한 달 살기 완벽 가이드) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(11, 1, 15, NULL, '한 달 살기 정말 부럽네요! 서울 생활 팁 더 알려주세요 🙏', '2024-09-10 10:00:00', '2024-09-10 10:00:00'),
(11, 2, 18, NULL, '홍대 맛집 리스트도 공유해주시면 좋을 것 같아요!', '2024-09-10 14:20:00', '2024-09-10 14:20:00'),
(11, 3, 13, NULL, '이태원도 추천합니다! 다양한 음식 문화를 경험할 수 있어요', '2024-09-11 09:30:00', '2024-09-11 09:30:00');

-- post_id 7 (전주 한옥마을 2박 3일 여행) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(7, 1, 12, NULL, '전주 비빔밥 정말 맛있어요! 한옥 스테이도 추천합니다 🏡', '2024-10-05 17:30:00', '2024-10-05 17:30:00'),
(7, 2, 16, NULL, '가을에 가면 더 예쁠 것 같아요. 단풍 구경도 할 수 있어요!', '2024-10-06 11:00:00', '2024-10-06 11:00:00');

-- post_id 2 (강릉 커피거리와 안목해변) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(2, 1, 14, NULL, '안목해변 카페에서 일몰 보는 게 최고예요! 🌅', '2024-07-12 12:00:00', '2024-07-12 12:00:00'),
(2, 2, 18, NULL, '커피거리 카페 추천 부탁드려요!', '2024-07-12 15:30:00', '2024-07-12 15:30:00'),
(2, 3, 15, NULL, '안목해변 카페거리 정말 좋아요! 바다 보며 커피 마시기 최고', '2024-07-13 09:20:00', '2024-07-13 09:20:00');

-- post_id 12 (제주도 여행 후기) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(12, 1, 12, NULL, '일출 정말 멋지네요! 다음에 꼭 가보고 싶어요 🌅', '2024-08-01 21:00:00', '2024-08-01 21:00:00'),
(12, 2, 15, NULL, '협재해수욕장 물이 정말 투명하네요!', '2024-08-02 10:30:00', '2024-08-02 10:30:00'),
(12, 3, 11, NULL, '제주도 여행 정말 좋아요! 또 가고 싶어요', '2024-08-02 14:15:00', '2024-08-02 14:15:00');

-- post_id 5 (부산 해운대에서의 특별한 하루) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(5, 1, 13, NULL, '해운대 해수욕장 산책 정말 좋아요! 저도 자주 가요', '2024-04-18 20:00:00', '2024-04-18 20:00:00'),
(5, 2, 14, NULL, '광안리 저녁 식사 분위기 정말 좋죠!', '2024-04-19 11:30:00', '2024-04-19 11:30:00');

-- post_id 18 (가을 단풍 여행 - 설악산 후기) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(18, 1, 16, NULL, '설악산 단풍 정말 아름다워요! 가을 여행 추천합니다 🍂', '2024-10-25 16:00:00', '2024-10-25 16:00:00'),
(18, 2, 17, NULL, '등반하시느라 고생 많으셨어요! 정상에서 본 풍경 장관이겠어요', '2024-10-25 18:30:00', '2024-10-25 18:30:00'),
(18, 3, 12, NULL, '다음 가을에 꼭 가보고 싶어요!', '2024-10-26 09:15:00', '2024-10-26 09:15:00');

-- post_id 10 (전주 한옥마을에서 보낸 전통문화 체험) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(10, 1, 15, NULL, '한복 입어보기 정말 좋은 경험이었을 것 같아요! 👘', '2024-10-12 13:00:00', '2024-10-12 13:00:00'),
(10, 2, 11, NULL, '전주 비빔밥 정말 맛있죠! 한정식도 추천합니다', '2024-10-12 15:45:00', '2024-10-12 15:45:00');

-- post_id 6 (강릉 커피거리 투어) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(6, 1, 15, NULL, '커피 애호가로서 강릉 커피거리 정말 좋아요! ☕', '2024-07-28 18:30:00', '2024-07-28 18:30:00'),
(6, 2, 14, NULL, '안목해변 카페에서 바다 보며 커피 마시기 최고예요!', '2024-07-29 10:00:00', '2024-07-29 10:00:00');

-- post_id 14 (서울 성수동 카페거리 핫플) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(14, 1, 14, NULL, '성수동 카페 정말 트렌디해요! 사진 찍기 좋아요 📸', '2024-04-02 14:00:00', '2024-04-02 14:00:00'),
(14, 2, 18, NULL, '카페 추천 부탁드려요!', '2024-04-02 16:30:00', '2024-04-02 16:30:00'),
(14, 3, 13, NULL, '성수동 카페거리 정말 좋아요! 주말에 사람 많으니 평일에 가세요', '2024-04-03 09:20:00', '2024-04-03 09:20:00');

-- post_id 17 (제주 협재해수욕장) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(17, 1, 16, NULL, '협재해수욕장 물이 정말 투명해요! 여름에 가면 최고예요 🌊', '2024-07-05 11:00:00', '2024-07-05 11:00:00'),
(17, 2, 11, NULL, '주변 카페도 많아서 좋아요!', '2024-07-05 13:30:00', '2024-07-05 13:30:00'),
(17, 3, 12, NULL, '다음 여름에 꼭 가보고 싶어요!', '2024-07-06 10:15:00', '2024-07-06 10:15:00');

-- post_id 3 (부산 감천문화마을) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(3, 1, 11, NULL, '감천문화마을 정말 컬러풀해요! 사진 찍기 최고의 장소예요', '2024-03-28 15:00:00', '2024-03-28 15:00:00'),
(3, 2, 14, NULL, '부산의 산토리니라고 불리는 곳이죠! 정말 예뻐요', '2024-03-28 17:30:00', '2024-03-28 17:30:00');

-- post_id 15 (강릉 안목해변 카페거리) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(15, 1, 15, NULL, '안목해변 카페거리 정말 좋아요! 일몰 시간대 추천합니다 🌅', '2024-08-10 17:00:00', '2024-08-10 17:00:00'),
(15, 2, 18, NULL, '바다 보며 커피 마시기 최고예요!', '2024-08-11 09:30:00', '2024-08-11 09:30:00');

-- post_id 4 (전주 한옥마을 - 전통과 현대의 만남) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(4, 1, 12, NULL, '전주 한옥마을 정말 아름다워요! 전통과 현대가 잘 어우러져요', '2024-09-22 12:30:00', '2024-09-22 12:30:00'),
(4, 2, 16, NULL, '한옥 카페도 많아서 좋아요!', '2024-09-22 15:00:00', '2024-09-22 15:00:00');

-- post_id 16 (서울 이태원) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(16, 1, 13, NULL, '이태원 정말 다양한 음식 문화를 경험할 수 있어요! 🌍', '2024-10-08 19:00:00', '2024-10-08 19:00:00'),
(16, 2, 18, NULL, '레스토랑 추천 부탁드려요!', '2024-10-09 11:20:00', '2024-10-09 11:20:00');

-- post_id 9 (제주 성산일출봉) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(9, 1, 12, NULL, '성산일출봉 일출 정말 장관이에요! 새벽에 일어나는 게 힘들지만 가치 있어요 🌅', '2024-08-15 07:00:00', '2024-08-15 07:00:00'),
(9, 2, 16, NULL, '일출 보기 위해 일찍 일어나는 게 정말 힘들지만, 본 순간 모든 게 사라져요!', '2024-08-15 09:30:00', '2024-08-15 09:30:00'),
(9, 3, 11, NULL, '정상에서 본 풍경 정말 아름다워요!', '2024-08-15 14:20:00', '2024-08-15 14:20:00');

-- post_id 8 (부산 광안리 해수욕장) 댓글
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) VALUES
(8, 1, 13, NULL, '광안리 야경 정말 멋져요! 해변가 레스토랑에서 저녁 먹기 최고예요', '2024-07-20 21:30:00', '2024-07-20 21:30:00'),
(8, 2, 14, NULL, '밤에 가면 정말 예뻐요! 불꽃놀이도 볼 수 있어요', '2024-07-21 10:00:00', '2024-07-21 10:00:00');

-- 대댓글 더미 데이터
-- 참고: 위의 INSERT 문들이 실행된 후 comment_id가 자동 생성되므로, 아래 대댓글들은 각 게시물의 첫 번째 댓글에 대한 대댓글입니다.
-- 실제 사용 시에는 comment_order를 적절히 증가시켜야 합니다.

-- post_id 13의 첫 번째 댓글에 대한 대댓글 (커플 여행 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 13, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 13), 12, 
       (SELECT comment_id FROM board_comments WHERE post_id = 13 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '저도 다음 달에 가볼 예정이에요! 혹시 숙소 추천받을 수 있을까요?', '2024-06-20 16:00:00', '2024-06-20 16:00:00';

-- post_id 11의 첫 번째 댓글에 대한 대댓글 (한 달 살기 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 11, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 11), 13,
       (SELECT comment_id FROM board_comments WHERE post_id = 11 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '서울 생활 팁은 블로그에 자세히 정리해둘게요! 곧 업로드할 예정입니다 😊', '2024-09-10 11:00:00', '2024-09-10 11:00:00';

-- post_id 7의 첫 번째 댓글에 대한 대댓글 (전주 한옥마을 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 7, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 7), 14,
       (SELECT comment_id FROM board_comments WHERE post_id = 7 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '전주 비빔밥 정말 최고죠! 한옥 스테이는 어떤 곳 추천하시나요?', '2024-10-05 18:00:00', '2024-10-05 18:00:00';

-- post_id 2의 첫 번째 댓글에 대한 대댓글 (안목해변 카페 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 2, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 2), 16,
       (SELECT comment_id FROM board_comments WHERE post_id = 2 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '일몰 시간대 정말 추천합니다! 사진도 너무 예쁘게 나와요 🌅', '2024-07-12 13:00:00', '2024-07-12 13:00:00';

-- post_id 12의 첫 번째 댓글에 대한 대댓글 (제주도 여행 후기 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 12, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 12), 16,
       (SELECT comment_id FROM board_comments WHERE post_id = 12 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '네! 일출 정말 장관이에요. 일찍 일어나서 가보시면 후회 안 하실 거예요!', '2024-08-01 22:00:00', '2024-08-01 22:00:00';

-- post_id 5의 첫 번째 댓글에 대한 대댓글 (부산 해운대 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 5, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 5), 11,
       (SELECT comment_id FROM board_comments WHERE post_id = 5 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '저도 해운대 자주 가요! 아침 산책이 정말 좋더라고요 🌊', '2024-04-18 21:00:00', '2024-04-18 21:00:00';

-- post_id 18의 첫 번째 댓글에 대한 대댓글 (설악산 후기 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 18, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 18), 17,
       (SELECT comment_id FROM board_comments WHERE post_id = 18 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '가을 단풍 정말 최고죠! 다음 가을에도 또 가고 싶어요 🍂', '2024-10-25 17:00:00', '2024-10-25 17:00:00';

-- post_id 10의 첫 번째 댓글에 대한 대댓글 (전주 한옥마을 체험 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 10, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 10), 12,
       (SELECT comment_id FROM board_comments WHERE post_id = 10 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '한복 입어보기는 정말 특별한 경험이었어요! 사진도 예쁘게 나왔답니다 📸', '2024-10-12 14:00:00', '2024-10-12 14:00:00';

-- post_id 14의 첫 번째 댓글에 대한 대댓글 (성수동 카페 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 14, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 14), 13,
       (SELECT comment_id FROM board_comments WHERE post_id = 14 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '사진 찍기 정말 좋은 카페들이 많아요! 인스타에 올리기 좋은 곳들이에요 📸', '2024-04-02 15:00:00', '2024-04-02 15:00:00';

-- post_id 17의 첫 번째 댓글에 대한 대댓글 (협재해수욕장 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 17, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 17), 12,
       (SELECT comment_id FROM board_comments WHERE post_id = 17 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '여름에 가면 정말 최고예요! 물도 맑고 수영하기 좋아요 🌊', '2024-07-05 12:00:00', '2024-07-05 12:00:00';

-- post_id 3의 첫 번째 댓글에 대한 대댓글 (감천문화마을 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 3, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 3), 15,
       (SELECT comment_id FROM board_comments WHERE post_id = 3 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '정말 컬러풀한 곳이죠! 사진 찍으러 가기 최고의 장소예요 📷', '2024-03-28 16:00:00', '2024-03-28 16:00:00';

-- post_id 15의 첫 번째 댓글에 대한 대댓글 (안목해변 카페거리 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 15, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 15), 16,
       (SELECT comment_id FROM board_comments WHERE post_id = 15 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '일몰 시간대 정말 추천합니다! 로맨틱한 분위기가 최고예요 🌅', '2024-08-10 18:00:00', '2024-08-10 18:00:00';

-- post_id 4의 첫 번째 댓글에 대한 대댓글 (전주 한옥마을 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 4, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 4), 17,
       (SELECT comment_id FROM board_comments WHERE post_id = 4 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '전통과 현대가 정말 잘 어우러져 있죠! 한옥 카페 분위기가 좋더라고요', '2024-09-22 13:30:00', '2024-09-22 13:30:00';

-- post_id 16의 첫 번째 댓글에 대한 대댓글 (이태원 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 16, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 16), 11,
       (SELECT comment_id FROM board_comments WHERE post_id = 16 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '이태원에 정말 다양한 음식이 있죠! 어떤 음식을 좋아하시나요?', '2024-10-08 20:00:00', '2024-10-08 20:00:00';

-- post_id 9의 첫 번째 댓글에 대한 대댓글 (성산일출봉 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 9, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 9), 11,
       (SELECT comment_id FROM board_comments WHERE post_id = 9 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '새벽에 일어나는 게 힘들지만 일출 보는 순간 모든 게 사라져요! 정말 장관이에요 🌅', '2024-08-15 08:00:00', '2024-08-15 08:00:00';

-- post_id 8의 첫 번째 댓글에 대한 대댓글 (광안리 해수욕장 댓글에 답변)
INSERT INTO board_comments (post_id, comment_order, user_id, parent_comment_id, content, created_at, updated_at) 
SELECT 8, (SELECT COUNT(*) + 1 FROM board_comments WHERE post_id = 8), 12,
       (SELECT comment_id FROM board_comments WHERE post_id = 8 AND parent_comment_id IS NULL ORDER BY created_at ASC LIMIT 1),
       '야경 정말 멋지죠! 해변가 레스토랑에서 저녁 먹는 게 최고예요 🌃', '2024-07-20 22:30:00', '2024-07-20 22:30:00';

