-- 상세코드 다건 저장: PK를 (code_id, code_val) 복합키로 변경
-- code_id 단일 PK인 경우 그룹당 상세 1건만 INSERT 가능합니다.
-- 실행 전 백업 권장: mysql -u springuser -p spring_boot_app < src/main/resources/schema/common_code_value_pk_alter.sql

ALTER TABLE common_code_value
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (code_id, code_val);
