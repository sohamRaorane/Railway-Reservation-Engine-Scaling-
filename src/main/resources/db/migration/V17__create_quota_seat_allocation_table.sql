CREATE TABLE quota_seat_allocations
(
    id BIGSERIAL PRIMARY KEY,

    schedule_id BIGINT NOT NULL,

    quota_id BIGINT NOT NULL,

    total_seats INT NOT NULL,

    available_seats INT NOT NULL,

    rac_limit INT NOT NULL,

    rac_available INT NOT NULL,

    waitlist_limit INT NOT NULL,

    waitlist_available INT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_qsa_schedule
        FOREIGN KEY (schedule_id)
            REFERENCES schedules(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_qsa_quota
        FOREIGN KEY (quota_id)
            REFERENCES quotas(id)
            ON DELETE CASCADE,

    CONSTRAINT uq_schedule_quota
        UNIQUE(schedule_id, quota_id)
);