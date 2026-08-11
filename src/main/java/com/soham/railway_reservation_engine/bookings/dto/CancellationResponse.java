package com.soham.railway_reservation_engine.bookings.dto;

import com.soham.railway_reservation_engine.common.enums.BookingStatus;

import java.math.BigDecimal;

public record CancellationResponse (
        String pnr,

        BookingStatus bookingStatus,

        BigDecimal refundAmount,

        String message

){

}
