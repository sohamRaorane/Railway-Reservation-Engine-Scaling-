package com.soham.railway_reservation_engine.train.controller;

import com.soham.railway_reservation_engine.train.dto.TrainAvailabilityResponse;
import com.soham.railway_reservation_engine.train.service.TrainAvailabilityService;
import com.soham.railway_reservation_engine.train.service.TrainSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/trains")
@RequiredArgsConstructor
public class TrainAvailabilityController {

    private final TrainAvailabilityService service;

     @GetMapping("/{trainId}/availability")
     public TrainAvailabilityResponse getTrainAvailability(
            @PathVariable Long trainId,
             @RequestParam LocalDate date,
             @RequestParam String quota

     ) {
         return service.getAvailability(trainId, date, quota);
     }
}
