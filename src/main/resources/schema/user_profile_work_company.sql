-- 사용자 프로필 — 직장명
-- USE spring_boot_app;

ALTER TABLE "user" ADD COLUMN IF NOT EXISTS work_company_name VARCHAR(200) NULL;
