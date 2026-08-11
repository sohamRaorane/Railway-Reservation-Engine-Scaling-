
ALTER TABLE waitlist
    ADD CONSTRAINT uk_waitlist_schedule_quota_number
        UNIQUE (schedule_id, quota_id, waitlist_number);