CREATE SCHEMA IF NOT EXISTS my_first_bot;

CREATE TABLE IF NOT EXISTS my_first_bot.locations (
    id BIGSERIAL,
    name CHARACTER VARYING(50) NOT NULL,
    latitude CHARACTER VARYING(50) NOT NULL,
    longitude CHARACTER VARYING(50) NOT NULL,
    country CHARACTER VARYING(50) NOT NULL,
    state CHARACTER VARYING(50),
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS my_first_bot.users (
    chat_id BIGINT,
    location BIGINT NOT NULL,
    PRIMARY KEY(chat_id),
    CONSTRAINT user_location FOREIGN KEY(location) REFERENCES my_first_bot.locations(id)
);

