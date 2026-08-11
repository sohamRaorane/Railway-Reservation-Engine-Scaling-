-- copy the rac and waitlist data from quota_seat_allocations to quota_reservation_pool
INSERT INTO quota_reservation_pool (

    schedule_id,
    quota_id,
    rac_limit,
    rac_available,
    waitlist_limit,
    waitlist_available
)
SELECT DISTINCT
    schedule_id,
    quota_id,
    rac_limit,
    rac_available,
    waitlist_limit,
    waitlist_available
FROM quota_seat_allocations;