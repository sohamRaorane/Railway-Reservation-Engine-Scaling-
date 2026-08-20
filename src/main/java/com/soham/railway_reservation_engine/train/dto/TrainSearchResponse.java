package com.soham.railway_reservation_engine.train.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Projection returned by the train-search endpoint: identity, journey date, and the boarding /
 * alighting times from the source and destination route entries. Immutable (all fields final via
 * the all-args constructor) — a DTO, never persisted.
 */
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
