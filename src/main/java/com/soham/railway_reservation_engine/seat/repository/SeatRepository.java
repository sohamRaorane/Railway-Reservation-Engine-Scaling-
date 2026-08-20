package com.soham.railway_reservation_engine.seat.repository;

import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.seat.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Data access for seats.
 *
 * <p>{@code findByIdForUpdate} is the row-lock used by {@code FirstAvailableSeatStrategy}: the
 * {@code PESSIMISTIC_WRITE} lock makes concurrent transactions block on the same seat until the
 * holder commits, so the seat's final check (is it booked? is it held in Redis?) and its
 * allocation happen atomically with respect to other bookers.
 */
public interface SeatRepository extends JpaRepository<Seat,Long> {
    List<Seat> findByCoachOrderBySeatNumberAsc(Coach coach);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select s
    from Seat s
    where s.id = :seatId
""")
    Optional<Seat> findByIdForUpdate(
            @Param("seatId") Long seatId
    );

    //Give me the list of all the coaches
   // List<Seat> findByCoach(Coach coach);
}
