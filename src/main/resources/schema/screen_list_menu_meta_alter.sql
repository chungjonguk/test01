-- screen_list 메뉴 메타 (그룹·아이콘·상위화면) — React/관리 메뉴 DB 연동용
-- USE spring_boot_app;

ALTER TABLE screen_list ADD COLUMN IF NOT EXISTS menu_group VARCHAR(100) NULL;
ALTER TABLE screen_list ADD COLUMN IF NOT EXISTS icon_class VARCHAR(100) NULL;
ALTER TABLE screen_list ADD COLUMN IF NOT EXISTS parent_screen_id VARCHAR(100) NULL;

CREATE INDEX IF NOT EXISTS idx_screen_list_menu_group ON screen_list (menu_group);
CREATE INDEX IF NOT EXISTS idx_screen_list_parent ON screen_list (parent_screen_id);
