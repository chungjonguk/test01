-- 관리자 > 업체별 도메인 등록
-- USE spring_boot_app;

INSERT INTO screen_list (
  screen_id, screen_nm, uri_path, template_path, sort_ord, use_yn, reg_id, update_id
) VALUES (
  'ADMIN_COMPANY_DOMAINS', '업체 도메인', '/admin/company-domains', 'admin/company-domains', 273, 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (screen_id) DO UPDATE SET
  screen_nm = EXCLUDED.screen_nm,
  template_path = EXCLUDED.template_path,
  sort_ord = EXCLUDED.sort_ord,
  use_yn = 'Y',
  update_id = 'SYSTEM';

INSERT INTO screen_table_map (
  uri_path, screen_id, primary_table, related_tables, data_type, table_desc,
  reg_id, update_id
) VALUES (
  '/admin/company-domains', 'ADMIN_COMPANY_DOMAINS', 'biz_company_domain', 'biz_company', 'D', '업체별 접속 도메인 등록',
  'SYSTEM', 'SYSTEM'
)
ON CONFLICT (uri_path) DO UPDATE SET
  screen_id = EXCLUDED.screen_id,
  primary_table = EXCLUDED.primary_table,
  related_tables = EXCLUDED.related_tables,
  data_type = EXCLUDED.data_type,
  table_desc = EXCLUDED.table_desc,
  update_id = 'SYSTEM';
