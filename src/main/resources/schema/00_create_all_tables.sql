-- =============================================================================
-- spring_boot_app 전체 테이블 CREATE (DBeaver: 이 파일 SQL 편집기에서 전체 실행)
-- 계정: redcroxx / DB: spring_boot_app / utf8mb4
-- =============================================================================

-- ########## 00_create_database.sql ##########
-- DB 생성 (root 또는 CREATE 권한 계정으로 1회 실행)
CREATE DATABASE IF NOT EXISTS spring_boot_app
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE spring_boot_app;

-- ########## user.sql ##########
-- spring_boot_app.user 테이블 (MySQL 8.4)
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS `user` (
  `id`        VARCHAR(100) NOT NULL COMMENT 'id',
  `pw`        VARCHAR(100) NOT NULL COMMENT 'password',
  `name`      VARCHAR(100) NOT NULL COMMENT 'name',
  `sex`       VARCHAR(100) NOT NULL COMMENT 'sex',
  `rrno`      VARCHAR(255) NOT NULL COMMENT 'rrno (AES 암호문 저장)',
  `email`          VARCHAR(100) NOT NULL,
  `zipcode`        VARCHAR(10) NULL COMMENT '우편번호',
  `address`        VARCHAR(255) NULL COMMENT '기본주소',
  `address_detail` VARCHAR(255) NULL COMMENT '상세주소',
  `update_id` VARCHAR(100) NOT NULL COMMENT 'update_id',
  `reg_dt`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'registration datetime',
  `upd_dt`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update datetime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_unique` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ########## user_access_log.sql ##########
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS user_access_log (
  access_id       BIGINT        NOT NULL AUTO_INCREMENT COMMENT '접속이력ID',
  user_id         VARCHAR(100)  NULL COMMENT '사용자ID (user.id, 실패 시 시도 ID)',
  user_nm         VARCHAR(100)  NULL COMMENT '접속 시점 표시명',
  access_type_cd  VARCHAR(30)   NOT NULL COMMENT 'LOGIN|LOGOUT|PAGE',
  login_type_cd   VARCHAR(30)   NULL COMMENT 'FORM|KAKAO|NAVER',
  success_yn      CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '성공여부 Y/N',
  request_uri     VARCHAR(500)  NULL COMMENT '요청 URI',
  http_method     VARCHAR(10)   NULL COMMENT 'HTTP 메서드',
  client_ip       VARCHAR(45)   NULL COMMENT '클라이언트 IP',
  user_agent      VARCHAR(500)  NULL COMMENT 'User-Agent',
  session_id      VARCHAR(64)   NULL COMMENT 'HTTP 세션 ID',
  fail_reason     VARCHAR(255)  NULL COMMENT '실패 사유',
  access_dt       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '접속일시',
  reg_id          VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  PRIMARY KEY (access_id),
  KEY idx_user_access_log_user_dt (user_id, access_dt),
  KEY idx_user_access_log_dt (access_dt),
  KEY idx_user_access_log_type (access_type_cd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ########## ecm_payment.sql ##########
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS ecm_payment (
  payment_id      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '결제ID',
  order_no        VARCHAR(64)   NOT NULL COMMENT '가맹점 주문번호(oid)',
  order_id        BIGINT        NULL COMMENT 'ecm_order.order_id',
  pg_cd           VARCHAR(20)   NOT NULL DEFAULT 'INICIS' COMMENT 'PG사',
  mid             VARCHAR(20)   NULL COMMENT '상점ID',
  tid             VARCHAR(100)  NULL COMMENT '이니시스 거래번호',
  amount          DECIMAL(12,2) NOT NULL COMMENT '결제금액',
  currency_cd     VARCHAR(10)   NOT NULL DEFAULT 'WON',
  good_name       VARCHAR(200)  NOT NULL,
  buyer_name      VARCHAR(100)  NULL,
  buyer_tel       VARCHAR(30)   NULL,
  buyer_email     VARCHAR(200)  NULL,
  status_cd       VARCHAR(30)   NOT NULL DEFAULT 'READY' COMMENT 'READY|PENDING_AUTH|PAID|FAILED|CANCELLED',
  result_code     VARCHAR(20)   NULL,
  result_msg      VARCHAR(500)  NULL,
  auth_token      VARCHAR(500)  NULL,
  idc_name        VARCHAR(50)   NULL,
  raw_auth_json   TEXT          NULL,
  raw_approve_json TEXT         NULL,
  reg_id          VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (payment_id),
  UNIQUE KEY uk_ecm_payment_order_no (order_no),
  KEY idx_ecm_payment_status (status_cd),
  KEY idx_ecm_payment_tid (tid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ########## common_code.sql ##########
-- 공통코드 마스터·상세 (신규 DB용)
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS common_code (
  code_id      VARCHAR(50)   NOT NULL COMMENT '코드그룹ID',
  code_nm      VARCHAR(200)  NOT NULL COMMENT '코드그룹명',
  use_yn       CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  reg_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  regdate_dt   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id    VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (code_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS common_code_value (
  code_id      VARCHAR(50)   NOT NULL COMMENT '코드그룹ID',
  code_val     VARCHAR(100)  NOT NULL COMMENT '코드값',
  use_yn       CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  reg_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  regdate_dt   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id    VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (code_id, code_val),
  CONSTRAINT fk_common_code_value_group
    FOREIGN KEY (code_id) REFERENCES common_code (code_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ########## screen_list.sql ##########
-- 화면 마스터 (신규 DB용). 기존 테이블이 있으면 screen_list_alter.sql 사용
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS screen_list (
  screen_id      VARCHAR(100)  NOT NULL COMMENT '화면ID',
  screen_nm      VARCHAR(200)  NOT NULL COMMENT '화면명',
  uri_path       VARCHAR(255)  NOT NULL COMMENT 'URL 경로',
  template_path  VARCHAR(255)  NULL COMMENT 'Thymeleaf 템플릿',
  sort_ord       INT           NOT NULL DEFAULT 0 COMMENT '정렬순서',
  use_yn         CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  reg_id         VARCHAR(100)  NOT NULL COMMENT '등록자',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id      VARCHAR(100)  NOT NULL COMMENT '수정자',
  update_dt      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (screen_id),
  UNIQUE KEY uk_screen_list_uri (uri_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ########## screen_table_map.sql ##########
-- 화면(URL)별 연동 테이블 매핑
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS screen_table_map (
  uri_path        VARCHAR(255)  NOT NULL COMMENT 'URL 경로',
  screen_id       VARCHAR(100)  NULL COMMENT '화면ID(screen_list)',
  primary_table   VARCHAR(64)   NULL COMMENT '주 테이블(NULL=정적/미연동)',
  related_tables  VARCHAR(500)  NULL COMMENT '참조·상세 테이블(쉼표 구분)',
  data_type       CHAR(1)       NOT NULL DEFAULT 'S' COMMENT 'D=DB CRUD,C=공통코드만,S=정적 UI',
  table_desc      VARCHAR(500)  NULL COMMENT '화면별 테이블 설명',
  reg_id          VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id       VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (uri_path),
  KEY idx_screen_table_map_screen (screen_id),
  KEY idx_screen_table_map_primary (primary_table)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ########## tables\biz_schema.sql ##########
-- 업무 화면(/app/** 등)용 테이블 — Falcon UI 목록·상세와 1:1 대응
USE spring_boot_app;

-- ========== E-Commerce ==========
CREATE TABLE IF NOT EXISTS ecm_product (
  product_id     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '상품ID',
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
  KEY idx_ecm_product_status (status_cd)
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
  message        VARCHAR(500)  NOT NULL,
  read_yn        CHAR(1)       NOT NULL DEFAULT 'N',
  notified_dt    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (notification_id)
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
