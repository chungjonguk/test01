-- 테이블별 PK 난수 채번 설정 (기존 AUTO_INCREMENT·시퀀스와 병행 가능)
-- USE spring_boot_app;

CREATE TABLE IF NOT EXISTS sys_table_random_id (
  config_name    VARCHAR(100)  NOT NULL,
  table_name     VARCHAR(100)  NOT NULL,
  column_name    VARCHAR(100)  NOT NULL,
  id_type_cd     CHAR(1)       NOT NULL DEFAULT 'N',
  min_val        BIGINT        NOT NULL DEFAULT 1000000000000,
  max_val        BIGINT        NOT NULL DEFAULT 9999999999999,
  string_length  INTEGER       NULL,
  max_retry      INTEGER       NOT NULL DEFAULT 25,
  description    VARCHAR(255)  NULL,
  use_yn         CHAR(1)       NOT NULL DEFAULT 'Y',
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (config_name),
  CONSTRAINT uk_sys_table_random_id_tbl_col UNIQUE (table_name, column_name)
);
