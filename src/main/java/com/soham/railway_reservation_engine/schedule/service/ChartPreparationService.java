package com.soham.railway_reservation_engine.schedule.service;

import com.soham.railway_reservation_engine.common.enums.ScheduleStatus;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quota.repository.QuotaRepository;
import com.soham.railway_reservation_engine.quotaSeatAllocation.repository.QuotaSeatAllocationRepository;
import com.soham.railway_reservation_engine.rac.entity.Rac;
import com.soham.railway_reservation_engine.rac.repository.RacRepository;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.schedule.repository.ScheduleRepository;
import com.soham.railway_reservation_engine.waitlist.entity.Waitlist;
import com.soham.railway_reservation_engine.waitlist.repository.WaitlistRepository;
import com.soham.railway_reservation_engine.waitlist.service.WaitlistPromotionService;
import com.soham.railway_reservation_engine.waitlist.service.WaitlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Finalises a journey's reservation chart: promotes every waiting passenger into freed capacity.
 *
 * <p><b>Terminology — "chart":</b> the final seat-assignment list that decides who actually
 * boards. Before the chart, statuses are provisional; chart preparation locks them in.
 *
 * <p><b>Concurrency design (single-winner):</b> instead of read-then-check, preparation first runs
 * the atomic OPEN → CHART_PREPARING update and inspects the affected-row count. If 0, another
 * scheduler instance already claimed this schedule (or it is already CHART_PREPARED — in which
 * case we return quietly). Only the winner proceeds to promotions, so multiple scheduler replicas
 * can never double-process a schedule.
 *
 * <p><b>Promotion order per quota:</b>
 * <ol>
 *   <li>RAC → CONFIRMED first (senior RAC numbers first), while confirmed seats remain.</li>
 *   <li>Then WAITLIST → CONFIRMED, also capacity-gated.</li>
 * </ol>
 * Each promotion is done via {@code WaitlistPromotionService}; availability is recomputed per
 * iteration so the loop stops exactly when the seats run out.
 */
@Service
@RequiredArgsConstructor
public class ChartPreparationService {
    private final ScheduleRepository scheduleRepository;
    private final QuotaRepository quotaRepository;
    private final RacRepository racRepository;
    private final WaitlistRepository waitlistRepository;
    private final WaitlistPromotionService waitlistPromotionService;
    private  final QuotaSeatAllocationRepository    quotaSeatAllocationRepository;


    /*
    so now we no longer check open by reading first
    so we first do an atomic open -> chart preparing update and check the number of rows updated
    if the update count is 0 another run already prepared or grabbed it
    if the chart is already prepared second run returns quietly
    and at last the promotions only happens if this run successfully got the lock
    to avoid the conflict of  multiple schedulers
     */
    @Transactional
   public void prepareChart(Long scheduleId){
        int lockedRows = scheduleRepository.markChartPreparing(
                scheduleId,
                ScheduleStatus.OPEN,
                ScheduleStatus.CHART_PREPARING
        );
        if(lockedRows == 0 ){
            Schedule exsistingSchedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + scheduleId));

            if(exsistingSchedule.getStatus() == ScheduleStatus.CHART_PREPARED){
                return;
            }
            throw  new IllegalStateException("Schedule is not open for chart prepartion. Current status: " + exsistingSchedule.getStatus());
        }
        Schedule schedule =  scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + scheduleId));

        List<Quota> quotas = quotaRepository.findAll();
        for(Quota quota : quotas){
            processQuota(schedule , quota);
        }

        schedule.setStatus(ScheduleStatus.CHART_PREPARED);
        schedule.setChartPreparedAt(LocalDateTime.now());
        scheduleRepository.save(schedule);

    }

    private void processQuota(Schedule schedule, Quota quota) {
        // so if the passengers are the RAC passengers
        /*
        promote passenger {
        RAC --> Confirm
        if WL then wl to rac
        }
         */
        while (true) {

            Rac rac = racRepository
                    .findFirstByScheduleAndQuotaOrderByRacNumberAsc(
                            schedule,
                            quota
                    )
                    .orElse(null);

            if (rac == null) {
                break;
            }

            int availableSeats = getAvailableSeats(
                    schedule,
                    quota
            );

            if (availableSeats <= 0) {
                break;
            }

            waitlistPromotionService.promotePassenger(
                    schedule,
                    quota
            );
        }
        //Once no rac remains , promote remaining Wl passenegr according to the avaiable capacity
        while(true ){
            Waitlist waitlist = waitlistRepository.findFirstByScheduleAndQuotaOrderByWaitlistNumberAsc(schedule, quota)
                    .orElse(null);
            if(waitlist == null){
                break;

            }
            int availableSeats = getAvailableSeats(schedule, quota);
            if(availableSeats <= 0){
                break;
            }
            waitlistPromotionService.promoteWaitlistToConfirmed(schedule, quota);

        }

    }

    private int getAvailableSeats(Schedule schedule, Quota quota){
        Long total = quotaSeatAllocationRepository.getTotalAvailableSeats(
                schedule.getTrain().getId(),
                schedule.getJourneyDate(),
                quota.getCode()
        );
        return total != null ? total.intValue() : 0;
    }
}
