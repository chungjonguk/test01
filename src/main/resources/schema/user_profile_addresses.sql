-- 사용자 프로필 — 자택·직장 주소·전화, 기본 주소 구분
USE spring_boot_app;

ALTER TABLE `user` ADD COLUMN home_zipcode VARCHAR(10) NULL COMMENT '자택 우편번호' AFTER cover_image_url;
ALTER TABLE `user` ADD COLUMN home_address VARCHAR(255) NULL COMMENT '자택 기본주소' AFTER home_zipcode;
ALTER TABLE `user` ADD COLUMN home_address_detail VARCHAR(255) NULL COMMENT '자택 상세주소' AFTER home_address;
ALTER TABLE `user` ADD COLUMN home_phone VARCHAR(30) NULL COMMENT '자택 전화번호' AFTER home_address_detail;

ALTER TABLE `user` ADD COLUMN work_zipcode VARCHAR(10) NULL COMMENT '직장 우편번호' AFTER home_phone;
ALTER TABLE `user` ADD COLUMN work_address VARCHAR(255) NULL COMMENT '직장 기본주소' AFTER work_zipcode;
ALTER TABLE `user` ADD COLUMN work_address_detail VARCHAR(255) NULL COMMENT '직장 상세주소' AFTER work_address;
ALTER TABLE `user` ADD COLUMN work_phone VARCHAR(30) NULL COMMENT '직장 전화번호' AFTER work_address_detail;

ALTER TABLE `user` ADD COLUMN primary_address_type VARCHAR(10) NOT NULL DEFAULT 'HOME'
  COMMENT '기본 주소 HOME|WORK' AFTER work_phone;

UPDATE `user`
SET home_zipcode = zipcode,
    home_address = address,
    home_address_detail = address_detail,
    primary_address_type = 'HOME'
WHERE (home_zipcode IS NULL OR home_zipcode = '')
  AND zipcode IS NOT NULL
  AND zipcode != '';
