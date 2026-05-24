-- NAS 저장 파일 메타 (이미지·문서·영상·상품)
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS nas_file (
  file_id        BIGINT        NOT NULL AUTO_INCREMENT COMMENT '파일ID',
  media_type_cd  VARCHAR(20)   NOT NULL COMMENT 'IMAGE|DOCUMENT|VIDEO|PRODUCT',
  stored_nm      VARCHAR(255)  NOT NULL COMMENT '저장 파일명 (UUID.ext)',
  original_nm    VARCHAR(500)  NULL COMMENT '원본 파일명',
  file_ext       VARCHAR(20)   NULL COMMENT '확장자',
  file_size      BIGINT        NOT NULL DEFAULT 0 COMMENT '파일 크기(byte)',
  file_path      VARCHAR(1000) NOT NULL COMMENT '디스크 절대경로',
  url_path       VARCHAR(500)  NOT NULL COMMENT '웹 URL 경로',
  content_type   VARCHAR(100)  NULL COMMENT 'Content-Type',
  reg_id         VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
  reg_dt         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (file_id),
  KEY idx_nas_file_type_dt (media_type_cd, reg_dt),
  KEY idx_nas_file_url (url_path(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
