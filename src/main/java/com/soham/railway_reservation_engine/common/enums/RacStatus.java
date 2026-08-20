package com.soham.railway_reservation_engine.common.enums;

/**
 * State of a RAC (Reservation Against Cancellation) entry — a shared side-lower berth
 * granted to two passengers. ACTIVE while waiting; PROMOTED once upgraded to a full
 * confirmed berth; CANCELLED when released.
 */
public enum RacStatus {
    ACTIVE,
    PROMOTED,
    CANCELLED
}
