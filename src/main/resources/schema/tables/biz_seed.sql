-- 업무 테이블 샘플 데이터 (화면 목업과 유사)
USE spring_boot_app;

INSERT INTO ecm_customer (customer_id, customer_nm, email, phone, address, reg_id, update_id) VALUES
(1, 'Ricky Antony', 'ricky@example.com', '010-1111-0001', '2392 Main Avenue, Penasauka, New Jersey', 'SYSTEM', 'SYSTEM'),
(2, 'Kin Rossow', 'kin@example.com', '010-1111-0002', '1 Hollywood Blvd, Beverly Hills, California', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE customer_nm = VALUES(customer_nm), update_id = VALUES(update_id);

INSERT INTO ecm_product (product_id, product_nm, category_cd, price, stock_qty, status_cd, reg_id, update_id) VALUES
(1, 'Fitbit Tracker', 'WEARABLE', 99.00, 50, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
(2, 'Apple Watch', 'WEARABLE', 199.00, 30, 'ACTIVE', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE product_nm = VALUES(product_nm), update_id = VALUES(update_id);

INSERT INTO ecm_order (order_id, order_no, customer_id, order_dt, ship_to, shipping_method, status_cd, amount, reg_id, update_id) VALUES
(1, '#181', 1, '2019-04-20', 'Ricky Antony, 2392 Main Avenue, Penasauka, New Jersey 02149', 'Flat Rate', 'COMPLETED', 99.00, 'SYSTEM', 'SYSTEM'),
(2, '#182', 2, '2019-04-20', 'Kin Rossow, 1 Hollywood Blvd, Beverly Hills, California 90210', 'Free Shipping', 'PROCESSING', 120.00, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE status_cd = VALUES(status_cd), update_id = VALUES(update_id);

INSERT INTO ecm_order_item (order_id, line_no, product_id, qty, unit_price) VALUES
(1, 1, 1, 1, 99.00),
(2, 1, 2, 1, 120.00)
ON DUPLICATE KEY UPDATE qty = VALUES(qty);

INSERT INTO lms_course (course_id, course_nm, category_cd, trainer_nm, price, rating, status_cd, reg_id, update_id) VALUES
(1, 'Spring Boot 입문', 'BACKEND', 'Jane Cooper', 49000.00, 4.80, 'OPEN', 'SYSTEM', 'SYSTEM'),
(2, 'Thymeleaf 실전', 'FRONTEND', 'Cody Fisher', 39000.00, 4.50, 'OPEN', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE course_nm = VALUES(course_nm), update_id = VALUES(update_id);

INSERT INTO lms_trainer (trainer_id, trainer_nm, email, bio) VALUES
(1, 'Jane Cooper', 'jane@example.com', '백엔드 강사'),
(2, 'Cody Fisher', 'cody@example.com', '프론트엔드 강사')
ON DUPLICATE KEY UPDATE trainer_nm = VALUES(trainer_nm);

INSERT INTO calendar_event (event_id, title, category_cd, label_cd, start_dt, end_dt, location, description, all_day_yn, reg_id, update_id) VALUES
(1, '팀 미팅', 'BUSINESS', 'primary', '2026-05-22 10:00:00', '2026-05-22 11:00:00', '회의실 A', '주간 스프린트 리뷰', 'N', 'SYSTEM', 'SYSTEM'),
(2, '휴가', 'OTHER', 'success', '2026-05-25 00:00:00', '2026-05-26 00:00:00', NULL, '연차', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE title = VALUES(title), label_cd = VALUES(label_cd), description = VALUES(description),
  all_day_yn = VALUES(all_day_yn), update_id = VALUES(update_id);

INSERT INTO email_message (message_id, folder_cd, subject, sender_email, recipient, body_text, read_yn) VALUES
(1, 'INBOX', '주문 확인', 'orders@printmall.com', 'user@example.com', '주문이 접수되었습니다.', 'N')
ON DUPLICATE KEY UPDATE subject = VALUES(subject);

INSERT INTO social_post (post_id, author_nm, content, like_cnt) VALUES
(1, 'PrintMall', '새 대시보드가 오픈되었습니다.', 12)
ON DUPLICATE KEY UPDATE content = VALUES(content);

INSERT INTO kanban_board (board_id, board_nm) VALUES (1, 'Default Board')
ON DUPLICATE KEY UPDATE board_nm = VALUES(board_nm);

INSERT INTO kanban_column (column_id, board_id, column_nm, sort_ord) VALUES
(1, 1, 'To Do', 1),
(2, 1, 'In Progress', 2),
(3, 1, 'Done', 3)
ON DUPLICATE KEY UPDATE column_nm = VALUES(column_nm);

INSERT INTO kanban_card (card_id, column_id, title, assignee_nm, sort_ord) VALUES
(1, 1, 'DB 스키마 정리', 'SYSTEM', 1),
(2, 2, '화면 테이블 매핑', 'SYSTEM', 1)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO chat_room (room_id, room_nm) VALUES (1, 'General')
ON DUPLICATE KEY UPDATE room_nm = VALUES(room_nm);

INSERT INTO chat_message (message_id, room_id, sender_nm, body_text) VALUES
(1, 1, 'Admin', '안녕하세요. PrintMall 채팅입니다.')
ON DUPLICATE KEY UPDATE body_text = VALUES(body_text);

INSERT INTO pricing_plan (plan_id, plan_nm, price_monthly, sort_ord) VALUES
(1, 'Starter', 0.00, 1),
(2, 'Business', 49.00, 2),
(3, 'Enterprise', 99.00, 3)
ON DUPLICATE KEY UPDATE plan_nm = VALUES(plan_nm);

INSERT INTO faq_item (faq_id, category_nm, question, answer, sort_ord) VALUES
(1, '일반', 'PrintMall이란?', 'Spring Boot + Thymeleaf 기반 관리 포털입니다.', 1)
ON DUPLICATE KEY UPDATE question = VALUES(question);
