package com.soham.railway_reservation_engine.train.entity;

import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.common.enums.TrainType;
import com.soham.railway_reservation_engine.route.entity.Route;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


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
