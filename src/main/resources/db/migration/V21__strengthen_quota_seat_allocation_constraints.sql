
ALTER TABLE quota_seat_allocations
    DROP CONSTRAINT IF EXISTS chk_qsa_available_seats;

ALTER TABLE quota_seat_allocations
    DROP CONSTRAINT IF EXISTS chk_qsa_rac_available;

ALTER TABLE quota_seat_allocations
    DROP CONSTRAINT IF EXISTS chk_qsa_waitlist_available;


ALTER TABLE quota_seat_allocations
    ADD CONSTRAINT chk_qsa_available_seats
        CHECK (
            total_seats >= 0
                AND available_seats >= 0
                AND available_seats <= total_seats
            );


ALTER TABLE quota_seat_allocations
    ADD CONSTRAINT chk_qsa_rac_available
        CHECK (
            rac_limit >= 0
                AND rac_available >= 0
                AND rac_available <= rac_limit
            );


ALTER TABLE quota_seat_allocations
    ADD CONSTRAINT chk_qsa_waitlist_available
        CHECK (
            waitlist_limit >= 0
                AND waitlist_available >= 0
                AND waitlist_available <= waitlist_limit
            );