package com.soham.railway_reservation_engine.bookings.event;

//kafka event --> small immutable DTO --> contains only information required by the consumer
public record BookingCreatedEvent(

        Long bookingId,
        String pnr,
        Long scheduleId,
        Long quotaId
) {
}

