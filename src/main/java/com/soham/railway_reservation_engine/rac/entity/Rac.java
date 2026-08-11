package com.soham.railway_reservation_engine.rac.entity;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.common.enums.RacStatus;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.seat.entity.Seat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "rac",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rac_schedule_number",
                        columnNames = {
                                "schedule_id",
                                "rac_number"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false, unique = true)
    private Passenger passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Column(name = "rac_number", nullable = false)
    private Integer racNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quota_id", nullable = false)
    private Quota quota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RacStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
