package com.soham.railway_reservation_engine.train.controller;

import com.soham.railway_reservation_engine.train.dto.TrainSearchResponse;
//import com.soham.railway_reservation_engine.train.service.TrainAvailabilityService;
import com.soham.railway_reservation_engine.train.service.TrainSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * HTTP layer for train search: {@code GET /api/v1/trains/search?source=..&destination=..&date=..}.
 *
 * <p>{@code @DateTimeFormat(ISO.DATE)} tells Spring how to parse the {@code date} query parameter
 * (yyyy-MM-dd) into a {@code LocalDate} — without it Spring's default string binding would fail
 * or misinterpret the format.
 */
@RestController
@RequestMapping("/api/v1/trains")
@RequiredArgsConstructor
public class TrainSearchController {
    private final TrainSearchService trainSearchService;




    @GetMapping("/search")
    public List<TrainSearchResponse> searchTrains(

            @RequestParam String source,

            @RequestParam String destination,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {

        return trainSearchService.searchTrains(
                source,
                destination,
                date
        );
    }
}
