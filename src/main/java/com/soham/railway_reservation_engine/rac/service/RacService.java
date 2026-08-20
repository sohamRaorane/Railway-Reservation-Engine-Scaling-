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

/**
 * Creates RAC queue entries.
 *
 * <p>The next {@code racNumber} is derived from the current highest entry for the
 * schedule+quota (max + 1, starting at 1) rather than a shared counter — but because the
 * (schedule, rac_number) column pair is unique, a concurrent duplicate insert fails at the DB.
 * The {@code QuotaReservationPoolRepository} dependency is commented out: capacity gating happens
 * in the booking flow before this service is reached, so the service itself stays a pure factory.
 */
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
