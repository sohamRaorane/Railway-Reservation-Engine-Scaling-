package com.soham.railway_reservation_engine.rac.repository;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.common.enums.RacStatus;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.rac.entity.Rac;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RacRepository extends JpaRepository<Rac, Long> {

    Optional<Rac> findByPassenger(Passenger passenger);

    List<Rac> findByScheduleOrderByRacNumberAsc(Schedule schedule);

    //for finding the top most one
    Optional<Rac> findTopByScheduleOrderByRacNumberDesc(
            Schedule schedule
    );
    Optional<Rac> findFirstByScheduleAndQuotaOrderByRacNumberAsc(
            Schedule schedule,
            Quota quota
    );

    Optional<Rac> findTopByScheduleAndQuotaOrderByRacNumberDesc(
            Schedule schedule,
            Quota quota
    );
    //for finding the bottom most one means with the least rac number hence we will require it status also
    Optional<Rac> findTopByScheduleAndStatusOrderByRacNumberAsc(
            Schedule schedule,
            RacStatus status
    );

}