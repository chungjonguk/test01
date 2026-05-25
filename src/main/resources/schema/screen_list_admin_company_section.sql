-- 관리자 > 업체 섹션 (업체관리·대시보드 구성 정렬)
USE spring_boot_app;

INSERT INTO screen_list (
  screen_id, screen_nm, uri_path, template_path, sort_ord, use_yn, reg_id, update_id
) VALUES (
  'ADMIN_DASHBOARD_CONFIG', '대시보드 구성', '/admin/dashboard-config', 'admin/dashboard-config', 271, 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
  screen_nm = VALUES(screen_nm),
  template_path = VALUES(template_path),
  sort_ord = VALUES(sort_ord),
  use_yn = 'Y',
  update_id = 'SYSTEM';

UPDATE screen_list
SET sort_ord = 270, update_id = 'SYSTEM'
WHERE screen_id = 'ADMIN_COMPANIES';

UPDATE screen_list
SET sort_ord = 271, update_id = 'SYSTEM'
WHERE screen_id = 'ADMIN_DASHBOARD_CONFIG';

INSERT INTO screen_table_map (
  uri_path, screen_id, primary_table, related_tables, data_type, table_desc,
  reg_id, update_id
) VALUES (
  '/admin/dashboard-config', 'ADMIN_DASHBOARD_CONFIG', 'dashboard_company_config', 'biz_company', 'D', '업체별 대시보드 위젯 구성',
  'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
  screen_id = VALUES(screen_id),
  primary_table = VALUES(primary_table),
  related_tables = VALUES(related_tables),
  data_type = VALUES(data_type),
  table_desc = VALUES(table_desc),
  update_id = 'SYSTEM';
