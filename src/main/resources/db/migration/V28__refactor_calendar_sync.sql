ALTER TABLE itinerary ADD COLUMN calendar_synced_at TIMESTAMPTZ NULL;

DROP TABLE IF EXISTS calendar_sync_history;
