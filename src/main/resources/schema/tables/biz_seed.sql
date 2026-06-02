-- 업무 테이블 샘플 데이터 (화면 목업과 유사)
-- USE spring_boot_app;

INSERT INTO ecm_customer (customer_id, customer_nm, email, phone, address, reg_id, update_id) VALUES
(1, 'Ricky Antony', 'ricky@example.com', '010-1111-0001', '2392 Main Avenue, Penasauka, New Jersey', 'SYSTEM', 'SYSTEM'),
(2, 'Kin Rossow', 'kin@example.com', '010-1111-0002', '1 Hollywood Blvd, Beverly Hills, California', 'SYSTEM', 'SYSTEM')
ON CONFLICT (customer_id) DO UPDATE SET customer_nm = EXCLUDED.customer_nm, update_id = EXCLUDED.update_id;

INSERT INTO biz_company (company_id, company_nm, status_cd, use_yn, reg_id, update_id)
VALUES (1, '기본 데모 업체', 'ACTIVE', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (company_id) DO UPDATE SET company_nm = EXCLUDED.company_nm, update_id = EXCLUDED.update_id;

INSERT INTO ecm_product (product_id, company_id, product_nm, category_cd, price, stock_qty, status_cd, reg_id, update_id) VALUES
(1, 1, 'Fitbit Tracker', 'WEARABLE', 99.00, 50, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
(2, 1, 'Apple Watch', 'WEARABLE', 199.00, 30, 'ACTIVE', 'SYSTEM', 'SYSTEM')
ON CONFLICT (product_id) DO UPDATE SET product_nm = EXCLUDED.product_nm, company_id = EXCLUDED.company_id, update_id = EXCLUDED.update_id;

INSERT INTO ecm_order (order_id, order_no, customer_id, order_dt, ship_to, shipping_method, status_cd, amount, reg_id, update_id) VALUES
(1, '#181', 1, '2019-04-20', 'Ricky Antony, 2392 Main Avenue, Penasauka, New Jersey 02149', 'Flat Rate', 'COMPLETED', 99.00, 'SYSTEM', 'SYSTEM'),
(2, '#182', 2, '2019-04-20', 'Kin Rossow, 1 Hollywood Blvd, Beverly Hills, California 90210', 'Free Shipping', 'PROCESSING', 120.00, 'SYSTEM', 'SYSTEM')
ON CONFLICT (order_id) DO UPDATE SET status_cd = EXCLUDED.status_cd, update_id = EXCLUDED.update_id;

INSERT INTO ecm_order_item (order_id, line_no, product_id, qty, unit_price) VALUES
(1, 1, 1, 1, 99.00),
(2, 1, 2, 1, 120.00)
ON CONFLICT (order_id, line_no) DO UPDATE SET qty = EXCLUDED.qty;

INSERT INTO lms_course (course_id, course_nm, category_cd, trainer_nm, price, rating, status_cd, reg_id, update_id) VALUES
(1, 'Spring Boot 입문', 'BACKEND', 'Jane Cooper', 49000.00, 4.80, 'OPEN', 'SYSTEM', 'SYSTEM'),
(2, 'Thymeleaf 실전', 'FRONTEND', 'Cody Fisher', 39000.00, 4.50, 'OPEN', 'SYSTEM', 'SYSTEM')
ON CONFLICT (course_id) DO UPDATE SET course_nm = EXCLUDED.course_nm, update_id = EXCLUDED.update_id;

INSERT INTO lms_trainer (trainer_id, trainer_nm, email, bio) VALUES
(1, 'Jane Cooper', 'jane@example.com', '백엔드 강사'),
(2, 'Cody Fisher', 'cody@example.com', '프론트엔드 강사')
ON CONFLICT (trainer_id) DO UPDATE SET trainer_nm = EXCLUDED.trainer_nm;

INSERT INTO calendar_event (event_id, title, category_cd, label_cd, start_dt, end_dt, location, description, all_day_yn, reg_id, update_id) VALUES
(1, '팀 미팅', 'BUSINESS', 'primary', '2026-05-22 10:00:00', '2026-05-22 11:00:00', '회의실 A', '주간 스프린트 리뷰', 'N', 'SYSTEM', 'SYSTEM'),
(2, '휴가', 'OTHER', 'success', '2026-05-25 00:00:00', '2026-05-26 00:00:00', NULL, '연차', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (event_id) DO UPDATE SET title = EXCLUDED.title, label_cd = EXCLUDED.label_cd, description = EXCLUDED.description,
  all_day_yn = EXCLUDED.all_day_yn, update_id = EXCLUDED.update_id;

INSERT INTO email_message (message_id, folder_cd, subject, sender_email, recipient, body_text, read_yn) VALUES
(1, 'INBOX', '주문 확인', 'orders@printmall.com', 'user@example.com', '주문이 접수되었습니다.', 'N')
ON CONFLICT (message_id) DO UPDATE SET subject = EXCLUDED.subject;

INSERT INTO social_post (post_id, author_nm, content, like_cnt) VALUES
(1, 'PrintMall', '새 대시보드가 오픈되었습니다.', 12)
ON CONFLICT (post_id) DO UPDATE SET content = EXCLUDED.content;

INSERT INTO kanban_board (board_id, board_nm) VALUES (1, 'Default Board')
ON CONFLICT (board_id) DO UPDATE SET board_nm = EXCLUDED.board_nm;

INSERT INTO kanban_column (column_id, board_id, column_nm, sort_ord) VALUES
(1, 1, 'To Do', 1),
(2, 1, 'In Progress', 2),
(3, 1, 'Done', 3)
ON CONFLICT (column_id) DO UPDATE SET column_nm = EXCLUDED.column_nm;

INSERT INTO kanban_card (card_id, column_id, title, assignee_nm, sort_ord) VALUES
(1, 1, 'DB 스키마 정리', 'SYSTEM', 1),
(2, 2, '화면 테이블 매핑', 'SYSTEM', 1)
ON CONFLICT (card_id) DO UPDATE SET title = EXCLUDED.title;

INSERT INTO chat_room (room_id, room_nm) VALUES (1, 'General')
ON CONFLICT (room_id) DO UPDATE SET room_nm = EXCLUDED.room_nm;

INSERT INTO chat_message (message_id, room_id, sender_nm, body_text) VALUES
(1, 1, 'Admin', '안녕하세요. PrintMall 채팅입니다.')
ON CONFLICT (message_id) DO UPDATE SET body_text = EXCLUDED.body_text;

INSERT INTO pricing_plan (plan_id, plan_nm, price_monthly, sort_ord) VALUES
(1, 'Starter', 0.00, 1),
(2, 'Business', 49.00, 2),
(3, 'Enterprise', 99.00, 3)
ON CONFLICT (plan_id) DO UPDATE SET plan_nm = EXCLUDED.plan_nm;

INSERT INTO faq_item (faq_id, category_nm, question, answer, sort_ord) VALUES
(1, '일반', 'PrintMall이란?', 'Spring Boot + Thymeleaf 기반 관리 포털입니다.', 1)
ON CONFLICT (faq_id) DO UPDATE SET question = EXCLUDED.question;
