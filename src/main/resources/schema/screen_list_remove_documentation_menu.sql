-- 문서(Documentation) 섹션 메뉴 제거 — 관리자·모듈에 남은 customization 링크는 유지
USE spring_boot_app;

DELETE FROM screen_list
WHERE uri_path LIKE '/documentation/getting-started%'
   OR uri_path LIKE '/documentation/customization/plugin%'
   OR uri_path LIKE '/documentation/faq%'
   OR uri_path LIKE '/documentation/gulp%'
   OR uri_path LIKE '/documentation/design-file%'
   OR uri_path IN ('/changelog', '/changelog.do')
   OR screen_id IN (
     'DOCUMENTATION_GETTING_STARTED',
     'DOCUMENTATION_CUSTOMIZATION_PLUGIN',
     'DOCUMENTATION_FAQ',
     'DOCUMENTATION_GULP',
     'DOCUMENTATION_DESIGN_FILE',
     'CHANGELOG'
   );
