-- 업체별 홈 대시보드 위젯 구성
-- USE spring_boot_app;

CREATE TABLE IF NOT EXISTS dashboard_company_config (
  company_id    BIGINT        NOT NULL,
  hidden_json   VARCHAR(4000) NOT NULL DEFAULT '[]',
  reg_id        VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id     VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (company_id),
  CONSTRAINT fk_dashboard_company_config_company
    FOREIGN KEY (company_id) REFERENCES biz_company (company_id) ON DELETE CASCADE
);
