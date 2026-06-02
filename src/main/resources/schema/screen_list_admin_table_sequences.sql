-- 관리자 > 테이블 시퀀스 메뉴 (screen_list · screen_table_map)
-- USE spring_boot_app;

INSERT INTO screen_list (
  screen_id, screen_nm, uri_path, template_path, sort_ord, use_yn, reg_id, update_id
) VALUES (
  'ADMIN_TABLE_SEQUENCES', '테이블 시퀀스', '/admin/table-sequences.do', 'admin/table-sequences', 245, 'Y', 'SYSTEM', 'SYSTEM'
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
  '/admin/table-sequences.do', 'ADMIN_TABLE_SEQUENCES', 'sys_table_sequence', NULL, 'D', '테이블별 PK 시퀀스(sys_table_sequence) 조회',
  'SYSTEM', 'SYSTEM'
)
ON CONFLICT (uri_path) DO UPDATE SET
  screen_id = EXCLUDED.screen_id,
  primary_table = EXCLUDED.primary_table,
  related_tables = EXCLUDED.related_tables,
  data_type = EXCLUDED.data_type,
  table_desc = EXCLUDED.table_desc,
  update_id = 'SYSTEM';
