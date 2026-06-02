-- 화면(URL)별 연동 테이블 매핑
-- USE spring_boot_app;

CREATE TABLE IF NOT EXISTS screen_table_map (
  uri_path        VARCHAR(255)  NOT NULL,
  screen_id       VARCHAR(100)  NULL,
  primary_table   VARCHAR(64)   NULL,
  related_tables  VARCHAR(500)  NULL,
  data_type       CHAR(1)       NOT NULL DEFAULT 'S',
  table_desc      VARCHAR(500)  NULL,
  reg_id          VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (uri_path)
);
CREATE INDEX IF NOT EXISTS idx_screen_table_map_screen ON screen_table_map (screen_id);
CREATE INDEX IF NOT EXISTS idx_screen_table_map_primary ON screen_table_map (primary_table);
