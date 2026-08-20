package com.soham.railway_reservation_engine.common.exception;



/**
 * Raised when no booking exists for a given PNR (lookup or cancellation).
 * Subclass of {@code ResourceNotFoundException}; handled centrally → HTTP 404.
 */
public class BookingNotFoundException
        extends ResourceNotFoundException {

    public BookingNotFoundException(String pnr) {

        super("Booking not found with PNR : " + pnr);

    }

}