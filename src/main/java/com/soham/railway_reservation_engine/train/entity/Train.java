package com.soham.railway_reservation_engine.train.entity;

import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.common.enums.TrainType;
import com.soham.railway_reservation_engine.route.entity.Route;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


/**
 * A train: the immutable reference record a journey is built around.
 *
 * <p>{@code number} is the public identifier (e.g. {@code 12951}) used across the API;
 * {@code type} distinguishes express/superfast/premium categories. The relationships fan out to
 * its coaches (physical layout), routes (static stops) and schedules (per-date journeys) — the
 * train itself carries no mutable reservation state.
 */
@Entity
@Table(
        name = "trains"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String number;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TrainType type;

    @OneToMany(mappedBy = "train", fetch = FetchType.LAZY)
    private List<Coach> coaches = new ArrayList<>();

    @OneToMany(mappedBy = "train", fetch = FetchType.LAZY)
    private List<Route> routes = new ArrayList<>();

    @OneToMany(mappedBy = "train", fetch = FetchType.LAZY)
    private List<Schedule> schedules = new ArrayList<>();
}
