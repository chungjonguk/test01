-- USE spring_boot_app;

ALTER TABLE screen_list
  ALTER COLUMN screen_nm TYPE VARCHAR(200);
ALTER TABLE screen_list
  ALTER COLUMN screen_nm SET NOT NULL;

ALTER TABLE screen_list
  ADD COLUMN IF NOT EXISTS uri_path VARCHAR(255) NULL;

ALTER TABLE screen_list
  ADD COLUMN IF NOT EXISTS template_path VARCHAR(255) NULL;

ALTER TABLE screen_list
  ADD COLUMN IF NOT EXISTS sort_ord INTEGER NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_screen_list_uri ON screen_list (uri_path);
