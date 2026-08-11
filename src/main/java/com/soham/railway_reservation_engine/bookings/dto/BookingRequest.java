package com.soham.railway_reservation_engine.bookings.dto;


import com.soham.railway_reservation_engine.common.enums.CoachType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

//record --> transparent carrier for immutable data
//also used to drastically reduce boilerplate code for data carrier classes
//like the getters and setters equals hashCode
/*
why curly bracs at the end
act as a standard java body
can use the body to add custom logic

 */
public record BookingRequest(
        @NotNull(message = "Train Id is required ")
        Long trainId,

        @NotNull(message ="Journey date is required ")
        @FutureOrPresent(message ="Journey date must be a future or present date")
        LocalDate journeyDate,

        @NotBlank(message = "Quota code is required ")
        String quotaCode,

        @NotNull(message = "Coach type is required ")
        CoachType coachType,

        @Valid
        @NotEmpty(message = "At least one passenger is required ")
        List<PassengerRequest> passengers
) {
}
