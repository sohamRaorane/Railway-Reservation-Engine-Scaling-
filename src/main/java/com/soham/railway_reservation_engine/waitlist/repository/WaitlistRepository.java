package com.soham.railway_reservation_engine.waitlist.repository;
import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.common.enums.WaitlistStatus;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    Optional<Waitlist> findByPassenger(Passenger passenger);

    List<Waitlist> findByScheduleAndQuotaOrderByWaitlistNumberAsc(
            Schedule schedule,
            Quota quota
    );

    /*
    Prevents Query Exceptions:Without Top, your method would look like findByScheduleAndQuota.... If the database finds multiple records matching that schedule and quota
    , Spring Data will throw a IncorrectResultSizeDataAccessException because it expects only one result.
     Top tells the repository that it is okay if multiple rows exist; just fetch the first one.
     Works Hand-in-Hand with "OrderBy":Top is almost always paired with OrderBy to give it predictable meaning.Top + OrderBy...Desc = Gives you the highest value (the maximum).
     Top + OrderBy...Asc = Gives you the lowest value (the minimum).
     Performance Optimization:Instead of pulling thousands of waitlist records into application memory and sorting them in Java,
      Top forces the database to stop searching the moment it finds the single best match.
     */
    Optional<Waitlist> findTopByScheduleAndQuotaOrderByWaitlistNumberDesc(
            Schedule schedule,
            Quota quota
    );

    Optional<Waitlist> findFirstByScheduleAndQuotaOrderByWaitlistNumberAsc(
            Schedule schedule,
            Quota quota
    );

    Optional<Waitlist> findTopByScheduleAndQuotaAndStatusOrderByWaitlistNumberAsc(
            Schedule schedule,
            Quota quota,
            WaitlistStatus status
    );



}