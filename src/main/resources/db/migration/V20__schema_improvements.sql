-- ============================================================
-- V20 : Coach-wise Quota Seat Allocation
-- ============================================================

-- ------------------------------------------------------------
-- Step 1 : Add coach_id column
-- ------------------------------------------------------------
ALTER TABLE quota_seat_allocations
    ADD COLUMN coach_id BIGINT;

-- ------------------------------------------------------------
-- Step 2 : Add Foreign Key
-- ------------------------------------------------------------
ALTER TABLE quota_seat_allocations
    ADD CONSTRAINT fk_qsa_coach
        FOREIGN KEY (coach_id)
            REFERENCES coaches(id)
            ON DELETE CASCADE;

-- ------------------------------------------------------------
-- Step 3 : Make coach_id mandatory
-- ------------------------------------------------------------
ALTER TABLE quota_seat_allocations
    ALTER COLUMN coach_id SET NOT NULL;

-- ------------------------------------------------------------
-- Step 4 : Drop old unique constraint
-- ------------------------------------------------------------
ALTER TABLE quota_seat_allocations
    DROP CONSTRAINT uq_schedule_quota;

-- ------------------------------------------------------------
-- Step 5 : Add new unique constraint
-- ------------------------------------------------------------
ALTER TABLE quota_seat_allocations
    ADD CONSTRAINT uq_schedule_coach_quota
        UNIQUE (schedule_id, coach_id, quota_id);

-- ------------------------------------------------------------
-- Step 6 : Add data integrity constraints
-- ------------------------------------------------------------
ALTER TABLE quota_seat_allocations
    ADD CONSTRAINT chk_qsa_available_seats
        CHECK (available_seats <= total_seats);

ALTER TABLE quota_seat_allocations
    ADD CONSTRAINT chk_qsa_rac_available
        CHECK (rac_available <= rac_limit);

ALTER TABLE quota_seat_allocations
    ADD CONSTRAINT chk_qsa_waitlist_available
        CHECK (waitlist_available <= waitlist_limit);

-- ------------------------------------------------------------
-- Step 7 : Index for coach lookups
-- ------------------------------------------------------------
CREATE INDEX idx_qsa_coach
    ON quota_seat_allocations(coach_id);