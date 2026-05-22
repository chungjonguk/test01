-- 공통코드 마스터·상세 (신규 DB용)
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS common_code (
  code_id      VARCHAR(50)   NOT NULL COMMENT '코드그룹ID',
  code_nm      VARCHAR(200)  NOT NULL COMMENT '코드그룹명',
  use_yn       CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  reg_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  regdate_dt   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id    VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (code_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS common_code_value (
  code_id      VARCHAR(50)   NOT NULL COMMENT '코드그룹ID',
  code_val     VARCHAR(100)  NOT NULL COMMENT '코드값',
  use_yn       CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  reg_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  regdate_dt   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id    VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (code_id, code_val),
  CONSTRAINT fk_common_code_value_group
    FOREIGN KEY (code_id) REFERENCES common_code (code_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
