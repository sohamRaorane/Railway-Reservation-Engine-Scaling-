package com.soham.railway_reservation_engine.common.exception;

/**
 * Thrown when payment is attempted for a booking that cannot be paid yet —
 * e.g. the payment record is missing or the booking isn't in PENDING_PAYMENT.
 * Maps to HTTP 400.
 */
public class PaymentNotAllowedException
        extends RuntimeException {

    public PaymentNotAllowedException(String pnr) {

        super("Booking is not eligible for payment : " + pnr);

    }

}