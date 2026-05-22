-- 상품 이미지 (최대 5장 / 상품)
USE spring_boot_app;

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
