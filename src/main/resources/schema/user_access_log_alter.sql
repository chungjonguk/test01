-- user_access_log 장비 정보 컬럼 (기존 DB 마이그레이션)
USE spring_boot_app;

ALTER TABLE user_access_log
  ADD COLUMN device_type_cd VARCHAR(20) NULL COMMENT '장비유형 DESKTOP|MOBILE|TABLET|UNKNOWN' AFTER client_ip;

ALTER TABLE user_access_log
  ADD COLUMN device_os VARCHAR(80) NULL COMMENT '장비 OS' AFTER device_type_cd;

ALTER TABLE user_access_log
  ADD COLUMN device_browser VARCHAR(80) NULL COMMENT '브라우저' AFTER device_os;

ALTER TABLE user_access_log
  ADD COLUMN device_model VARCHAR(120) NULL COMMENT '장비 모델명' AFTER device_browser;
