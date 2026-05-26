-- 업체별 접속 도메인 등록
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS biz_company_domain (
  domain_id       BIGINT        NOT NULL AUTO_INCREMENT COMMENT '도메인ID',
  company_id      BIGINT        NOT NULL COMMENT '업체ID',
  host_name       VARCHAR(253)  NOT NULL COMMENT '호스트명(프로토콜 제외)',
  primary_yn      CHAR(1)       NOT NULL DEFAULT 'N' COMMENT '대표도메인 Y/N',
  ssl_yn          CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT 'HTTPS 사용 Y/N',
  ssl_cert_not_before DATETIME  NULL COMMENT 'SSL 인증서 유효 시작',
  ssl_cert_not_after  DATETIME  NULL COMMENT 'SSL 인증서 유효 만료',
  ssl_cert_subject    VARCHAR(500) NULL COMMENT 'SSL 인증서 Subject DN',
  ssl_cert_issuer     VARCHAR(500) NULL COMMENT 'SSL 인증서 Issuer DN',
  ssl_cert_file_id    BIGINT    NULL COMMENT 'NAS SSL 인증서 파일 ID',
  verify_status_cd VARCHAR(30)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|VERIFIED|FAILED',
  use_yn          CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  memo            VARCHAR(500)  NULL COMMENT '비고',
  reg_id          VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (domain_id),
  UNIQUE KEY uk_biz_company_domain_host (host_name),
  KEY idx_biz_company_domain_company (company_id),
  KEY idx_biz_company_domain_use (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
