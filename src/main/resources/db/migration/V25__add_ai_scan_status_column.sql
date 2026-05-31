-- Add ai_scan_status column to posts and review tables
-- Separates AI scan state from public visibility status

ALTER TABLE posts ADD COLUMN ai_scan_status VARCHAR(20) NOT NULL DEFAULT 'PENDING_SCAN';
ALTER TABLE review ADD COLUMN ai_scan_status VARCHAR(20) NOT NULL DEFAULT 'PENDING_SCAN';

-- Migrate rows that were FLAGGED: keep aiScanStatus = FLAGGED, reset main status
UPDATE posts SET ai_scan_status = 'FLAGGED', status = 'PUBLISHED' WHERE status = 'FLAGGED';
UPDATE review SET ai_scan_status = 'FLAGGED', status = 'ACTIVE' WHERE status = 'FLAGGED';

-- Remove FLAGGED from post_status enum
ALTER TYPE post_status RENAME TO post_status_old;
CREATE TYPE post_status AS ENUM ('PUBLISHED', 'ARCHIVED', 'BANNED');
ALTER TABLE posts ALTER COLUMN status TYPE post_status USING status::text::post_status;
DROP TYPE post_status_old;

-- Remove FLAGGED from review_status_enum
ALTER TYPE review_status_enum RENAME TO review_status_enum_old;
CREATE TYPE review_status_enum AS ENUM ('ACTIVE', 'BANNED');
ALTER TABLE review ALTER COLUMN status TYPE review_status_enum USING status::text::review_status_enum;
DROP TYPE review_status_enum_old;
