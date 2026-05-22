-- 업체 샘플 데이터
USE spring_boot_app;

INSERT INTO biz_company (company_nm, biz_no, ceo_nm, tel, email, address, status_cd, use_yn, memo, reg_id, update_id) VALUES
('(주)프린트몰', '123-45-67890', '홍길동', '02-1234-5678', 'contact@printmall.local', '서울특별시 강남구 테헤란로 123', 'ACTIVE', 'Y', '본사', 'SYSTEM', 'SYSTEM'),
('한국인쇄공업', '234-56-78901', '김인쇄', '031-987-6543', 'sales@print-korea.example', '경기도 성남시 분당구 판교역로 10', 'ACTIVE', 'Y', NULL, 'SYSTEM', 'SYSTEM'),
('스마트패키징', '345-67-89012', '이포장', '051-222-3333', 'info@smart-pack.example', '부산광역시 해운대구 센텀중앙로 55', 'INACTIVE', 'N', '거래 중지', 'SYSTEM', 'SYSTEM')
;
