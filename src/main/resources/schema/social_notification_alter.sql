-- USE spring_boot_app;

ALTER TABLE social_notification
  ADD COLUMN IF NOT EXISTS sender_nm VARCHAR(100) NULL;

ALTER TABLE social_notification
  ADD COLUMN IF NOT EXISTS section_cd VARCHAR(20) NULL DEFAULT 'NEW';

ALTER TABLE social_notification
  ADD COLUMN IF NOT EXISTS time_icon VARCHAR(30) NULL;
