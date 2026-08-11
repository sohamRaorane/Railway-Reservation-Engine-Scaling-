package com.soham.railway_reservation_engine.waitlist.service;

import com.soham.railway_reservation_engine.common.enums.PassengerStatus;
import com.soham.railway_reservation_engine.common.enums.WaitlistStatus;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.passenger.repository.PassengerRepository;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quotaReservationPool.repository.QuotaReservationPoolRepository;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.waitlist.entity.Waitlist;
import com.soham.railway_reservation_engine.waitlist.repository.WaitlistRepository;
import jakarta.persistence.Persistence;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WaitlistService {
    private final WaitlistRepository waitlistRepository;
    private final PassengerRepository passengerRepository;
    private final QuotaReservationPoolRepository quotaReservationPoolRepository;


    /*
    Now the whole flow is like this
    transctional --> lock quota pool ---> find  max waitlist --> max+1 --> insert waitlist --> commit --> release lock
     */
    @Transactional // even if one thing fails do not commit to do the database just commit if everything is successful
    public Waitlist createWaitlistEntry(
            Schedule schedule,
            Quota quota,
            Passenger passenger
    ) {
        quotaReservationPoolRepository.findForUpdate(schedule, quota)
                .orElseThrow(() -> new RuntimeException("Quota Reservation Pool not found for given schedule and quota"));

        //find the nextWaitlist number
        Integer nextWaitlistNumber = waitlistRepository.findTopByScheduleAndQuotaOrderByWaitlistNumberDesc(
                schedule , quota
        ).map(waitlist -> waitlist.getWaitlistNumber() + 1)
                .orElse(1);

        passenger.setPassengerStatus(PassengerStatus.WAITLISTED);
        passengerRepository.save(passenger);
        Waitlist waitlist = Waitlist.builder()
                .schedule(schedule)
                .quota(quota)
                .passenger(passenger)
                .waitlistNumber(nextWaitlistNumber)
                .status(WaitlistStatus.ACTIVE)
                .build();

        return waitlistRepository.save(waitlist);



        //------------Design Issue -----------------
        /*
        //make the object of the Waitlist using the builder
        Waitlist waitlist = Waitlist.builder()
                .schedule(schedule)
                .quota(quota)
                .passenger(passenger)
                .waitlistNumber(nextWaitlistNumber)
                .status(WaitlistStatus.ACTIVE)
                .build();

        //Update the passenger status also
        passenger.setPassengerStatus(PassengerStatus.WAITLISTED);
        //so this passenger is still a transient entity --> an object that you
        //have created using the  new keyword in java but it has no relationship with the database yet
        //The 3 Core Characteristics of a Transient EntityNo Database Row: There is no matching record for this object in your database tables.
        // No Database Identifier: The primary key field (like @Id Long id) is usually null
        // .Ignored by Hibernate: The JPA Persistence Context (Hibernate) does not know this object exists. If you change a property on it, nothing happens in the database.
        passengerRepository.save(passenger);
        return waitlistRepository.save(waitlist);
         */

        //-------------- --------------------------------


        //Just the return the waiter object
        /*
        return Waitlist.builder()
                .schedule(schedule)
                .quota(quota)
                .passenger(passenger)
                .waitlistNumber(nextWaitlistNumber)
                .status(WaitlistStatus.ACTIVE)
                .build();
         */

    }
}
