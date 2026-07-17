CREATE TABLE email_verifications (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL,
                                     purpose VARCHAR(30) NOT NULL,
                                     code VARCHAR(10) NOT NULL,
                                     expires_at DATETIME NOT NULL,
                                     created_at DATETIME NOT NULL,

                                     CONSTRAINT uk_email_verification_email_purpose
                                         UNIQUE (email, purpose)
);