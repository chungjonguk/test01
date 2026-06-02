-- 테이블별 PK 채번용 시퀀스 마스터 (MySQL: AUTO_INCREMENT 대체·병행 사용 가능)
-- USE spring_boot_app;

CREATE TABLE IF NOT EXISTS sys_table_sequence (
  seq_name       VARCHAR(100)  NOT NULL,
  table_name     VARCHAR(100)  NOT NULL,
  column_name    VARCHAR(100)  NOT NULL,
  next_val       BIGINT        NOT NULL DEFAULT 0,
  increment_by   INTEGER       NOT NULL DEFAULT 1,
  min_val        BIGINT        NOT NULL DEFAULT 1,
  max_val        BIGINT        NULL,
  description    VARCHAR(255)  NULL,
  use_yn         CHAR(1)       NOT NULL DEFAULT 'Y',
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (seq_name),
  CONSTRAINT uk_sys_table_sequence_tbl_col UNIQUE (table_name, column_name)
);
