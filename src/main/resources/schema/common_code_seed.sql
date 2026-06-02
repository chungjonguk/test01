-- 코드 관리 화면 조회용 샘플 (common_code + common_code_value)
-- 실행: mysql -u redcroxx -p spring_boot_app < src/main/resources/schema/common_code_seed.sql

INSERT INTO common_code (code_id, code_nm, use_yn, reg_id, update_id) VALUES
('ORDER_STATUS', '주문상태', 'Y', 'SYSTEM', 'SYSTEM'),
('PAYMENT_METHOD', '결제수단', 'Y', 'SYSTEM', 'SYSTEM'),
('USER_ROLE', '사용자권한', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_PROJECT_TIME', '대시보드-프로젝트시간', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '대시보드-월', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_DATE_RANGE', '대시보드-기간', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_BANDWIDTH_PERIOD', '대시보드-대역폭기간', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (code_id) DO UPDATE SET
  code_nm = EXCLUDED.code_nm,
  use_yn = EXCLUDED.use_yn,
  update_id = EXCLUDED.update_id;

-- 상세코드: code_val 형식 = "값|표시명" (| 없으면 값=표시명)
-- PK (code_id, code_val) 필요 — common_code_value_pk_alter.sql 실행
INSERT INTO common_code_value (code_id, code_val, use_yn, reg_id, update_id) VALUES
('ORDER_STATUS', 'COMPLETED', 'Y', 'SYSTEM', 'SYSTEM'),
('PAYMENT_METHOD', 'CARD', 'Y', 'SYSTEM', 'SYSTEM'),
('USER_ROLE', 'ADMIN', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_PROJECT_TIME', 'WORKING|Working Time', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_PROJECT_TIME', 'ESTIMATED|Estimated Time', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_PROJECT_TIME', 'BILLABLE|Billable Time', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '0|January', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '1|February', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '2|March', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '3|April', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '4|May', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '5|Jun', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '6|July', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '7|August', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '8|September', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '9|October', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '10|November', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_MONTH', '11|December', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_DATE_RANGE', 'week|Last 7 days', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_DATE_RANGE', 'month|Last Month', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_DATE_RANGE', 'year|Last Year', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_BANDWIDTH_PERIOD', '6months|Last 6 Months', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_BANDWIDTH_PERIOD', 'year|Last Year', 'Y', 'SYSTEM', 'SYSTEM'),
('DASHBOARD_BANDWIDTH_PERIOD', '2year|Last 2 Year', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (code_id, code_val) DO UPDATE SET
  use_yn = EXCLUDED.use_yn,
  update_id = EXCLUDED.update_id;
