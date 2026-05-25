-- 쇼핑몰 대표 메뉴 — screen_list 한글 화면명 (사이드바 쇼핑몰 섹션과 동기)
USE spring_boot_app;

INSERT INTO screen_list (
  screen_id, screen_nm, uri_path, template_path, sort_ord, use_yn, reg_id, update_id
) VALUES
  ('SHOP_HOME', '쇼핑몰 홈', '/', 'index', 15, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_PRODUCT_GRID', '상품 카탈로그', '/app/e-commerce/product/product-grid', 'app/e-commerce/product/product-grid', 16, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_PRODUCT_LIST', '상품 목록', '/app/e-commerce/product/product-list', 'app/e-commerce/product/product-list', 17, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_CART', '장바구니', '/app/e-commerce/shopping-cart', 'app/e-commerce/shopping-cart', 18, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_CHECKOUT', '주문·결제', '/app/e-commerce/checkout', 'app/e-commerce/checkout', 19, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_ORDER_LIST', '주문 내역', '/app/e-commerce/orders/order-list', 'app/e-commerce/orders/order-list', 20, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_ORDER_DETAIL', '주문·배송 상세', '/app/e-commerce/orders/order-details', 'app/e-commerce/orders/order-details', 21, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_MY_PROFILE', '내 프로필', '/pages/user/profile', 'pages/user/profile', 22, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_CUSTOMERS', '고객 정보', '/app/e-commerce/customers', 'app/e-commerce/customers', 23, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_INVOICE', '영수증·인보이스', '/app/e-commerce/invoice', 'app/e-commerce/invoice', 24, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_FAQ', '자주 묻는 질문', '/pages/faq/faq-basic', 'pages/faq/faq-basic', 25, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SHOP_DASHBOARD', '쇼핑몰 통계', '/dashboard/e-commerce', 'dashboard/e-commerce', 26, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
  screen_nm = VALUES(screen_nm),
  template_path = VALUES(template_path),
  sort_ord = VALUES(sort_ord),
  use_yn = 'Y',
  update_id = 'SYSTEM';
