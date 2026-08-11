CREATE TABLE bookings
(
    id BIGSERIAL PRIMARY KEY,

    pnr VARCHAR(15) NOT NULL UNIQUE,

    user_id BIGINT NOT NULL,

    schedule_id BIGINT NOT NULL,

    quota_id BIGINT NOT NULL,

    booking_status VARCHAR(30) NOT NULL,

    total_fare NUMERIC(10,2) NOT NULL,

    idempotency_key VARCHAR(255) UNIQUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT fk_booking_schedule
        FOREIGN KEY (schedule_id)
            REFERENCES schedules(id),

    CONSTRAINT fk_booking_quota
        FOREIGN KEY (quota_id)
            REFERENCES quotas(id)
);