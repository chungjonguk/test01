-- common_code 테이블 한글 깨짐 방지 (utf8mb4)
ALTER TABLE common_code CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE common_code_value CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
