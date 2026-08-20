package com.soham.railway_reservation_engine.common.enums;

/**
 * State of an individual waitlist entry. Entries are created ACTIVE and either get promoted
 * (to RAC, freeing their waitlist slot) or cancelled when the booking fails payment.
 */
public enum WaitlistStatus {
    ACTIVE,
    PROMOTED_TO_RAC,
    CANCELLED
}
