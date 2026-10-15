CREATE TABLE user_favorite_lectures (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    lecture_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT fk_user_favorite_lectures_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_favorite_lectures_lecture
        FOREIGN KEY (lecture_id) REFERENCES lectures(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_user_favorite_lecture UNIQUE (user_id, lecture_id)
);