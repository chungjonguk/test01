-- 배송·운송장 (CJ / 우체국 / 롯데)
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS ecm_shipment (
  shipment_id       BIGINT        NOT NULL AUTO_INCREMENT COMMENT '배송ID',
  order_id          BIGINT        NOT NULL COMMENT '주문ID',
  carrier_cd        VARCHAR(20)   NOT NULL COMMENT 'CJ,EPOST,LOTTE',
  invoice_no        VARCHAR(30)   NULL COMMENT '운송장번호',
  status_cd         VARCHAR(20)   NOT NULL DEFAULT 'REQUESTED' COMMENT 'REQUESTED,ISSUED,FAILED,CANCELLED',
  recipient_nm      VARCHAR(100)  NOT NULL,
  recipient_phone   VARCHAR(30)   NOT NULL,
  zipcode           VARCHAR(10)   NOT NULL,
  address           VARCHAR(255)  NOT NULL,
  address_detail    VARCHAR(255)  NULL,
  box_cnt           INT           NOT NULL DEFAULT 1,
  weight_kg         DECIMAL(6,2)  NULL,
  request_payload   MEDIUMTEXT    NULL,
  response_payload  MEDIUMTEXT    NULL,
  issued_dt         DATETIME      NULL,
  reg_id            VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (shipment_id),
  KEY idx_shipment_order (order_id),
  KEY idx_shipment_carrier (carrier_cd, status_cd),
  UNIQUE KEY uk_shipment_invoice (carrier_cd, invoice_no),
  CONSTRAINT fk_ecm_shipment_order FOREIGN KEY (order_id) REFERENCES ecm_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
