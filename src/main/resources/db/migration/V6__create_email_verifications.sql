CREATE TABLE email_verifications (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     code VARCHAR(10) NOT NULL,
                                     expires_at DATETIME NOT NULL,
                                     created_at DATETIME NOT NULL
);