-- spots 테이블 필터링 및 공간 검색 최적화를 위한 인덱스 추가

-- 1. 지역/분류 필터링용 복합 인덱스
CREATE INDEX idx_spots_filter ON spots (area_code, sigungu_code, content_type_id);

-- 2. "이 위치에서 검색" 범위(위경도) 탐색용 인덱스
CREATE INDEX idx_spots_coords ON spots (mapx, mapy);
