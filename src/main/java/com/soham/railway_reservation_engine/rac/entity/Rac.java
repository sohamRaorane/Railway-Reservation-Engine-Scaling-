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

/**
 * RAC (Reservation Against Cancellation) entry — a passenger granted a shared berth
 * while waiting for a full confirmed berth.
 *
 * <p><b>Terminology:</b> when confirmed seats are exhausted but RAC capacity remains, the passenger
 * boards the train anyway and is assigned a side-lower berth SHARED with another RAC passenger.
 * The {@code racNumber} is the queue position per schedule+quota (1, 2, 3...) and the unique
 * (schedule, racNumber) constraint guarantees numbering cannot collide across concurrent
 * allocations. The passenger-to-RAC link is one-to-one (a passenger can hold only one RAC slot).
 */
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
