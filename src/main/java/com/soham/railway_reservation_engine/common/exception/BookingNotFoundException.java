package com.soham.railway_reservation_engine.common.exception;



public class BookingNotFoundException
        extends ResourceNotFoundException {

    public BookingNotFoundException(String pnr) {

        super("Booking not found with PNR : " + pnr);

    }

}