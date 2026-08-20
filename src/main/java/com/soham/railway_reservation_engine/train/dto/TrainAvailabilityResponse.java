package com.soham.railway_reservation_engine.train.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Availability summary for one train-date-quota combination, served from the Redis cache.
 *
 * <p>Counts are {@code Long} because they aggregate via SQL {@code SUM} over multiple coach
 * allocations; the JSON this serializes to is what the frontend renders in the seat-availability
 * UI.
 */
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
