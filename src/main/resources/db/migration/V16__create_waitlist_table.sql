CREATE TABLE waitlist
(
    id BIGSERIAL PRIMARY KEY,

    schedule_id BIGINT NOT NULL,

    quota_id BIGINT NOT NULL,

    passenger_id BIGINT NOT NULL UNIQUE,

    waitlist_number INT NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_waitlist_schedule
        FOREIGN KEY (schedule_id)
            REFERENCES schedules(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_waitlist_quota
        FOREIGN KEY (quota_id)
            REFERENCES quotas(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_waitlist_passenger
        FOREIGN KEY (passenger_id)
            REFERENCES passengers(id)
            ON DELETE CASCADE,

    CONSTRAINT uq_waitlist_position
        UNIQUE(schedule_id, quota_id, waitlist_number)
);