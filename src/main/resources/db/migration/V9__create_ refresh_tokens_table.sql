CREATE TABLE refresh_tokens
(
    id BIGSERIAL PRIMARY KEY,

    token VARCHAR(255) NOT NULL UNIQUE,

    user_id BIGINT NOT NULL,

    expiry_date TIMESTAMP NOT NULL,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE --> suppose it gets deleted, then all the refresh tokens associated with that user will also be deleted
);