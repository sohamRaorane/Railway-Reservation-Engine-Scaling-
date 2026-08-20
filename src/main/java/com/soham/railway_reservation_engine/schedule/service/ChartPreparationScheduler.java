package com.soham.railway_reservation_engine.schedule.service;


import ch.qos.logback.core.util.FixedDelay;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Periodic job that finalises charts for journeys whose departure has arrived.
 *
 * <p>{@code @Scheduled(fixedDelay = 60000)} runs this every 60 seconds on the application's
 * scheduler thread pool. It finds schedules that are still OPEN but whose journey date is
 * past (or today with departure time elapsed) and hands each to {@code ChartPreparationService}.
 * Per-schedule try/catch keeps one failing schedule from blocking the rest of the batch —
 * important because a scheduler crash mid-loop must not stall every chart.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChartPreparationScheduler {
    private final ScheduleRepository scheduleRepository;
    private final ChartPreparationService chartPreparationService;

    @Scheduled(fixedDelay = 60000) // Run every 60 seconds
    public void prepareDueCharts(){
        LocalDate  today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Schedule> dueSchedules = scheduleRepository.findDueOpenSchedules(
                com.soham.railway_reservation_engine.common.enums.ScheduleStatus.OPEN,
                today,
                now
        );

        for(Schedule schedule : dueSchedules){
            try{
                chartPreparationService.prepareChart(schedule.getId());
                log.info("Chart prepared automatically for scheduleId={}", schedule.getId());
            }catch(Exception ex){
                log.error(" Failed to prepare chart automatically for scheduleId={}" , schedule.getId() ,ex);

            }
        }

    }
}
