package com.soham.railway_reservation_engine.quotaSeatAllocation.entity;

import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Availability of a quota's berths on ONE coach of ONE train-date (schedule).
 *
 * <p><b>Modelling:</b> seats are scarce per coach, and each coach's seats are partitioned among
 * quotas — so availability is tracked at the (schedule × coach × quota) grain. This is exactly the
 * granularity {@code FirstAvailableSeatStrategy} needs: it scans coaches of the requested type,
 * reads each coach's {@code availableSeats} for the quota, and picks a free seat inside.
 *
 * <p>Follows the <b>Single Responsibility Principle</b>: this row owns <i>confirmed-seat</i>
 * counts only. RAC and waitlist capacity live in {@code QuotaReservationPool}, keeping each
 * counter in exactly one place.
 */
@Entity
@Table(
        name = "quota_seat_allocations"

)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class QuotaSeatAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quota_id", nullable = false)
    private Quota quota;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;


}
