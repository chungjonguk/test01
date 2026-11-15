-- 로컬/개발용 데모 관리자 계정 (plain pw — 개발 DB 전용)
-- id: admin  /  pw: admin123

INSERT INTO "user" (
  id, pw, name, sex, rrno, email,
  zipcode, address, address_detail,
  update_id, primary_address_type
) VALUES (
  'admin', 'admin123', '관리자', 'M', '9001011234567', 'admin@printmall.local',
  '06236', '서울특별시 강남구', '프린트몰',
  'SYSTEM', 'HOME'
)
ON CONFLICT (id) DO UPDATE SET
  pw = EXCLUDED.pw,
  name = EXCLUDED.name,
  email = EXCLUDED.email,
  update_id = 'SYSTEM',
  upd_dt = CURRENT_TIMESTAMP;

INSERT INTO user_auth_profile (user_id, role_cd, use_yn, reg_id, update_id)
VALUES ('admin', 'PLATFORM_ADMIN', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (user_id) DO UPDATE SET
  role_cd = 'PLATFORM_ADMIN',
  use_yn = 'Y',
  update_id = 'SYSTEM';
