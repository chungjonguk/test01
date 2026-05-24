-- redcroxx 계정 생성 (DBeaver에서 root 계정으로 실행)
-- FLUSH PRIVILEGES 는 root 전용 → 아래 스크립트에는 포함하지 않음 (대부분 생략 가능)

CREATE DATABASE IF NOT EXISTS spring_boot_app
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'redcroxx'@'localhost' IDENTIFIED BY 'jonguk0412';
CREATE USER IF NOT EXISTS 'redcroxx'@'127.0.0.1' IDENTIFIED BY 'jonguk0412';

GRANT ALL PRIVILEGES ON spring_boot_app.* TO 'redcroxx'@'localhost';
GRANT ALL PRIVILEGES ON spring_boot_app.* TO 'redcroxx'@'127.0.0.1';

-- 권한 확인 (실행 결과에 redcroxx 가 보이면 성공)
SELECT user, host FROM mysql.user WHERE user = 'redcroxx';
