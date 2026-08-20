
package com.soham.railway_reservation_engine.quotaSeatAllocation.repository;


import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Data access for per-coach quota availability.
 *
 * <p>{@code findByScheduleAndCoachAndQuota} locates the counter the seat strategy decrements when
 * confirming a passenger; {@code getTotalAvailableSeats} aggregates availability across all coaches
 * of a train-date for the availability response (COALESCE keeps the sum at 0 instead of null when
 * no rows exist). The commented-out block shows an earlier design that returned a DTO directly
 * from the query — replaced by the leaner aggregate below.
 */
public interface QuotaSeatAllocationRepository
        extends JpaRepository<QuotaSeatAllocation, Long> {

    Optional<QuotaSeatAllocation> findByScheduleAndQuota(
            Schedule schedule,
            Quota quota
    );
    Optional<QuotaSeatAllocation> findByScheduleAndCoachAndQuota(
            Schedule scheduleId,
             Coach coach,
            Quota quota
    );
/*
    @Query("""
     SELECT new com.soham.railway_reservation_engine.train.dto.TrainAvailabilityResponse(
             s.train.number,
             s.train.name,
             s.journeyDate,
             q.code,
             SUM(qsa.availableSeats),
             SUM(qsa.racAvailable),
             SUM(qsa.waitlistAvailable)
         )
         FROM QuotaSeatAllocation qsa
         JOIN qsa.schedule s
         JOIN qsa.quota q
         
         WHERE s.train.id = :trainId AND
             s.journeyDate = :journeyDate AND
             q.code = :quota
            GROUP BY s.train.number, s.train.name, s.journeyDate, q.code
""")
    Optional<TrainAvailabilityResponse> findAvailability(
            @Param("trainId") Long trainId,
            @Param("journeyDate") LocalDate journeyDate,
            @Param("quota") String quota
    );



 */
@Query("""
SELECT COALESCE(SUM(qsa.availableSeats), 0)
FROM QuotaSeatAllocation qsa
JOIN qsa.schedule s
JOIN qsa.quota q
WHERE s.train.id = :trainId
AND s.journeyDate = :journeyDate
AND q.code = :quota
""")
Long getTotalAvailableSeats(
        @Param("trainId") Long trainId,
        @Param("journeyDate") LocalDate journeyDate,
        @Param("quota") String quota
);
}

