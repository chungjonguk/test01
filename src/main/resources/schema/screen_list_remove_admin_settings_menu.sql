-- 관리자 > 설정 메뉴 제거 (sidebar.html 에서 삭제됨)
-- /pages/user/settings 는 페이지 > User > 계정 설정 에 남음
-- USE spring_boot_app;

DELETE FROM screen_list
WHERE uri_path LIKE '/documentation/customization/configuration%'
   OR screen_id = 'DOCUMENTATION_CUSTOMIZATION_CONFIGURATION';
