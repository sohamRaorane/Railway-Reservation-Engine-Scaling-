package com.soham.railway_reservation_engine.schedule.controller;

import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.schedule.service.ChartPreparationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/schedules")
public class ScheduleController {

    private final ChartPreparationService chartPreparationService;
    @PostMapping("/{scheduleId}/prepare-chart")
    public ResponseEntity<String> prepareChart(
            @PathVariable Long scheduleId
    ){
        chartPreparationService.prepareChart(scheduleId);
        return ResponseEntity.ok("Chart prepared successfully" + scheduleId);

    }

}
