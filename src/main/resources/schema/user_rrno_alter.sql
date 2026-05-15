-- 기존 DB: rrno 컬럼을 암호문 길이에 맞게 확장
USE spring_boot_app;

ALTER TABLE `user`
  MODIFY COLUMN `rrno` VARCHAR(255) NOT NULL COMMENT 'rrno (AES 암호문 저장)';
