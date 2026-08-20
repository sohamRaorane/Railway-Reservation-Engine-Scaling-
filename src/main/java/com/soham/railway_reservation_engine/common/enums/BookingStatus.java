package com.soham.railway_reservation_engine.common.enums;

/**
 * Lifecycle states of a booking (PNR-level status).
 *
 * <p>PENDING_PAYMENT is the initial state after seat allocation but before the Razorpay
 * webhook confirms payment; the waitlist/RAC states mean at least one passenger is
 * not yet confirmed. Legal transitions are enforced by {@code PnrStateMachine}.
 */
public enum BookingStatus {
    CONFIRMED,
    RAC,
    WAITLIST,
    CANCELLED,
    PENDING_PAYMENT

}
