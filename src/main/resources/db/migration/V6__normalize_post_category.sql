-- 기존 값을 ASCII 안전 영문 enum으로 정규화
UPDATE posts SET category = 'FREE' WHERE category NOT IN ('FREE', 'QUESTION') OR category IS NULL OR category = '';
ALTER TABLE posts MODIFY COLUMN category VARCHAR(20) CHARACTER SET ascii NOT NULL DEFAULT 'FREE';
