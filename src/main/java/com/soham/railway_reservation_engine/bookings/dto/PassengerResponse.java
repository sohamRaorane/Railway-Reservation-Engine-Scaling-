package com.soham.railway_reservation_engine.bookings.dto;


import com.soham.railway_reservation_engine.common.enums.BerthType;
import com.soham.railway_reservation_engine.common.enums.PassengerStatus;

public record PassengerResponse(

        String name,

        String coachNumber,

        Integer seatNumber,

        BerthType berthType,

        PassengerStatus passengerStatus

) {}