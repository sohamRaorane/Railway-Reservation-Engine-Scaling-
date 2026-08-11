package com.soham.railway_reservation_engine.quotaReservationPool.repository;

import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quotaReservationPool.entity.QuotaReservationPool;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface QuotaReservationPoolRepository extends JpaRepository<QuotaReservationPool, Long> {

    Optional<QuotaReservationPool> findByScheduleAndQuota(Schedule schedule, Quota quota);
    @Query("""
SELECT qrp
FROM QuotaReservationPool qrp
JOIN qrp.schedule s
JOIN qrp.quota q
WHERE s.train.id = :trainId
AND s.journeyDate = :journeyDate
AND q.code = :quota
""")
    Optional<QuotaReservationPool> findPool(
            @Param("trainId") Long trainId,
            @Param("journeyDate") LocalDate journeyDate,
            @Param("quota") String quota
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
    select q 
    from QuotaReservationPool q
    where q.schedule = :schedule
    and q.quota = :quota
"""
    )
    Optional<QuotaReservationPool> findForUpdate(
            @Param("schedule") Schedule schedule,
            @Param("quota") Quota quota
    );
}
