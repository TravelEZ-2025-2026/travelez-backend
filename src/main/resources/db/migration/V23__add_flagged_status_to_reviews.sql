-- Add FLAGGED status to review status enum if not exists
-- This allows reviews to be flagged by AI moderation while remaining visible

DO $$
BEGIN
    -- Check if FLAGGED value exists in the review_status_enum
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum 
        WHERE enumlabel = 'FLAGGED' 
        AND enumtypid = (SELECT oid FROM pg_type WHERE typname = 'review_status_enum')
    ) THEN
        -- Add FLAGGED to the enum
        ALTER TYPE review_status_enum ADD VALUE 'FLAGGED';
    END IF;
END$$;

COMMENT ON TYPE review_status_enum IS 'Review status: ACTIVE, FLAGGED (by AI), BANNED (by admin)';
