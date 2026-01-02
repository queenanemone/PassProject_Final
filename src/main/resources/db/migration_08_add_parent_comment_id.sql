-- 대댓글 기능을 위한 parent_comment_id 컬럼 추가
ALTER TABLE board_comments 
ADD COLUMN parent_comment_id BIGINT NULL AFTER comment_id,
ADD FOREIGN KEY (parent_comment_id) REFERENCES board_comments(comment_id) ON DELETE CASCADE,
ADD INDEX idx_parent_comment_id (parent_comment_id);

