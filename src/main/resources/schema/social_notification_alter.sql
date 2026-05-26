USE spring_boot_app;

ALTER TABLE social_notification
  ADD COLUMN sender_nm VARCHAR(100) NULL COMMENT '발신 표시명' AFTER user_nm;

ALTER TABLE social_notification
  ADD COLUMN section_cd VARCHAR(20) NULL DEFAULT 'NEW' COMMENT 'NEW, EARLIER' AFTER message;

ALTER TABLE social_notification
  ADD COLUMN time_icon VARCHAR(30) NULL COMMENT '시간 아이콘(이모지 등)' AFTER section_cd;
