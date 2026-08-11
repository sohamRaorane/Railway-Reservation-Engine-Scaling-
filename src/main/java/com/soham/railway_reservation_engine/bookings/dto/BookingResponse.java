package com.soham.railway_reservation_engine.bookings.dto;


import com.soham.railway_reservation_engine.common.enums.BookingStatus;

import java.math.BigDecimal;
import java.util.List;

public record BookingResponse(

        String pnr,

        BookingStatus bookingStatus,

        BigDecimal totalFare,

        List<PassengerResponse> passengers

) {}