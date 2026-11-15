-- React 전용·누락 화면을 screen_list / screen_table_map 에 등록하고 menu_group 을 채웁니다.
-- USE spring_boot_app;

INSERT INTO screen_list (
  screen_id, screen_nm, uri_path, template_path, sort_ord, use_yn,
  menu_group, icon_class, reg_id, update_id
) VALUES
  ('ADMIN_SCREEN_LOCALES', '화면별 언어', '/admin/screen-locales.do', 'admin/screen-locales', 735, 'Y', '관리자', 'fas fa-language', 'SYSTEM', 'SYSTEM'),
  ('ADMIN_LAYOUT', '레이아웃 구조', '/admin/layout.do', 'admin/layout', 736, 'Y', '관리자', 'fas fa-border-all', 'SYSTEM', 'SYSTEM'),
  ('ADMIN_WIDGETS', '위젯 등록', '/admin/widgets.do', 'admin/widgets', 737, 'Y', '관리자', 'fas fa-puzzle-piece', 'SYSTEM', 'SYSTEM'),
  ('ADMIN_WIDGET_LAYOUTS', '위젯 레이아웃', '/admin/widget-layouts.do', 'admin/widget-layouts', 738, 'Y', '관리자', 'fas fa-th-large', 'SYSTEM', 'SYSTEM'),
  ('ADMIN_PERMISSIONS', '화면 권한', '/admin/permissions.do', 'admin/permissions', 739, 'Y', '관리자', 'fas fa-user-shield', 'SYSTEM', 'SYSTEM'),
  ('PAGES_WIDGET_BOARD', '위젯 보드', '/pages/widget-board.do', 'pages/widget-board', 371, 'Y', '페이지', 'fas fa-th', 'SYSTEM', 'SYSTEM'),
  ('PAGES_ERRORS_403', '403', '/pages/errors/403.do', 'pages/errors/403', 615, 'Y', '페이지', 'fas fa-ban', 'SYSTEM', 'SYSTEM'),
  ('PAGES_AUTHENTICATION_MODAL', 'Modal', '/pages/authentication/modal.do', 'pages/authentication/modal', 605, 'Y', '페이지', 'fas fa-window-maximize', 'SYSTEM', 'SYSTEM'),
  ('PAGES_USER_PROFILE', '프로필', '/pages/user/profile.do', 'pages/user/profile', 612, 'Y', '마이페이지', 'fas fa-user', 'SYSTEM', 'SYSTEM'),
  ('AUTH_LOGIN_SHORT', '로그인(단축)', '/login.do', 'pages/authentication/simple/login', 385, 'Y', '페이지', 'fas fa-sign-in-alt', 'SYSTEM', 'SYSTEM'),
  ('AUTH_REGISTER_SHORT', '회원가입(단축)', '/register.do', 'pages/authentication/simple/register', 386, 'Y', '페이지', 'fas fa-user-plus', 'SYSTEM', 'SYSTEM'),
  ('AUTH_FORGOT_SHORT', '비밀번호 찾기(단축)', '/forgot-password.do', 'pages/authentication/simple/forgot-password', 387, 'Y', '페이지', 'fas fa-key', 'SYSTEM', 'SYSTEM')
ON CONFLICT (screen_id) DO UPDATE SET
  screen_nm = EXCLUDED.screen_nm,
  uri_path = EXCLUDED.uri_path,
  template_path = EXCLUDED.template_path,
  sort_ord = EXCLUDED.sort_ord,
  use_yn = 'Y',
  menu_group = EXCLUDED.menu_group,
  icon_class = EXCLUDED.icon_class,
  update_id = 'SYSTEM',
  update_dt = CURRENT_TIMESTAMP;

