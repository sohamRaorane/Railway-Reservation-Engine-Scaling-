package com.soham.railway_reservation_engine.common.enums;

/**
 * State machine of a payment record: PENDING once the Razorpay order is created, then
 * SUCCESS or FAILED based on the verified webhook. REFUNDED applies when a cancellation
 * returns money. Success is what flips a booking to CONFIRMED.
 */
public enum PaymentStatus {

    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}
