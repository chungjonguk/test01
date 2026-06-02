-- 사용자 프로필 — 자택·직장 주소·전화, 기본 주소 구분
-- USE spring_boot_app;

ALTER TABLE "user" ADD COLUMN IF NOT EXISTS home_zipcode VARCHAR(10) NULL;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS home_address VARCHAR(255) NULL;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS home_address_detail VARCHAR(255) NULL;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS home_phone VARCHAR(30) NULL;

ALTER TABLE "user" ADD COLUMN IF NOT EXISTS work_zipcode VARCHAR(10) NULL;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS work_address VARCHAR(255) NULL;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS work_address_detail VARCHAR(255) NULL;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS work_phone VARCHAR(30) NULL;

ALTER TABLE "user" ADD COLUMN IF NOT EXISTS primary_address_type VARCHAR(10) NOT NULL DEFAULT 'HOME';

UPDATE "user"
SET home_zipcode = zipcode,
    home_address = address,
    home_address_detail = address_detail,
    primary_address_type = 'HOME'
WHERE (home_zipcode IS NULL OR home_zipcode = '')
  AND zipcode IS NOT NULL
  AND zipcode != '';
