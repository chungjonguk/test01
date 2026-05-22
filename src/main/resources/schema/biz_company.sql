-- 업체(거래처) 마스터 — 관리자 > 업체관리
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS biz_company (
  company_id    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '업체ID',
  company_nm    VARCHAR(200)  NOT NULL COMMENT '업체명',
  biz_no        VARCHAR(20)   NULL COMMENT '사업자등록번호',
  ceo_nm        VARCHAR(100)  NULL COMMENT '대표자명',
  tel           VARCHAR(30)   NULL COMMENT '전화번호',
  email         VARCHAR(200)  NULL COMMENT '이메일',
  address       VARCHAR(500)  NULL COMMENT '주소',
  status_cd     VARCHAR(30)   NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|INACTIVE',
  use_yn        CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '사용여부 Y/N',
  memo          VARCHAR(1000) NULL COMMENT '비고',
  reg_id        VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id     VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (company_id),
  KEY idx_biz_company_nm (company_nm),
  KEY idx_biz_company_status (status_cd),
  KEY idx_biz_company_use (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
