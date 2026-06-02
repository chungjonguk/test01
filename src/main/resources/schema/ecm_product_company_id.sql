-- ecm_product 업체 스코프 (기존 DB 마이그레이션 — 일부 문장은 재실행 시 오류, continueOnError 로 무시)
-- USE spring_boot_app;

-- 기본 업체(시드·FK용). 이미 있으면 이름만 갱신
INSERT INTO biz_company (company_id, company_nm, biz_no, status_cd, use_yn, reg_id, update_id)
VALUES (1, '기본 데모 업체', NULL, 'ACTIVE', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (company_id) DO UPDATE SET company_nm = EXCLUDED.company_nm, update_id = EXCLUDED.update_id;

ALTER TABLE ecm_product ADD COLUMN IF NOT EXISTS company_id BIGINT NULL;
CREATE INDEX IF NOT EXISTS idx_ecm_product_company ON ecm_product (company_id);
ALTER TABLE ecm_product ADD CONSTRAINT fk_ecm_product_company FOREIGN KEY (company_id) REFERENCES biz_company (company_id);

UPDATE ecm_product SET company_id = 1 WHERE company_id IS NULL;
