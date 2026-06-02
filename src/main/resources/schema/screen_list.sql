-- 화면 마스터 (신규 DB용). 기존 테이블이 있으면 screen_list_alter.sql 사용
-- USE spring_boot_app;

CREATE TABLE IF NOT EXISTS screen_list (
  screen_id      VARCHAR(100)  NOT NULL,
  screen_nm      VARCHAR(200)  NOT NULL,
  uri_path       VARCHAR(255)  NOT NULL,
  template_path  VARCHAR(255)  NULL,
  sort_ord       INTEGER       NOT NULL DEFAULT 0,
  use_yn         CHAR(1)       NOT NULL DEFAULT 'Y',
  reg_id         VARCHAR(100)  NOT NULL,
  reg_dt         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id      VARCHAR(100)  NOT NULL,
  update_dt      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (screen_id),
  CONSTRAINT uk_screen_list_uri UNIQUE (uri_path)
);
