-- spring_boot_app.user 테이블 (PostgreSQL 16)
-- USE spring_boot_app;

CREATE TABLE IF NOT EXISTS "user" (
  id        VARCHAR(100) NOT NULL,
  pw        VARCHAR(100) NOT NULL,
  name      VARCHAR(100) NOT NULL,
  sex       VARCHAR(100) NOT NULL,
  rrno      VARCHAR(255) NOT NULL,
  email          VARCHAR(100) NOT NULL,
  zipcode        VARCHAR(10) NULL,
  address        VARCHAR(255) NULL,
  address_detail VARCHAR(255) NULL,
  update_id VARCHAR(100) NOT NULL,
  reg_dt    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  upd_dt    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT user_unique UNIQUE (email)
);
