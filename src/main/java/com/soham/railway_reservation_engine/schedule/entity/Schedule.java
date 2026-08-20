package com.soham.railway_reservation_engine.schedule.entity;


import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.common.enums.ScheduleStatus;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.rac.entity.Rac;
import com.soham.railway_reservation_engine.train.entity.Train;
import com.soham.railway_reservation_engine.waitlist.entity.Waitlist;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A specific journey: one train on one date. This is the anchor of all reservation state.
 *
 * <p><b>Terminology:</b> the {@code Route} entity describes a train's stops in the abstract; a
 * {@code Schedule} instantiates the train for a concrete {@code journeyDate}. Every booking,
 * quota allocation, waitlist and RAC entry hangs off a schedule, and availability answers are
 * always per schedule (same train, different date = different pools).
 *
 * <p>{@code status} gates the lifecycle: OPEN (accepting bookings) → CHART_PREPARING →
 * CHART_PREPARED (no more bookings; the reservation chart is final).
 */
@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "train_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_schedules_train")
    )
    private Train train;

    @Column(nullable = false)
    private LocalDate journeyDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status;

    private LocalDateTime chartPreparedAt;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY)
    private List<QuotaSeatAllocation> quotaSeatAllocations = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY)
    private List<Waitlist> waitlists = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY)
    private List<Rac> racEntries = new ArrayList<>();

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;
}