INSERT INTO screen_table_map (
  uri_path, screen_id, primary_table, related_tables, data_type, table_desc, reg_id, update_id
) VALUES
  ('/admin/screen-locales.do', 'ADMIN_SCREEN_LOCALES', NULL, NULL, 'S', '화면별 언어 오버라이드(로컬/정적)', 'SYSTEM', 'SYSTEM'),
  ('/admin/layout.do', 'ADMIN_LAYOUT', NULL, NULL, 'S', '레이아웃 구조(로컬 설정)', 'SYSTEM', 'SYSTEM'),
  ('/admin/widgets.do', 'ADMIN_WIDGETS', NULL, NULL, 'S', '위젯 등록(로컬 스토어)', 'SYSTEM', 'SYSTEM'),
  ('/admin/widget-layouts.do', 'ADMIN_WIDGET_LAYOUTS', 'dashboard_company_config', 'biz_company', 'D', '위젯 레이아웃 구성', 'SYSTEM', 'SYSTEM'),
  ('/admin/permissions.do', 'ADMIN_PERMISSIONS', NULL, NULL, 'S', '역할별 화면 권한(로컬)', 'SYSTEM', 'SYSTEM'),
  ('/pages/widget-board.do', 'PAGES_WIDGET_BOARD', NULL, NULL, 'S', '위젯 보드 페이지', 'SYSTEM', 'SYSTEM'),
  ('/pages/errors/403.do', 'PAGES_ERRORS_403', NULL, NULL, 'S', '권한 없음', 'SYSTEM', 'SYSTEM'),
  ('/pages/authentication/modal.do', 'PAGES_AUTHENTICATION_MODAL', NULL, NULL, 'S', '인증 모달', 'SYSTEM', 'SYSTEM'),
  ('/pages/user/profile.do', 'PAGES_USER_PROFILE', 'user', NULL, 'D', '프로필', 'SYSTEM', 'SYSTEM'),
  ('/login.do', 'AUTH_LOGIN_SHORT', 'user', 'user_access_log', 'D', '로그인 단축 URL', 'SYSTEM', 'SYSTEM'),
  ('/register.do', 'AUTH_REGISTER_SHORT', 'user', NULL, 'D', '회원가입 단축 URL', 'SYSTEM', 'SYSTEM'),
  ('/forgot-password.do', 'AUTH_FORGOT_SHORT', NULL, NULL, 'S', '비밀번호 찾기 단축 URL', 'SYSTEM', 'SYSTEM')
ON CONFLICT (uri_path) DO UPDATE SET
  screen_id = EXCLUDED.screen_id,
  primary_table = EXCLUDED.primary_table,
  related_tables = EXCLUDED.related_tables,
  data_type = EXCLUDED.data_type,
  table_desc = EXCLUDED.table_desc,
  update_id = 'SYSTEM',
  update_dt = CURRENT_TIMESTAMP;

-- ========== 기존 화면 menu_group 백필 ==========
UPDATE screen_list SET menu_group = '대시보드', update_dt = CURRENT_TIMESTAMP
WHERE menu_group IS NULL AND (uri_path IN ('/index.do', '/dashboard.do') OR uri_path LIKE '/dashboard/%');

UPDATE screen_list SET menu_group = '쇼핑몰', update_dt = CURRENT_TIMESTAMP
WHERE menu_group IS NULL AND (
  uri_path LIKE '/shop%' OR uri_path LIKE '/app/e-commerce/%'
  OR uri_path IN ('/admin/inventory.do', '/admin/shipping.do')
);

UPDATE screen_list SET menu_group = '마이페이지', update_dt = CURRENT_TIMESTAMP
WHERE menu_group IS NULL AND uri_path LIKE '/pages/user/%';

UPDATE screen_list SET menu_group = '앱', update_dt = CURRENT_TIMESTAMP
WHERE menu_group IS NULL AND uri_path LIKE '/app/%' AND uri_path NOT LIKE '/app/e-commerce/%';

UPDATE screen_list SET menu_group = '페이지', update_dt = CURRENT_TIMESTAMP
WHERE menu_group IS NULL AND (
  uri_path LIKE '/pages/%' OR uri_path IN ('/login.do', '/register.do', '/forgot-password.do')
);

UPDATE screen_list SET menu_group = '관리자', update_dt = CURRENT_TIMESTAMP
WHERE menu_group IS NULL AND (uri_path LIKE '/admin/%' OR uri_path = '/users.do');

UPDATE screen_list SET menu_group = '기타', update_dt = CURRENT_TIMESTAMP
WHERE menu_group IS NULL;
