-- Create banned_keywords table for content moderation
CREATE TABLE banned_keywords (
    id BIGSERIAL PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL UNIQUE,
    violation_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_banned_keywords_active ON banned_keywords(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_banned_keywords_violation_type ON banned_keywords(violation_type);
CREATE INDEX idx_banned_keywords_severity ON banned_keywords(severity);

-- Add comment
COMMENT ON TABLE banned_keywords IS 'Danh sách từ khóa cấm cho kiểm duyệt nội dung';
