package com.soham.railway_reservation_engine.passenger.repository;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Data access for passengers.
 *
 * <p>{@code findBookedSeatIdsByScheduleId} answers "which physical seats are already occupied on
 * this train-date?" via a single JPQL projection (only the seat ids — no full entities), used by
 * {@code FirstAvailableSeatStrategy} to decide which seats to skip. Fetching only the ids keeps
 * the query light compared to loading entire {@code Passenger}/{@code Seat} entities.
 */
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
