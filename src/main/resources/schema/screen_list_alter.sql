USE spring_boot_app;

ALTER TABLE screen_list
  MODIFY COLUMN screen_nm VARCHAR(200) NOT NULL;

ALTER TABLE screen_list
  ADD COLUMN uri_path VARCHAR(255) NULL AFTER screen_nm;

ALTER TABLE screen_list
  ADD COLUMN template_path VARCHAR(255) NULL AFTER uri_path;

ALTER TABLE screen_list
  ADD COLUMN sort_ord INT NOT NULL DEFAULT 0 AFTER template_path;

ALTER TABLE screen_list
  ADD UNIQUE KEY uk_screen_list_uri (uri_path);
