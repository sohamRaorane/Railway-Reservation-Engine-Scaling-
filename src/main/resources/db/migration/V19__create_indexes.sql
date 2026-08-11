CREATE INDEX idx_schedules_train_date
    ON schedules(train_id, journey_date);

CREATE INDEX idx_bookings_user
    ON bookings(user_id);

CREATE INDEX idx_bookings_schedule
    ON bookings(schedule_id);

CREATE INDEX idx_passengers_booking
    ON passengers(booking_id);

CREATE INDEX idx_passengers_seat
    ON passengers(seat_id);

CREATE INDEX idx_routes_train
    ON routes(train_id);

CREATE INDEX idx_coaches_train
    ON coaches(train_id);

CREATE INDEX idx_qsa_schedule
    ON quota_seat_allocations(schedule_id);

CREATE INDEX idx_qsa_quota
    ON quota_seat_allocations(quota_id);

CREATE INDEX idx_rac_schedule
    ON rac(schedule_id);

CREATE INDEX idx_waitlist_schedule
    ON waitlist(schedule_id);

CREATE INDEX idx_waitlist_schedule_quota
    ON waitlist(schedule_id, quota_id);

CREATE INDEX idx_pnr_history_booking
    ON pnr_state_history(booking_id);

CREATE INDEX idx_payments_booking
    ON payments(booking_id);

CREATE INDEX idx_refresh_tokens_user
    ON refresh_tokens(user_id);