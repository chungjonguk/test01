-- 화면(URL)별 연동 테이블 매핑
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS screen_table_map (
  uri_path        VARCHAR(255)  NOT NULL COMMENT 'URL 경로',
  screen_id       VARCHAR(100)  NULL COMMENT '화면ID(screen_list)',
  primary_table   VARCHAR(64)   NULL COMMENT '주 테이블(NULL=정적/미연동)',
  related_tables  VARCHAR(500)  NULL COMMENT '참조·상세 테이블(쉼표 구분)',
  data_type       CHAR(1)       NOT NULL DEFAULT 'S' COMMENT 'D=DB CRUD,C=공통코드만,S=정적 UI',
  table_desc      VARCHAR(500)  NULL COMMENT '화면별 테이블 설명',
  reg_id          VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (uri_path),
  KEY idx_screen_table_map_screen (screen_id),
  KEY idx_screen_table_map_primary (primary_table)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
