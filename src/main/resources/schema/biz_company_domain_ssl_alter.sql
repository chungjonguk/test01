-- 업체 도메인 SSL 인증서 유효기간 컬럼 (기존 DB 마이그레이션)
USE spring_boot_app;

ALTER TABLE biz_company_domain
  ADD COLUMN ssl_cert_not_before DATETIME NULL COMMENT 'SSL 인증서 유효 시작' AFTER ssl_yn;

ALTER TABLE biz_company_domain
  ADD COLUMN ssl_cert_not_after DATETIME NULL COMMENT 'SSL 인증서 유효 만료' AFTER ssl_cert_not_before;

ALTER TABLE biz_company_domain
  ADD COLUMN ssl_cert_subject VARCHAR(500) NULL COMMENT 'SSL 인증서 Subject DN' AFTER ssl_cert_not_after;

ALTER TABLE biz_company_domain
  ADD COLUMN ssl_cert_issuer VARCHAR(500) NULL COMMENT 'SSL 인증서 Issuer DN' AFTER ssl_cert_subject;

ALTER TABLE biz_company_domain
  ADD COLUMN ssl_cert_file_id BIGINT NULL COMMENT 'NAS SSL 인증서 파일 ID' AFTER ssl_cert_issuer;
