-- 테이블별 PK 난수 채번 설정 (기존 AUTO_INCREMENT·시퀀스와 병행 가능)
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS sys_table_random_id (
  config_name    VARCHAR(100)  NOT NULL COMMENT '설정명(기본=테이블명)',
  table_name     VARCHAR(100)  NOT NULL COMMENT '대상 테이블',
  column_name    VARCHAR(100)  NOT NULL COMMENT 'PK 컬럼명',
  id_type_cd     CHAR(1)       NOT NULL DEFAULT 'N' COMMENT 'N=숫자(long), S=문자열',
  min_val        BIGINT        NOT NULL DEFAULT 1000000000000 COMMENT '숫자ID 최소값',
  max_val        BIGINT        NOT NULL DEFAULT 9999999999999 COMMENT '숫자ID 최대값',
  string_length  INT           NULL COMMENT '문자열ID 길이',
  max_retry      INT           NOT NULL DEFAULT 25 COMMENT '중복 시 재시도 횟수',
  description    VARCHAR(255)  NULL COMMENT '설명',
  use_yn         CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (config_name),
  UNIQUE KEY uk_sys_table_random_id_tbl_col (table_name, column_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='테이블별 난수 ID';
