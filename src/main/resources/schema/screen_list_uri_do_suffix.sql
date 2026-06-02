-- screen_list / screen_table_map URI를 *.do 형식으로 마이그레이션
-- USE spring_boot_app;

UPDATE screen_list
SET uri_path = '/index.do'
WHERE uri_path = '/';

UPDATE screen_list
SET uri_path = CONCAT(uri_path, '.do')
WHERE uri_path NOT LIKE '%.do'
  AND uri_path NOT LIKE '/auth/%'
  AND uri_path NOT LIKE '/api/%'
  AND uri_path NOT LIKE '/assets/%'
  AND uri_path NOT LIKE '/vendors/%'
  AND uri_path NOT LIKE '/error%'
  AND uri_path NOT LIKE '%.%.%';

UPDATE screen_table_map
SET uri_path = '/index.do'
WHERE uri_path = '/';

UPDATE screen_table_map
SET uri_path = CONCAT(uri_path, '.do')
WHERE uri_path NOT LIKE '%.do'
  AND uri_path NOT LIKE '/auth/%'
  AND uri_path NOT LIKE '/api/%'
  AND uri_path NOT LIKE '/assets/%'
  AND uri_path NOT LIKE '/vendors/%'
  AND uri_path NOT LIKE '/error%'
  AND uri_path NOT LIKE '%.%.%';
