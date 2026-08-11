CREATE TABLE payments
(
    id BIGSERIAL PRIMARY KEY,

    booking_id BIGINT NOT NULL,

    amount DECIMAL(10,2) NOT NULL,

    payment_method VARCHAR(30) NOT NULL,

    payment_status VARCHAR(20) NOT NULL,

    transaction_id VARCHAR(100) UNIQUE,

    paid_at TIMESTAMP,

    CONSTRAINT fk_payment_booking
        FOREIGN KEY (booking_id)
            REFERENCES bookings(id)
);