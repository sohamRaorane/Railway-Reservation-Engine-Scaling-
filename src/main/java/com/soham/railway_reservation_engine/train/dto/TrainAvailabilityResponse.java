package com.soham.railway_reservation_engine.train.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor

public class TrainAvailabilityResponse {
    String trainNumber;
    String trainName;
    LocalDate journeyDate;
    String quota;

    //Long since it will be a summation
    Long totalAvailableSeats;
    Long totalRacAvailable;
    Long totalWaitlistSeats;
}
