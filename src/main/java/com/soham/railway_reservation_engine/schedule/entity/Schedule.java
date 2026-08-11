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