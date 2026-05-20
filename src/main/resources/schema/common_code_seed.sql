-- 코드 관리 화면 조회용 샘플 (common_code + common_code_value)
-- 실행: mysql -u springuser -p spring_boot_app < src/main/resources/schema/common_code_seed.sql

INSERT INTO common_code (code_id, code_nm, use_yn, reg_id, update_id) VALUES
('ORDER_STATUS', '주문상태', 'Y', 'SYSTEM', 'SYSTEM'),
('PAYMENT_METHOD', '결제수단', 'Y', 'SYSTEM', 'SYSTEM'),
('USER_ROLE', '사용자권한', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
  code_nm = VALUES(code_nm),
  use_yn = VALUES(use_yn),
  update_id = VALUES(update_id);

-- common_code_value PK가 code_id 단일이면 그룹당 1건만 저장 가능합니다.
INSERT INTO common_code_value (code_id, code_val, use_yn, reg_id, update_id) VALUES
('ORDER_STATUS', 'COMPLETED', 'Y', 'SYSTEM', 'SYSTEM'),
('PAYMENT_METHOD', 'CARD', 'Y', 'SYSTEM', 'SYSTEM'),
('USER_ROLE', 'ADMIN', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
  code_val = VALUES(code_val),
  use_yn = VALUES(use_yn),
  update_id = VALUES(update_id);
