package com.soham.railway_reservation_engine.quotaReservationPool.entity;

import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


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
