package com.soham.railway_reservation_engine.schedule.controller;

import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.schedule.service.ChartPreparationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP trigger for chart preparation ({@code POST /api/v1/schedules/{id}/prepare-chart}).
 *
 * <p>Primarily for manual/administrative use — the automatic path runs via
 * {@code ChartPreparationScheduler}. The endpoint delegates to the same single-winner CAS logic,
 * so triggering it manually while the scheduler is running is safe.
 */
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
