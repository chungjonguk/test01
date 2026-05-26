-- 메뉴(screen_list) URI 중복 정리: .do 미부착 행 제거, 동일 논리 경로 병합, SHOP_* 충돌
USE spring_boot_app;

-- 1) .do 형식이 있으면 동일 경로의 비-.do 행 삭제
DELETE s FROM screen_list s
WHERE s.uri_path NOT LIKE '%.do'
  AND s.uri_path NOT LIKE '/auth/%'
  AND s.uri_path NOT LIKE '/api/%'
  AND s.uri_path NOT LIKE '/assets/%'
  AND s.uri_path NOT LIKE '/vendors/%'
  AND EXISTS (
    SELECT 1 FROM screen_list d
    WHERE d.uri_path = (
      CASE WHEN s.uri_path IN ('/', '/index') THEN '/index.do'
           ELSE CONCAT(s.uri_path, '.do')
      END
    )
  );

-- 2) 논리 URI가 같으나 screen_id만 다른 잔여 중복(우선순위 낮은 행 삭제)
DELETE FROM screen_list
WHERE screen_id IN (
  SELECT screen_id FROM (
    SELECT screen_id,
           ROW_NUMBER() OVER (
             PARTITION BY
               CASE
                 WHEN uri_path IN ('/', '/index', '/index.do') THEN '/index.do'
                 WHEN uri_path LIKE '%.do' THEN uri_path
                 ELSE CONCAT(TRIM(TRAILING '/' FROM uri_path), '.do')
               END
             ORDER BY
               (uri_path LIKE '%.do') DESC,
               (screen_id NOT LIKE '%.DO') DESC,
               (screen_id NOT LIKE 'SHOP\_%') DESC,
               (screen_id REGEXP '^(ADMIN_|APP_|ECM_|DASHBOARD_|HOME)') DESC,
               sort_ord ASC,
               screen_id ASC
           ) AS rn
    FROM screen_list
  ) ranked
  WHERE rn > 1
);

-- 3) screen_table_map: .do 쌍이 있으면 비-.do 행 삭제
DELETE m FROM screen_table_map m
WHERE m.uri_path NOT LIKE '%.do'
  AND m.uri_path NOT LIKE '/auth/%'
  AND m.uri_path NOT LIKE '/api/%'
  AND EXISTS (
    SELECT 1 FROM screen_table_map d
    WHERE d.uri_path = (
      CASE WHEN m.uri_path IN ('/', '/index') THEN '/index.do'
           ELSE CONCAT(m.uri_path, '.do')
      END
    )
  );

-- 4) 대시보드 이커머스 vs 쇼핑몰 통계, SHOP_* / APP_* 충돌 (기존)
UPDATE screen_list
SET uri_path = '/shop-dashboard.do',
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
WHERE screen_id LIKE 'SHOP\_%'
  AND screen_id NOT IN ('SHOP_HOME', 'SHOP_DASHBOARD');
