CREATE TABLE calendar_sync_history (
    id                BIGSERIAL PRIMARY KEY,
    itinerary_id      BIGINT NOT NULL REFERENCES itinerary(id) ON DELETE CASCADE,
    activity_id       BIGINT REFERENCES itinerary_activity(id) ON DELETE CASCADE,
    google_event_id   VARCHAR(255),
    google_calendar_id VARCHAR(255) NOT NULL DEFAULT 'primary',
    sync_status       VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    synced_at         TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ
);

CREATE INDEX idx_calendar_sync_itinerary ON calendar_sync_history(itinerary_id);
CREATE INDEX idx_calendar_sync_activity  ON calendar_sync_history(activity_id);
