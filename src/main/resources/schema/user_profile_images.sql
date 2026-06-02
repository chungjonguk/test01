-- 사용자 프로필·커버 이미지 URL (설정 화면)
-- USE spring_boot_app;

ALTER TABLE "user" ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500) NULL;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(500) NULL;
