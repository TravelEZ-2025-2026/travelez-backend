ALTER TABLE user_oauth_tokens
    ADD COLUMN scopes TEXT,
    ADD COLUMN expires_at TIMESTAMPTZ;
