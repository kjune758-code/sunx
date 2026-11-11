CREATE TABLE IF NOT EXISTS signal (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(255),
    strategy VARCHAR(255),
    side VARCHAR(255),
    status VARCHAR(255),
    risk_message VARCHAR(255),
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMP(6),
    version BIGINT
);
