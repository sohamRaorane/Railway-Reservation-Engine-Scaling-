package com.soham.railway_reservation_engine.train.service;

import com.soham.railway_reservation_engine.schedule.repository.ScheduleRepository;
import com.soham.railway_reservation_engine.train.dto.TrainSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Facade over the train-search query — separates the HTTP layer from the (fairly involved)
 * JPQL that joins schedules, trains and two route stops. The query logic itself lives in
 * {@code ScheduleRepository.searchTrains}; this thin service exists so controllers depend on a
 * named service and the query stays reusable and testable.
 */
@Service
@RequiredArgsConstructor
public class TrainSearchService {
    private final ScheduleRepository scheduleRepository;
    public List<TrainSearchResponse> searchTrains(
            String source,
            String destination,
            LocalDate journeyDate
    ) {

        return scheduleRepository.searchTrains(
                source,
                destination,
                journeyDate
        );
    }
}
