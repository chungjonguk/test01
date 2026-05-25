-- 관리자 > 업체 > 페이지 이미지
USE spring_boot_app;

INSERT INTO screen_list (
  screen_id, screen_nm, uri_path, template_path, sort_ord, use_yn, reg_id, update_id
) VALUES (
  'ADMIN_COMPANY_PAGE_IMAGES', '페이지 이미지', '/admin/company-page-images', 'admin/company-page-images', 272, 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
  screen_nm = VALUES(screen_nm),
  template_path = VALUES(template_path),
  sort_ord = VALUES(sort_ord),
  use_yn = 'Y',
  update_id = 'SYSTEM';

INSERT INTO screen_table_map (
  uri_path, screen_id, primary_table, related_tables, data_type, table_desc,
  reg_id, update_id
) VALUES (
  '/admin/company-page-images', 'ADMIN_COMPANY_PAGE_IMAGES', 'biz_company_page_image', 'biz_company,nas_file', 'D', '업체별 페이지 이미지 등록',
  'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
  screen_id = VALUES(screen_id),
  primary_table = VALUES(primary_table),
  related_tables = VALUES(related_tables),
  data_type = VALUES(data_type),
  table_desc = VALUES(table_desc),
  update_id = 'SYSTEM';
