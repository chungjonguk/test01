-- MySQL 8.4 + JDBC/DBeaver: "Public Key Retrieval is not allowed"
--
-- [Spring Boot] application.properties 에 이미 설정됨:
--   allowPublicKeyRetrieval=true&useSSL=false
--
-- [DBeaver] 연결 편집 → Driver properties → 추가:
--   allowPublicKeyRetrieval = true
--   useSSL = false
--
-- [JDBC URL 예시]
--   jdbc:mysql://127.0.0.1:3306/spring_boot_app?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Seoul
--
-- 계정 비밀번호 재설정이 필요할 때만 root 로 실행:
-- ALTER USER 'redcroxx'@'localhost' IDENTIFIED BY 'jonguk0412';
-- ALTER USER 'redcroxx'@'127.0.0.1' IDENTIFIED BY 'jonguk0412';

SELECT '설정은 클라이언트(allowPublicKeyRetrieval=true)에서 적용하세요.' AS guide;
