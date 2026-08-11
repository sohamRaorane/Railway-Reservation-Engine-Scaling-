package com.soham.railway_reservation_engine.route.entity;

import com.soham.railway_reservation_engine.station.entity.Station;
import com.soham.railway_reservation_engine.train.entity.Train;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalTime;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//so what does it represents --> it represents one stop of one train
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id" , nullable = false , foreignKey = @ForeignKey(name = "fk_route_train"))
    private Train train;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id" , nullable = false , foreignKey = @ForeignKey(name = "fk_route_station"))
    private Station station;


    @Column(nullable = false)
    private Integer sequenceNo;

    private LocalTime arrivalTime;
    private LocalTime departureTime;

    @Column(nullable = false)
    private Integer distanceKm;



}
