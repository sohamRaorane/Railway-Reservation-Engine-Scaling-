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
