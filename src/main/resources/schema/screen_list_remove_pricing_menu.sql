-- Pricing 메뉴 제거 (sidebar.html 에서 삭제됨)
USE spring_boot_app;

DELETE FROM screen_list
WHERE uri_path LIKE '/pages/pricing/%'
   OR uri_path LIKE '/pages/pricing%.do'
   OR screen_id LIKE 'PAGES_PRICING%';
