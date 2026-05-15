CREATE TABLE IF NOT EXISTS `user` (
    id          VARCHAR(100) NOT NULL PRIMARY KEY,
    pw          VARCHAR(100) NOT NULL,
    name        VARCHAR(100) NOT NULL,
    sex         VARCHAR(100) NOT NULL,
    rrno        VARCHAR(255) NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    zipcode        VARCHAR(10),
    address        VARCHAR(255),
    address_detail VARCHAR(255),
    update_id   VARCHAR(100) NOT NULL,
    reg_dt      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    upd_dt      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shop_orders (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number  VARCHAR(32) NOT NULL UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    amount        DECIMAL(12, 2) NOT NULL,
    status        VARCHAR(30) NOT NULL,
    created_at    TIMESTAMP NOT NULL
);
