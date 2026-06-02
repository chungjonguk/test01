-- 기존 DB: rrno 컬럼을 암호문 길이에 맞게 확장
-- PostgreSQL은 USE 미지원 — 접속 시 DB가 지정됨
-- USE spring_boot_app;

-- MySQL MODIFY COLUMN → PostgreSQL ALTER COLUMN 분리
ALTER TABLE "user" ALTER COLUMN rrno TYPE VARCHAR(255);
ALTER TABLE "user" ALTER COLUMN rrno SET NOT NULL;
