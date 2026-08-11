CREATE TABLE pnr_state_history
(
    id BIGSERIAL PRIMARY KEY,

    booking_id BIGINT NOT NULL,

    previous_status VARCHAR(30),

    current_status VARCHAR(30) NOT NULL,

    remarks VARCHAR(255),

    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pnr_history_booking
        FOREIGN KEY (booking_id)
            REFERENCES bookings(id)
            ON DELETE CASCADE
);