package com.soham.railway_reservation_engine.common.exception;

/**
 * Thrown when a client tries to initiate a payment twice for the same booking.
 * The payment row is created once with PENDING status; a second attempt is a
 * duplicate → HTTP 409 CONFLICT.
 */
public class PaymentAlreadyInitiatedException
        extends RuntimeException {

    public PaymentAlreadyInitiatedException(String pnr) {

        super("Payment has already been initiated for booking : " + pnr);

    }

}
