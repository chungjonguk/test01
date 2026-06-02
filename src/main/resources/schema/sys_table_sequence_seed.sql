-- sys_table_sequence 카탈로그 시드 (DDL은 sys_table_sequence.sql 선행)
-- USE spring_boot_app;

INSERT INTO sys_table_sequence (seq_name, table_name, column_name, next_val, increment_by, min_val, description, use_yn, reg_id, update_id) VALUES
('user_access_log', 'user_access_log', 'access_id', 0, 1, 1, '접속 이력', 'Y', 'SYSTEM', 'SYSTEM'),
('biz_company', 'biz_company', 'company_id', 0, 1, 1, '업체', 'Y', 'SYSTEM', 'SYSTEM'),
('biz_company_page_image', 'biz_company_page_image', 'image_id', 0, 1, 1, '업체 페이지 이미지', 'Y', 'SYSTEM', 'SYSTEM'),
('biz_company_domain', 'biz_company_domain', 'domain_id', 0, 1, 1, '업체 도메인', 'Y', 'SYSTEM', 'SYSTEM'),
('ecm_payment', 'ecm_payment', 'payment_id', 0, 1, 1, '결제', 'Y', 'SYSTEM', 'SYSTEM'),
('nas_file', 'nas_file', 'file_id', 0, 1, 1, 'NAS 파일', 'Y', 'SYSTEM', 'SYSTEM'),
('ecm_product', 'ecm_product', 'product_id', 0, 1, 1, '상품', 'Y', 'SYSTEM', 'SYSTEM'),
('ecm_product_image', 'ecm_product_image', 'image_id', 0, 1, 1, '상품 이미지', 'Y', 'SYSTEM', 'SYSTEM'),
('ecm_customer', 'ecm_customer', 'customer_id', 0, 1, 1, '고객', 'Y', 'SYSTEM', 'SYSTEM'),
('ecm_order', 'ecm_order', 'order_id', 0, 1, 1, '주문', 'Y', 'SYSTEM', 'SYSTEM'),
('ecm_shipment', 'ecm_shipment', 'shipment_id', 0, 1, 1, '배송·운송장', 'Y', 'SYSTEM', 'SYSTEM'),
('ecm_cart_item', 'ecm_cart_item', 'cart_item_id', 0, 1, 1, '장바구니', 'Y', 'SYSTEM', 'SYSTEM'),
('ecm_invoice', 'ecm_invoice', 'invoice_id', 0, 1, 1, '인보이스', 'Y', 'SYSTEM', 'SYSTEM'),
('ecm_billing', 'ecm_billing', 'billing_id', 0, 1, 1, '청구', 'Y', 'SYSTEM', 'SYSTEM'),
('lms_course', 'lms_course', 'course_id', 0, 1, 1, '강좌', 'Y', 'SYSTEM', 'SYSTEM'),
('lms_enrollment', 'lms_enrollment', 'enrollment_id', 0, 1, 1, '수강', 'Y', 'SYSTEM', 'SYSTEM'),
('lms_trainer', 'lms_trainer', 'trainer_id', 0, 1, 1, '강사', 'Y', 'SYSTEM', 'SYSTEM'),
('email_message', 'email_message', 'message_id', 0, 1, 1, '메일', 'Y', 'SYSTEM', 'SYSTEM'),
('calendar_event', 'calendar_event', 'event_id', 0, 1, 1, '일정', 'Y', 'SYSTEM', 'SYSTEM'),
('social_post', 'social_post', 'post_id', 0, 1, 1, '소셜 게시', 'Y', 'SYSTEM', 'SYSTEM'),
('social_activity', 'social_activity', 'activity_id', 0, 1, 1, '소셜 활동', 'Y', 'SYSTEM', 'SYSTEM'),
('social_notification', 'social_notification', 'notification_id', 0, 1, 1, '알림', 'Y', 'SYSTEM', 'SYSTEM'),
('social_follower', 'social_follower', 'follower_id', 0, 1, 1, '팔로워', 'Y', 'SYSTEM', 'SYSTEM'),
('kanban_board', 'kanban_board', 'board_id', 0, 1, 1, '칸반 보드', 'Y', 'SYSTEM', 'SYSTEM'),
('kanban_column', 'kanban_column', 'column_id', 0, 1, 1, '칸반 컬럼', 'Y', 'SYSTEM', 'SYSTEM'),
('kanban_card', 'kanban_card', 'card_id', 0, 1, 1, '칸반 카드', 'Y', 'SYSTEM', 'SYSTEM'),
('chat_room', 'chat_room', 'room_id', 0, 1, 1, '채팅방', 'Y', 'SYSTEM', 'SYSTEM'),
('chat_message', 'chat_message', 'message_id', 0, 1, 1, '채팅 메시지', 'Y', 'SYSTEM', 'SYSTEM'),
('pricing_plan', 'pricing_plan', 'plan_id', 0, 1, 1, '요금제', 'Y', 'SYSTEM', 'SYSTEM'),
('faq_item', 'faq_item', 'faq_id', 0, 1, 1, 'FAQ', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (seq_name) DO NOTHING;

INSERT INTO sys_table_sequence (seq_name, table_name, column_name, next_val, increment_by, min_val, description, use_yn, reg_id, update_id)
SELECT LOWER(c.table_name), LOWER(c.table_name), LOWER(c.column_name), 0, 1, 1,
       c.table_name || ' PK', 'Y', 'SYSTEM', 'SYSTEM'
FROM information_schema.columns c
WHERE c.table_schema = current_schema()
  AND c.is_identity = 'YES'
ON CONFLICT (seq_name) DO NOTHING;
