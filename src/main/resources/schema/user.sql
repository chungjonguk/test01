-- spring_boot_app.user 테이블 (MySQL 8.4)
USE spring_boot_app;

CREATE TABLE IF NOT EXISTS `user` (
  `id`        VARCHAR(100) NOT NULL COMMENT 'id',
  `pw`        VARCHAR(100) NOT NULL COMMENT 'password',
  `name`      VARCHAR(100) NOT NULL COMMENT 'name',
  `sex`       VARCHAR(100) NOT NULL COMMENT 'sex',
  `rrno`      VARCHAR(255) NOT NULL COMMENT 'rrno (AES 암호문 저장)',
  `email`          VARCHAR(100) NOT NULL,
  `zipcode`        VARCHAR(10) NULL COMMENT '우편번호',
  `address`        VARCHAR(255) NULL COMMENT '기본주소',
  `address_detail` VARCHAR(255) NULL COMMENT '상세주소',
  `update_id` VARCHAR(100) NOT NULL COMMENT 'update_id',
  `reg_dt`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'registration datetime',
  `upd_dt`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update datetime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_unique` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
