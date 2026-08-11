CREATE TABLE passengers
(
    id BIGSERIAL PRIMARY KEY,

    booking_id BIGINT NOT NULL,

    name VARCHAR(100) NOT NULL,

    age INT NOT NULL,

    gender VARCHAR(10) NOT NULL,

    berth_preference VARCHAR(15),

    seat_id BIGINT,

    passenger_status VARCHAR(20) NOT NULL,

    CONSTRAINT fk_passenger_booking
        FOREIGN KEY (booking_id)
            REFERENCES bookings(id),

    CONSTRAINT fk_passenger_seat
        FOREIGN KEY (seat_id)
            REFERENCES seats(id)
);