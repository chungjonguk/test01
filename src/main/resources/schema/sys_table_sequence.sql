-- 테이블별 PK 채번용 시퀀스 마스터 (MySQL: AUTO_INCREMENT 대체·병행 사용 가능)
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS sys_table_sequence (
  seq_name       VARCHAR(100)  NOT NULL COMMENT '시퀀스명(기본=테이블명)',
  table_name     VARCHAR(100)  NOT NULL COMMENT '대상 테이블',
  column_name    VARCHAR(100)  NOT NULL COMMENT 'PK 컬럼명',
  next_val       BIGINT        NOT NULL DEFAULT 0 COMMENT '다음 채번 기준값(할당 후 증가)',
  increment_by   INT           NOT NULL DEFAULT 1 COMMENT '증가값',
  min_val        BIGINT        NOT NULL DEFAULT 1 COMMENT '최소 할당값',
  max_val        BIGINT        NULL COMMENT '최대값(선택)',
  description    VARCHAR(255)  NULL COMMENT '설명',
  use_yn         CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (seq_name),
  UNIQUE KEY uk_sys_table_sequence_tbl_col (table_name, column_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='테이블별 시퀀스';
