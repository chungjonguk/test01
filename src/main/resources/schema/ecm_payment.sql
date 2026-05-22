-- 이니시스 등 PG 결제 이력
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
