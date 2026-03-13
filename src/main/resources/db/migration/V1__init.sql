CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(320) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       created_at DATETIME NOT NULL,
                       updated_at DATETIME NOT NULL
);

CREATE TABLE lectures (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          title VARCHAR(200) NOT NULL,
                          lecture_date DATETIME NOT NULL,
                          location VARCHAR(200) NULL,
                          lecture_summary TEXT NULL,
                          lecturer_name VARCHAR(100) NULL,
                          topic VARCHAR(100) NULL,
                          status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
                          created_by BIGINT NOT NULL,
                          created_at DATETIME NOT NULL,
                          updated_at DATETIME NOT NULL,
                          CONSTRAINT fk_lectures_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_lectures_date ON lectures(lecture_date);
CREATE INDEX idx_lectures_topic ON lectures(topic);
CREATE INDEX idx_lectures_status ON lectures(status);

CREATE TABLE articles (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          lecture_id BIGINT NOT NULL,
                          article_title VARCHAR(200) NOT NULL,
                          author VARCHAR(100) NULL,
                          content LONGTEXT NOT NULL,
                          created_at DATETIME NOT NULL,
                          updated_at DATETIME NOT NULL,
                          CONSTRAINT fk_articles_lecture FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE
);

CREATE INDEX idx_articles_lecture_id ON articles(lecture_id);

CREATE TABLE videos (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        lecture_id BIGINT NOT NULL,
                        video_url VARCHAR(2048) NOT NULL,
                        caption VARCHAR(200) NULL,
                        created_at DATETIME NOT NULL,
                        CONSTRAINT fk_videos_lecture FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE
);

CREATE INDEX idx_videos_lecture_id ON videos(lecture_id);
