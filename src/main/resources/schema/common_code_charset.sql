-- common_code 테이블 한글 깨짐 방지 (utf8mb4)
-- PostgreSQL은 테이블 단위 문자셋 변환(CONVERT TO CHARACTER SET)을 지원하지 않습니다.
-- 데이터베이스 인코딩(UTF8)으로 처리되므로 아래 MySQL 전용 구문은 비활성화합니다.
-- ALTER TABLE common_code CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- ALTER TABLE common_code_value CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SELECT 1;
