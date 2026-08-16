package com.soham.railway_reservation_engine.bookings.event;

public record  BookingCancelledEvent(
        Long bookingId,
        String pnr,
        Long scheduleId,
        Long quotaId
) {

}
