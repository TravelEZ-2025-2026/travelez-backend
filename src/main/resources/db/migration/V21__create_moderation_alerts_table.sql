-- Create moderation_alerts table for AI moderation results
CREATE TABLE moderation_alerts (
    id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    violation_type VARCHAR(50) NOT NULL,
    confidence_score DOUBLE PRECISION NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    admin_note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_moderation_alert_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for performance
CREATE INDEX idx_moderation_alerts_status ON moderation_alerts(status);
CREATE INDEX idx_moderation_alerts_target ON moderation_alerts(target_type, target_id);
CREATE INDEX idx_moderation_alerts_created_at ON moderation_alerts(created_at DESC);
CREATE INDEX idx_moderation_alerts_violation_type ON moderation_alerts(violation_type);

-- Add comment
COMMENT ON TABLE moderation_alerts IS 'Cảnh báo vi phạm từ AI moderation';
