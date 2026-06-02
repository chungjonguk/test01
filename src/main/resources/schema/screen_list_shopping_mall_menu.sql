-- 쇼핑몰 전용 화면(URI 고유) — 앱/대시보드와 같은 경로는 APP_* / DASHBOARD_* 사용
-- USE spring_boot_app;

INSERT INTO screen_list (
  screen_id, screen_nm, uri_path, template_path, sort_ord, use_yn, reg_id, update_id
) VALUES
  ('SHOP_HOME', '쇼핑몰 홈', '/shop-home', 'app/e-commerce/product/product-grid', 15, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_DASHBOARD', '쇼핑몰 통계', '/shop-dashboard', 'dashboard/e-commerce', 26, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (screen_id) DO UPDATE SET
  screen_nm = EXCLUDED.screen_nm,
  uri_path = EXCLUDED.uri_path,
  template_path = EXCLUDED.template_path,
  sort_ord = EXCLUDED.sort_ord,
  use_yn = 'Y',
  update_id = 'SYSTEM';
