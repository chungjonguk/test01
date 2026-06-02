-- 공통코드 마스터·상세 (신규 DB용)
-- USE spring_boot_app;

CREATE TABLE IF NOT EXISTS common_code (
  code_id      VARCHAR(50)   NOT NULL,
  code_nm      VARCHAR(200)  NOT NULL,
  use_yn       CHAR(1)       NOT NULL DEFAULT 'Y',
  reg_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  regdate_dt   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id    VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (code_id)
);

CREATE TABLE IF NOT EXISTS common_code_value (
  code_id      VARCHAR(50)   NOT NULL,
  code_val     VARCHAR(100)  NOT NULL,
  use_yn       CHAR(1)       NOT NULL DEFAULT 'Y',
  reg_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  regdate_dt   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id    VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (code_id, code_val),
  CONSTRAINT fk_common_code_value_group
    FOREIGN KEY (code_id) REFERENCES common_code (code_id) ON DELETE CASCADE
);
