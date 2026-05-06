INSERT INTO users (
    email,
    password_hash,
    role,
    created_at,
    updated_at
)
SELECT
    'admin@example.com',
    '$2a$10$Tqv01ZYj7Rns5pMBjnxFMeWbeabO9eVAJAGqS7m0Iu23FxxpOlgJu',
    'ADMIN',
    NOW(),
    NOW()
    WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'admin@example.com'
);