-- 기존 DB: 주소 컬럼 추가
USE spring_boot_app;

ALTER TABLE `user`
  ADD COLUMN `zipcode` VARCHAR(10) NULL COMMENT '우편번호' AFTER `email`,
  ADD COLUMN `address` VARCHAR(255) NULL COMMENT '기본주소' AFTER `zipcode`,
  ADD COLUMN `address_detail` VARCHAR(255) NULL COMMENT '상세주소' AFTER `address`;
