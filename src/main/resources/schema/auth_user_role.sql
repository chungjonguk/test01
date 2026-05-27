-- 사용자 권한·업체 매핑
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS user_auth_profile (
  user_id     VARCHAR(100)  NOT NULL COMMENT 'user.id',
  role_cd     VARCHAR(30)   NOT NULL COMMENT 'PLATFORM_ADMIN|COMPANY_ADMIN|COMPANY_CUSTOMER',
  use_yn      CHAR(1)       NOT NULL DEFAULT 'Y',
  reg_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id   VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  KEY idx_user_auth_role (role_cd, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_company (
  user_id     VARCHAR(100)  NOT NULL,
  company_id  BIGINT        NOT NULL,
  use_yn      CHAR(1)       NOT NULL DEFAULT 'Y',
  reg_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id   VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, company_id),
  KEY idx_user_company_company (company_id, use_yn),
  CONSTRAINT fk_user_company_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_company_biz FOREIGN KEY (company_id) REFERENCES biz_company (company_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS company_customer_menu (
  company_id  BIGINT        NOT NULL,
  menu_path   VARCHAR(300)  NOT NULL COMMENT '논리 메뉴 경로 (data-menu-path)',
  use_yn      CHAR(1)       NOT NULL DEFAULT 'Y',
  sort_ord    INT           NOT NULL DEFAULT 0,
  reg_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id   VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (company_id, menu_path),
  CONSTRAINT fk_company_customer_menu_company FOREIGN KEY (company_id) REFERENCES biz_company (company_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
