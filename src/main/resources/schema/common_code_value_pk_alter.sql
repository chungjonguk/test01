-- 상세코드 다건 저장: PK를 (code_id, code_val) 복합키로 재적용 (PostgreSQL)
-- 이미 동일 복합 PK여도 재생성되며, 테이블 없으면 continueOnError로 무시됩니다.

ALTER TABLE common_code_value DROP CONSTRAINT IF EXISTS common_code_value_pkey;
ALTER TABLE common_code_value ADD PRIMARY KEY (code_id, code_val);
