-- USE spring_boot_app;

INSERT INTO screen_list (
  screen_id, screen_nm, uri_path, template_path, sort_ord, use_yn, reg_id, update_id
) VALUES (
  'ADMIN_COMPANY_CUSTOMER_MENUS', '고객 노출 메뉴', '/admin/company-customer-menus',
  'admin/company-customer-menus', 48, 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (screen_id) DO UPDATE SET
  screen_nm = EXCLUDED.screen_nm,
  template_path = EXCLUDED.template_path,
  use_yn = 'Y',
  update_id = EXCLUDED.update_id;
