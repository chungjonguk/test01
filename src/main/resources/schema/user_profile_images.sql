-- 사용자 프로필·커버 이미지 URL (설정 화면)
USE spring_boot_app;

ALTER TABLE `user` ADD COLUMN profile_image_url VARCHAR(500) NULL COMMENT '프로필 이미지 공개 URL' AFTER address_detail;
ALTER TABLE `user` ADD COLUMN cover_image_url VARCHAR(500) NULL COMMENT '커버 이미지 공개 URL' AFTER profile_image_url;
