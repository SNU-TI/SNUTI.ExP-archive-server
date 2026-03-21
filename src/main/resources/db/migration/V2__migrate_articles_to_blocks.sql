CREATE TABLE article_blocks (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                article_id BIGINT NOT NULL,
                                block_type VARCHAR(20) NOT NULL,
                                order_index INT NOT NULL,
                                text_content LONGTEXT NULL,
                                image_key VARCHAR(500) NULL,
                                original_file_name VARCHAR(255) NULL,
                                CONSTRAINT fk_article_blocks_article
                                    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
                                CONSTRAINT chk_article_blocks_type
                                    CHECK (block_type IN ('TEXT', 'IMAGE'))
);

CREATE INDEX idx_article_blocks_article_id ON article_blocks(article_id);
CREATE UNIQUE INDEX uq_article_blocks_article_order ON article_blocks(article_id, order_index);

INSERT INTO article_blocks (
    article_id,
    block_type,
    order_index,
    text_content,
    image_key,
    original_file_name
)
SELECT
    id,
    'TEXT',
    0,
    content,
    NULL,
    NULL
FROM articles;

ALTER TABLE articles
DROP COLUMN content;