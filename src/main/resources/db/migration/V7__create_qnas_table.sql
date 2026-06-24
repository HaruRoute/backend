CREATE TABLE IF NOT EXISTS qnas (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(255) NOT NULL,
    content     TEXT         NOT NULL,
    author_id   VARCHAR(50)  NOT NULL,
    author_name VARCHAR(50)  NOT NULL,
    view_count  INT          DEFAULT 0,
    route_data  TEXT,
    route_name  VARCHAR(255),
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME,
    PRIMARY KEY (id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
