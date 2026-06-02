-- 권한 시드: user 테이블에 존재하는 계정에만 프로필 부여 (없으면 무시)
-- USE spring_boot_app;

INSERT INTO user_auth_profile (user_id, role_cd, use_yn, reg_id, update_id)
SELECT u.id, 'PLATFORM_ADMIN', 'Y', 'SYSTEM', 'SYSTEM'
FROM "user" u
WHERE u.id IN ('admin', 'ADMIN', 'system', 'SYSTEM')
ON CONFLICT (user_id) DO UPDATE SET role_cd = EXCLUDED.role_cd, use_yn = 'Y', update_id = EXCLUDED.update_id;
