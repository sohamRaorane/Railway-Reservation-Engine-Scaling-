ALTER TABLE quota_seat_allocations
    DROP COLUMN rac_limit;

ALTER TABLE quota_seat_allocations
    DROP COLUMN rac_available;

ALTER TABLE quota_seat_allocations
    DROP COLUMN waitlist_limit;

ALTER TABLE quota_seat_allocations
    DROP COLUMN waitlist_available;