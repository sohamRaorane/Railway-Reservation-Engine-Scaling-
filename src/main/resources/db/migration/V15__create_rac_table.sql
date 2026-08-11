CREATE TABLE rac
(
    id BIGSERIAL PRIMARY KEY,

    schedule_id BIGINT NOT NULL,

    passenger_id BIGINT NOT NULL UNIQUE,

    seat_id BIGINT,

    rac_number INT NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_rac_schedule
        FOREIGN KEY (schedule_id)
            REFERENCES schedules(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_rac_passenger
        FOREIGN KEY (passenger_id)
            REFERENCES passengers(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_rac_seat
        FOREIGN KEY (seat_id)
            REFERENCES seats(id)
);