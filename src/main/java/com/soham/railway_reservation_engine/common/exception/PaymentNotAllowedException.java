package com.soham.railway_reservation_engine.common.exception;

public class PaymentNotAllowedException
        extends RuntimeException {

    public PaymentNotAllowedException(String pnr) {

        super("Booking is not eligible for payment : " + pnr);

    }

}