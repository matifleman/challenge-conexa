-- citext makes usernames case-insensitive ("Luke" = "luke"), as Spring's reference schema intends with varchar_ignorecase
CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
    username CITEXT       NOT NULL,
    password VARCHAR(500) NOT NULL,
    enabled  BOOLEAN      NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (username),
    CONSTRAINT ck_users_username_length CHECK (char_length(username) <= 50)
);

CREATE TABLE authorities (
    username  CITEXT      NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE,
    CONSTRAINT uk_authorities_username_authority UNIQUE (username, authority)
);
