package com.soham.railway_reservation_engine.passenger.repository;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    List<Passenger> findByBooking(Booking booking);

    //For a particular schedule this query will return how many seats are already booked

    @Query("""
        select p.seat.id
        from Passenger p
        where p.booking.schedule = :schedule
        """)
    List<Long> findBookedSeatIdsByScheduleId(
            @Param("schedule") Schedule schedule
    );


}
