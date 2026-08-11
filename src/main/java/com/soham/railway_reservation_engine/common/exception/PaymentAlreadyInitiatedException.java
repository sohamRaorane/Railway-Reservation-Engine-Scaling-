package com.soham.railway_reservation_engine.common.exception;

public class PaymentAlreadyInitiatedException
        extends RuntimeException {

    public PaymentAlreadyInitiatedException(String pnr) {

        super("Payment has already been initiated for booking : " + pnr);

    }

}
