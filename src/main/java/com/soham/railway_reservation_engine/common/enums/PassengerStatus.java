package com.soham.railway_reservation_engine.common.enums;

/**
 * Per-passenger seat status, tracked independently of the booking-level status.
 *
 * <p>A single booking can contain a mix — e.g. two CONFIRMED passengers and one WAITLISTED —
 * which is why the passenger row carries its own status alongside {@link BookingStatus}.
 */
public enum PassengerStatus {
    CONFIRMED,
    RAC,
    WAITLISTED,
    CANCELLED

}
