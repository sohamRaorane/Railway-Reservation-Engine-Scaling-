package com.soham.railway_reservation_engine.train.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class TrainSearchResponse {
    //so these are the things that would be returned in the response of the train search api
    private String trainNumber;
    private String trainName;
    private LocalDate journeyDate;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
}
