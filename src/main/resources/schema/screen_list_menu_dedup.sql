-- 메뉴 URI 중복 정리: 대시보드 이커머스 vs 쇼핑몰 통계, SHOP_* / APP_* 충돌
USE spring_boot_app;

UPDATE screen_list
SET uri_path = '/shop-dashboard',
    screen_nm = '쇼핑몰 통계',
    template_path = 'dashboard/e-commerce',
    use_yn = 'Y',
    update_id = 'SYSTEM'
WHERE screen_id = 'SHOP_DASHBOARD';

UPDATE screen_list
SET screen_id = 'DASHBOARD_ECOMMERCE',
    screen_nm = '이커머스',
    template_path = 'dashboard/e-commerce',
    use_yn = 'Y',
    update_id = 'SYSTEM'
WHERE uri_path IN ('/dashboard/e-commerce', '/dashboard/e-commerce.do');

DELETE FROM screen_list
WHERE screen_id LIKE 'SHOP_%'
  AND screen_id NOT IN ('SHOP_HOME', 'SHOP_DASHBOARD');
