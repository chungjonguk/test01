-- 업체별 페이지 이미지 (슬롯당 1건)
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS biz_company_page_image (
  image_id      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '이미지ID',
  company_id    BIGINT        NOT NULL COMMENT '업체ID',
  page_cd       VARCHAR(50)   NOT NULL COMMENT '페이지슬롯코드',
  nas_file_id   BIGINT        NULL COMMENT 'nas_file.file_id',
  url_path      VARCHAR(500)  NOT NULL COMMENT '공개 URL',
  alt_text      VARCHAR(200)  NULL COMMENT '대체텍스트',
  use_yn        CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  reg_id        VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_id     VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  update_dt     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (image_id),
  UNIQUE KEY uk_company_page (company_id, page_cd),
  KEY idx_company_page_company (company_id),
  KEY idx_company_page_file (nas_file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
