package com.soham.railway_reservation_engine.common.enums;

/**
 * Know-Your-Customer verification state for a user account — the placeholder for a
 * government-ID verification workflow. New users start PENDING.
 */
public enum KycStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
