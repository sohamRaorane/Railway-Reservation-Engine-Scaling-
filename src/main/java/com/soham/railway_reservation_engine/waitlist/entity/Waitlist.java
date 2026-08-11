package com.soham.railway_reservation_engine.waitlist.entity;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.common.enums.WaitlistStatus;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "waitlist",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_waitlist_schedule_quota_number",
                        columnNames = {
                                "schedule_id",
                                "quota_id",
                                "waitlist_number"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Waitlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quota_id", nullable = false)
    private Quota quota;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false, unique = true)
    private Passenger passenger;

    @Column(name = "waitlist_number", nullable = false)
    private Integer waitlistNumber;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitlistStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
