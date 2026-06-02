-- 택배사·배송상태 공통코드
-- USE spring_boot_app;

INSERT INTO common_code (code_id, code_nm, use_yn, reg_id, update_id) VALUES
('CARRIER_CD', '택배사', 'Y', 'SYSTEM', 'SYSTEM'),
('SHIPMENT_STATUS', '배송상태', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (code_id) DO UPDATE SET code_nm = EXCLUDED.code_nm, use_yn = EXCLUDED.use_yn, update_id = EXCLUDED.update_id;

INSERT INTO common_code_value (code_id, code_val, use_yn, reg_id, update_id) VALUES
('CARRIER_CD', 'CJ|CJ대한통운', 'Y', 'SYSTEM', 'SYSTEM'),
('CARRIER_CD', 'EPOST|우체국', 'Y', 'SYSTEM', 'SYSTEM'),
('CARRIER_CD', 'LOTTE|롯데택배', 'Y', 'SYSTEM', 'SYSTEM'),
('SHIPMENT_STATUS', 'REQUESTED|접수요청', 'Y', 'SYSTEM', 'SYSTEM'),
('SHIPMENT_STATUS', 'ISSUED|운송장발급', 'Y', 'SYSTEM', 'SYSTEM'),
('SHIPMENT_STATUS', 'FAILED|실패', 'Y', 'SYSTEM', 'SYSTEM'),
('SHIPMENT_STATUS', 'CANCELLED|취소', 'Y', 'SYSTEM', 'SYSTEM'),
('ORDER_STATUS', 'SHIPPED|배송중', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (code_id, code_val) DO UPDATE SET use_yn = EXCLUDED.use_yn, update_id = EXCLUDED.update_id;
