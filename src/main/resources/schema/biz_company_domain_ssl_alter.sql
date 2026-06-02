-- 업체 도메인 SSL 인증서 유효기간 컬럼 (기존 DB 마이그레이션)
-- USE spring_boot_app;

ALTER TABLE biz_company_domain
  ADD COLUMN IF NOT EXISTS ssl_cert_not_before TIMESTAMP NULL;

ALTER TABLE biz_company_domain
  ADD COLUMN IF NOT EXISTS ssl_cert_not_after TIMESTAMP NULL;

ALTER TABLE biz_company_domain
  ADD COLUMN IF NOT EXISTS ssl_cert_subject VARCHAR(500) NULL;

ALTER TABLE biz_company_domain
  ADD COLUMN IF NOT EXISTS ssl_cert_issuer VARCHAR(500) NULL;

ALTER TABLE biz_company_domain
  ADD COLUMN IF NOT EXISTS ssl_cert_file_id BIGINT NULL;
