-- user_access_log 장비 정보 컬럼 (기존 DB 마이그레이션)
-- USE spring_boot_app;

ALTER TABLE user_access_log
  ADD COLUMN IF NOT EXISTS device_type_cd VARCHAR(20) NULL;

ALTER TABLE user_access_log
  ADD COLUMN IF NOT EXISTS device_os VARCHAR(80) NULL;

ALTER TABLE user_access_log
  ADD COLUMN IF NOT EXISTS device_browser VARCHAR(80) NULL;

ALTER TABLE user_access_log
  ADD COLUMN IF NOT EXISTS device_model VARCHAR(120) NULL;
