-- 업무 화면(/app/** 등)용 테이블 — Falcon UI 목록·상세와 1:1 대응
USE spring_boot_app;

-- ========== E-Commerce ==========
CREATE TABLE IF NOT EXISTS ecm_product (
  product_id     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '상품ID',
  company_id     BIGINT        NULL COMMENT '업체ID(biz_company)',
  product_nm     VARCHAR(200)  NOT NULL COMMENT '상품명',
  category_cd    VARCHAR(50)   NULL COMMENT '카테고리(공통코드)',
  price          DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '가격',
  stock_qty      INT           NOT NULL DEFAULT 0 COMMENT '재고',
  status_cd      VARCHAR(30)   NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
  img_url        VARCHAR(500)  NULL COMMENT '이미지 URL',
  description    TEXT          NULL COMMENT '설명',
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (product_id),
  KEY idx_ecm_product_category (category_cd),
  KEY idx_ecm_product_status (status_cd),
  KEY idx_ecm_product_company (company_id),
  CONSTRAINT fk_ecm_product_company FOREIGN KEY (company_id) REFERENCES biz_company (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ecm_product_image (
  image_id     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '이미지ID',
  product_id   BIGINT        NOT NULL COMMENT '상품ID',
  sort_ord     INT           NOT NULL DEFAULT 1 COMMENT '표시순서 1~5',
  img_url      VARCHAR(500)  NOT NULL COMMENT '이미지 URL 또는 업로드 경로',
  reg_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (image_id),
  UNIQUE KEY uk_ecm_product_image_ord (product_id, sort_ord),
  KEY idx_ecm_product_image_product (product_id),
  CONSTRAINT fk_ecm_product_image_product
    FOREIGN KEY (product_id) REFERENCES ecm_product (product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ecm_customer (
  customer_id    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '고객ID',
  customer_nm    VARCHAR(100)  NOT NULL COMMENT '고객명',
  email          VARCHAR(200)  NOT NULL COMMENT '이메일',
  phone          VARCHAR(30)   NULL COMMENT '연락처',
  zipcode        VARCHAR(10)   NULL,
  address        VARCHAR(255)  NULL,
  address_detail VARCHAR(255)  NULL,
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (customer_id),
  UNIQUE KEY uk_ecm_customer_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ecm_order (
  order_id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '주문ID',
  order_no         VARCHAR(30)   NOT NULL COMMENT '주문번호(#181)',
  customer_id      BIGINT        NOT NULL COMMENT '고객ID',
  order_dt         DATE          NOT NULL COMMENT '주문일',
  ship_to          VARCHAR(500)  NOT NULL COMMENT '배송지',
  shipping_method  VARCHAR(100)  NULL COMMENT '배송방식',
  status_cd        VARCHAR(30)   NOT NULL COMMENT '상태(ORDER_STATUS)',
  amount           DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '금액',
  reg_id           VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id        VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (order_id),
  UNIQUE KEY uk_ecm_order_no (order_no),
  KEY idx_ecm_order_customer (customer_id),
  KEY idx_ecm_order_status (status_cd),
  CONSTRAINT fk_ecm_order_customer FOREIGN KEY (customer_id) REFERENCES ecm_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ecm_order_item (
  order_id       BIGINT        NOT NULL,
  line_no        INT           NOT NULL COMMENT '라인번호',
  product_id     BIGINT        NOT NULL,
  qty            INT           NOT NULL DEFAULT 1,
  unit_price     DECIMAL(12,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (order_id, line_no),
  CONSTRAINT fk_ecm_order_item_order FOREIGN KEY (order_id) REFERENCES ecm_order (order_id) ON DELETE CASCADE,
  CONSTRAINT fk_ecm_order_item_product FOREIGN KEY (product_id) REFERENCES ecm_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ecm_cart_item (
  cart_item_id   BIGINT        NOT NULL AUTO_INCREMENT,
  session_id     VARCHAR(100)  NOT NULL COMMENT '세션/사용자 식별',
  product_id     BIGINT        NOT NULL,
  qty            INT           NOT NULL DEFAULT 1,
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (cart_item_id),
  KEY idx_ecm_cart_session (session_id),
  CONSTRAINT fk_ecm_cart_product FOREIGN KEY (product_id) REFERENCES ecm_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ecm_invoice (
  invoice_id     BIGINT        NOT NULL AUTO_INCREMENT,
  order_id       BIGINT        NOT NULL,
  invoice_no     VARCHAR(30)   NOT NULL,
  issue_dt       DATE          NOT NULL,
  total_amount   DECIMAL(12,2) NOT NULL DEFAULT 0,
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (invoice_id),
  UNIQUE KEY uk_ecm_invoice_no (invoice_no),
  CONSTRAINT fk_ecm_invoice_order FOREIGN KEY (order_id) REFERENCES ecm_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ecm_billing (
  billing_id     BIGINT        NOT NULL AUTO_INCREMENT,
  customer_id    BIGINT        NOT NULL,
  cycle_cd       VARCHAR(30)   NOT NULL COMMENT 'BILLING_CYCLE',
  plan_nm        VARCHAR(100)  NOT NULL,
  amount         DECIMAL(12,2) NOT NULL DEFAULT 0,
  next_bill_dt   DATE          NULL,
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (billing_id),
  CONSTRAINT fk_ecm_billing_customer FOREIGN KEY (customer_id) REFERENCES ecm_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== E-Learning ==========
CREATE TABLE IF NOT EXISTS lms_course (
  course_id      BIGINT        NOT NULL AUTO_INCREMENT,
  course_nm      VARCHAR(200)  NOT NULL,
  category_cd    VARCHAR(50)   NULL,
  trainer_nm     VARCHAR(100)  NULL,
  price          DECIMAL(12,2) NOT NULL DEFAULT 0,
  rating         DECIMAL(3,2)  NULL,
  feature_cd     VARCHAR(50)   NULL,
  status_cd      VARCHAR(30)   NOT NULL DEFAULT 'OPEN',
  description    TEXT          NULL,
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lms_enrollment (
  enrollment_id  BIGINT        NOT NULL AUTO_INCREMENT,
  course_id      BIGINT        NOT NULL,
  student_nm     VARCHAR(100)  NOT NULL,
  progress_pct   INT           NOT NULL DEFAULT 0,
  enrolled_dt    DATE          NOT NULL,
  PRIMARY KEY (enrollment_id),
  CONSTRAINT fk_lms_enrollment_course FOREIGN KEY (course_id) REFERENCES lms_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lms_trainer (
  trainer_id     BIGINT        NOT NULL AUTO_INCREMENT,
  trainer_nm     VARCHAR(100)  NOT NULL,
  email          VARCHAR(200)  NULL,
  bio            TEXT          NULL,
  avatar_url     VARCHAR(500)  NULL,
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (trainer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Email ==========
CREATE TABLE IF NOT EXISTS email_message (
  message_id     BIGINT        NOT NULL AUTO_INCREMENT,
  folder_cd      VARCHAR(20)   NOT NULL DEFAULT 'INBOX' COMMENT 'INBOX,SENT,DRAFT',
  subject        VARCHAR(500)  NOT NULL,
  sender_email   VARCHAR(200)  NOT NULL,
  recipient      VARCHAR(500)  NOT NULL,
  body_text      MEDIUMTEXT    NULL,
  read_yn        CHAR(1)       NOT NULL DEFAULT 'N',
  starred_yn     CHAR(1)       NOT NULL DEFAULT 'N',
  sent_dt        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (message_id),
  KEY idx_email_folder (folder_cd),
  KEY idx_email_sent (sent_dt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Calendar / Events ==========
CREATE TABLE IF NOT EXISTS calendar_event (
  event_id       BIGINT        NOT NULL AUTO_INCREMENT,
  title          VARCHAR(200)  NOT NULL,
  category_cd    VARCHAR(50)   NULL COMMENT 'EVENT_CATEGORY',
  label_cd       VARCHAR(50)   NULL COMMENT 'CALENDAR_EVENT_LABEL',
  start_dt       DATETIME      NOT NULL,
  end_dt         DATETIME      NULL,
  location       VARCHAR(255)  NULL,
  description    TEXT          NULL,
  all_day_yn     CHAR(1)       NOT NULL DEFAULT 'N',
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id      VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (event_id),
  KEY idx_calendar_event_start (start_dt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Social ==========
CREATE TABLE IF NOT EXISTS social_post (
  post_id        BIGINT        NOT NULL AUTO_INCREMENT,
  author_nm      VARCHAR(100)  NOT NULL,
  content        TEXT          NOT NULL,
  like_cnt       INT           NOT NULL DEFAULT 0,
  comment_cnt    INT           NOT NULL DEFAULT 0,
  posted_dt      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS social_activity (
  activity_id    BIGINT        NOT NULL AUTO_INCREMENT,
  actor_nm       VARCHAR(100)  NOT NULL,
  action_cd      VARCHAR(50)   NOT NULL,
  target_desc    VARCHAR(500)  NULL,
  activity_dt    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS social_notification (
  notification_id BIGINT       NOT NULL AUTO_INCREMENT,
  user_nm        VARCHAR(100)  NOT NULL,
  sender_nm      VARCHAR(100)  NULL COMMENT '발신 표시명',
  message        VARCHAR(500)  NOT NULL,
  section_cd     VARCHAR(20)   NULL DEFAULT 'NEW' COMMENT 'NEW, EARLIER',
  time_icon      VARCHAR(30)   NULL COMMENT '시간 아이콘',
  read_yn        CHAR(1)       NOT NULL DEFAULT 'N',
  notified_dt    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (notification_id),
  KEY idx_social_notification_user (user_nm, notified_dt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS social_follower (
  follower_id    BIGINT        NOT NULL AUTO_INCREMENT,
  user_nm        VARCHAR(100)  NOT NULL,
  follower_nm    VARCHAR(100)  NOT NULL,
  group_cd       VARCHAR(50)   NULL COMMENT 'FOLLOWER_GROUP',
  followed_dt    DATE          NOT NULL,
  PRIMARY KEY (follower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Kanban / Chat ==========
CREATE TABLE IF NOT EXISTS kanban_board (
  board_id       BIGINT        NOT NULL AUTO_INCREMENT,
  board_nm       VARCHAR(100)  NOT NULL,
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (board_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS kanban_column (
  column_id      BIGINT        NOT NULL AUTO_INCREMENT,
  board_id       BIGINT        NOT NULL,
  column_nm      VARCHAR(100)  NOT NULL,
  sort_ord       INT           NOT NULL DEFAULT 0,
  PRIMARY KEY (column_id),
  CONSTRAINT fk_kanban_column_board FOREIGN KEY (board_id) REFERENCES kanban_board (board_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS kanban_card (
  card_id        BIGINT        NOT NULL AUTO_INCREMENT,
  column_id      BIGINT        NOT NULL,
  title          VARCHAR(200)  NOT NULL,
  assignee_nm    VARCHAR(100)  NULL,
  due_dt         DATE          NULL,
  sort_ord       INT           NOT NULL DEFAULT 0,
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (card_id),
  CONSTRAINT fk_kanban_card_column FOREIGN KEY (column_id) REFERENCES kanban_column (column_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chat_room (
  room_id        BIGINT        NOT NULL AUTO_INCREMENT,
  room_nm        VARCHAR(100)  NOT NULL,
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chat_message (
  message_id     BIGINT        NOT NULL AUTO_INCREMENT,
  room_id        BIGINT        NOT NULL,
  sender_nm      VARCHAR(100)  NOT NULL,
  body_text      TEXT          NOT NULL,
  sent_dt        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (message_id),
  CONSTRAINT fk_chat_message_room FOREIGN KEY (room_id) REFERENCES chat_room (room_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Pages (Pricing / FAQ) ==========
CREATE TABLE IF NOT EXISTS pricing_plan (
  plan_id        BIGINT        NOT NULL AUTO_INCREMENT,
  plan_nm        VARCHAR(100)  NOT NULL,
  price_monthly  DECIMAL(12,2) NOT NULL DEFAULT 0,
  price_yearly   DECIMAL(12,2) NULL,
  feature_json   JSON          NULL,
  sort_ord       INT           NOT NULL DEFAULT 0,
  use_yn         CHAR(1)       NOT NULL DEFAULT 'Y',
  PRIMARY KEY (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS faq_item (
  faq_id         BIGINT        NOT NULL AUTO_INCREMENT,
  category_nm    VARCHAR(100)  NULL,
  question       VARCHAR(500)  NOT NULL,
  answer         TEXT          NOT NULL,
  sort_ord       INT           NOT NULL DEFAULT 0,
  use_yn         CHAR(1)       NOT NULL DEFAULT 'Y',
  PRIMARY KEY (faq_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
