-- 관광지 추천 챗봇 DB 스키마

CREATE DATABASE IF NOT EXISTS chatbot_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE chatbot_db;

CREATE TABLE IF NOT EXISTS users (
    user_id     VARCHAR(50)  NOT NULL,
    pw          VARCHAR(255) NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS chat_history (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     VARCHAR(50)  NOT NULL,
    message     TEXT         NOT NULL,
    response    TEXT         NOT NULL,
    created_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_chat_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS favorites (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    user_id       VARCHAR(50)   NOT NULL,
    place_name    VARCHAR(255)  NOT NULL,
    place_address VARCHAR(500),
    lat           DOUBLE,
    lng           DOUBLE,
    memo          TEXT,
    created_at    DATETIME      NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_fav_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS spots (
    content_id      VARCHAR(50)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    addr1           VARCHAR(500),
    addr2           VARCHAR(500),
    first_image     VARCHAR(500),
    first_image2    VARCHAR(500),
    mapx            DOUBLE,
    mapy            DOUBLE,
    area_code       VARCHAR(20),
    sigungu_code    VARCHAR(20),
    content_type_id VARCHAR(20),
    PRIMARY KEY (content_id)
);

CREATE TABLE IF NOT EXISTS travel_plans (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    user_id        VARCHAR(50)   NOT NULL,
    title          VARCHAR(255)  NOT NULL,
    places_json    TEXT          NOT NULL,
    total_distance DOUBLE        NOT NULL,
    created_at     DATETIME      NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_plan_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS notices (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(255) NOT NULL,
    content     TEXT         NOT NULL,
    author_id   VARCHAR(50)  NOT NULL,
    author_name VARCHAR(50)  NOT NULL,
    is_pinned   BOOLEAN      DEFAULT FALSE,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS posts (
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
);