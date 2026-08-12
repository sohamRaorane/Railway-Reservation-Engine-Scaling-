package com.soham.railway_reservation_engine.rac.service;


import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quotaReservationPool.entity.QuotaReservationPool;
import com.soham.railway_reservation_engine.quotaReservationPool.repository.QuotaReservationPoolRepository;
import com.soham.railway_reservation_engine.rac.entity.Rac;
import com.soham.railway_reservation_engine.rac.repository.RacRepository;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RacService {
    private final RacRepository racRepository;
    //private final QuotaReservationPoolRepository quotaReservationPoolRepository;
    public Rac createRacEntry(Schedule schedule , Quota quota, Passenger passenger){
        Integer nextRacNumber = racRepository
                .findTopByScheduleAndQuotaOrderByRacNumberDesc(schedule, quota)
                .map(rac -> rac.getRacNumber() + 1)
                .orElse(1);

        return Rac.builder()
                .schedule(schedule)
                .quota(quota)
                .passenger(passenger)
                .racNumber(nextRacNumber)
                .status(com.soham.railway_reservation_engine.common.enums.RacStatus.ACTIVE)
                .build();

    }
}
