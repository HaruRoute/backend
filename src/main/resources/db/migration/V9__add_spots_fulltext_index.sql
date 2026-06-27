-- title 키워드 검색 최적화: LIKE '%keyword%' 풀스캔 → FULLTEXT ngram 인덱스
-- ngram_token_size=2 (MySQL 기본값) 기준, 2글자 이상 한국어 검색 최적화
ALTER TABLE spots ADD FULLTEXT INDEX idx_spots_title_ft (title) WITH PARSER ngram;
