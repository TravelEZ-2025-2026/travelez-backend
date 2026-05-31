-- Add FLAGGED status to posts status enum if not exists
-- This allows posts to be flagged by AI moderation while remaining visible

DO $$
BEGIN
    -- Check if FLAGGED value exists in the enum
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum 
        WHERE enumlabel = 'FLAGGED' 
        AND enumtypid = (SELECT oid FROM pg_type WHERE typname = 'post_status')
    ) THEN
        -- Add FLAGGED to the enum
        ALTER TYPE post_status ADD VALUE 'FLAGGED';
    END IF;
END$$;

COMMENT ON TYPE post_status IS 'Post status: DRAFT, PUBLISHED, FLAGGED (by AI), BANNED (by admin)';
