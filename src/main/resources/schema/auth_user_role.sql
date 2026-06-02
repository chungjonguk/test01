-- 사용자 권한·업체 매핑
-- USE spring_boot_app;

CREATE TABLE IF NOT EXISTS user_auth_profile (
  user_id     VARCHAR(100)  NOT NULL,
  role_cd     VARCHAR(30)   NOT NULL,
  use_yn      CHAR(1)       NOT NULL DEFAULT 'Y',
  reg_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id   VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id)
);
CREATE INDEX IF NOT EXISTS idx_user_auth_role ON user_auth_profile (role_cd, use_yn);

CREATE TABLE IF NOT EXISTS user_company (
  user_id     VARCHAR(100)  NOT NULL,
  company_id  BIGINT        NOT NULL,
  use_yn      CHAR(1)       NOT NULL DEFAULT 'Y',
  reg_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id   VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, company_id),
  CONSTRAINT fk_user_company_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_company_biz FOREIGN KEY (company_id) REFERENCES biz_company (company_id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_user_company_company ON user_company (company_id, use_yn);

CREATE TABLE IF NOT EXISTS company_customer_menu (
  company_id  BIGINT        NOT NULL,
  menu_path   VARCHAR(300)  NOT NULL,
  use_yn      CHAR(1)       NOT NULL DEFAULT 'Y',
  sort_ord    INTEGER       NOT NULL DEFAULT 0,
  reg_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id   VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (company_id, menu_path),
  CONSTRAINT fk_company_customer_menu_company FOREIGN KEY (company_id) REFERENCES biz_company (company_id) ON DELETE CASCADE
);
