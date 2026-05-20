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
