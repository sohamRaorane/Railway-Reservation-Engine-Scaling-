package com.soham.railway_reservation_engine.quotaReservationPool.entity;

import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


/**
 * Per train-date + quota counters for RAC and waitlist capacity — the row that serialises
 * fallback-seat allocation.
 *
 * <p><b>Why a separate pool instead of counters on Quota?</b> A quota's capacity is NOT static:
 * it varies per journey (date + train). This row is uniquely keyed by (schedule, quota) so each
 * train-date/quota combination has exactly one place where {@code racAvailable} and
 * {@code waitlistAvailable} are counted down/up.
 *
 * <p><b>Concurrency:</b> these counters are the classic <i>read-modify-write</i> hot spot. Every
 * concurrent booking would race on them, so callers always acquire a pessimistic row lock via
 * {@code QuotaReservationPoolRepository.findForUpdate} before touching the counters.
 * {@code racLimit}/{@code waitlistLimit} are the configured ceilings; available starts at the
 * limit and drains toward zero.
 */
@Entity
@Table(
        name = "quota_reservation_pool",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_qrp_schedule_quota",
                        columnNames = {
                                "schedule_id",
                                "quota_id"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaReservationPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quota_id", nullable = false)
    private Quota quota;

    @Column(name = "rac_limit", nullable = false)
    private Integer racLimit;

    @Column(name = "rac_available", nullable = false)
    private Integer racAvailable;

    @Column(name = "waitlist_limit", nullable = false)
    private Integer waitlistLimit;

    @Column(name = "waitlist_available", nullable = false)
    private Integer waitlistAvailable;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
