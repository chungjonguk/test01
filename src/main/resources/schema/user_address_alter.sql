-- 기존 DB: 주소 컬럼 추가
-- PostgreSQL은 USE 미지원 — 접속 시 DB가 지정됨
-- USE spring_boot_app;

-- PostgreSQL은 컬럼 위치 지정(AFTER) 미지원 — AFTER 절 제거
ALTER TABLE "user"
  ADD COLUMN IF NOT EXISTS zipcode VARCHAR(10) NULL,
  ADD COLUMN IF NOT EXISTS address VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS address_detail VARCHAR(255) NULL;
