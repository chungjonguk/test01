-- 사용자 접속(로그인·로그아웃·화면) 이력
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
  device_type_cd  VARCHAR(20)   NULL COMMENT '장비유형 DESKTOP|MOBILE|TABLET|UNKNOWN',
  device_os       VARCHAR(80)   NULL COMMENT '장비 OS',
  device_browser  VARCHAR(80)   NULL COMMENT '브라우저',
  device_model    VARCHAR(120)  NULL COMMENT '장비 모델명',
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
