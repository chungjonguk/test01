-- 관리자 > 재고관리 (screen_list · screen_table_map)
USE spring_boot_app;

INSERT INTO screen_list (
  screen_id, screen_nm, uri_path, template_path, sort_ord, use_yn, reg_id, update_id
) VALUES (
  'ADMIN_INVENTORY', '재고관리', '/admin/inventory.do', 'admin/inventory', 274, 'Y', 'SYSTEM', 'SYSTEM'
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
  '/admin/inventory.do', 'ADMIN_INVENTORY', 'ecm_product', NULL, 'D', '상품 재고 수량 조회·조정',
  'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
  screen_id = VALUES(screen_id),
  primary_table = VALUES(primary_table),
  related_tables = VALUES(related_tables),
  data_type = VALUES(data_type),
  table_desc = VALUES(table_desc),
  update_id = 'SYSTEM';
