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

@Service
@RequiredArgsConstructor
public class ChartPreparationService {
    private final ScheduleRepository scheduleRepository;
    private final QuotaRepository quotaRepository;
    private final RacRepository racRepository;
    private final WaitlistRepository waitlistRepository;
    private final WaitlistPromotionService waitlistPromotionService;
    private  final QuotaSeatAllocationRepository    quotaSeatAllocationRepository;



    @Transactional
    public void prepareChart(Long scheduleId){

        //find the schedule
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found with  id " + scheduleId));

        //so the railway chart can be only prepared once
        if(schedule.getStatus() != ScheduleStatus.OPEN){
            throw new IllegalStateException("Schedule status is not open . current status is " + schedule.getStatus());

        }

        //freeze the new bookings
        schedule.setStatus(ScheduleStatus.CLOSED);

        //process every quota
        List<Quota> quotas = quotaRepository.findAll();
        for(Quota quota : quotas){
            processQuota(schedule , quota);
        }
        //so after processing all the quota now the chart preparation is completed
        schedule.setStatus(ScheduleStatus.CHART_PREPARED);
        schedule.setChartPreparedAt(LocalDateTime.now());

        //saving explictly because it shows that we follow a specific pattern
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
        return quotaSeatAllocationRepository.findByScheduleAndQuota(schedule, quota)
                .stream()
                .mapToInt(allocation -> allocation.getAvailableSeats())
                .sum();
    }
}
