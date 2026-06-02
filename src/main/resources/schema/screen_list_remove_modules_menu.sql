-- 모듈(Modules) 메뉴 섹션 제거 (sidebar.html 에서 삭제됨)
-- USE spring_boot_app;

DELETE FROM screen_list
WHERE uri_path LIKE '/modules/%'
   OR uri_path LIKE '/modules%.do'
   OR uri_path IN ('/widgets', '/widgets.do')
   OR screen_id LIKE 'MODULES_%';
