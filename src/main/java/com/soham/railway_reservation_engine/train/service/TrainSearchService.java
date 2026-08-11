package com.soham.railway_reservation_engine.train.service;

import com.soham.railway_reservation_engine.schedule.repository.ScheduleRepository;
import com.soham.railway_reservation_engine.train.dto.TrainSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
