CREATE TABLE tags (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      name VARCHAR(100) NOT NULL UNIQUE,
                      created_at DATETIME NOT NULL,
                      updated_at DATETIME NOT NULL
);

CREATE TABLE lecture_tags (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              lecture_id BIGINT NOT NULL,
                              tag_id BIGINT NOT NULL,
                              created_at DATETIME NOT NULL,
                              updated_at DATETIME NOT NULL,

                              CONSTRAINT fk_lecture_tags_lecture
                                  FOREIGN KEY (lecture_id) REFERENCES lectures(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_lecture_tags_tag
                                  FOREIGN KEY (tag_id) REFERENCES tags(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT uk_lecture_tag UNIQUE (lecture_id, tag_id)
);