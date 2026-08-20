package com.soham.railway_reservation_engine.common.enums;

/**
 * The passenger's requested berth position (what they ask for at booking time).
 * Distinct from {@link BerthType}, which is what was actually allocated on the seat.
 */
public enum BerthPreference {
    LOWER,
    MIDDLE,
    UPPER,
    SIDE_LOWER,
    SIDE_UPPER,
    NO_PREFERENCE
}
