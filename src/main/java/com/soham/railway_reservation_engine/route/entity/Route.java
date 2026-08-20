package com.soham.railway_reservation_engine.route.entity;

import com.soham.railway_reservation_engine.station.entity.Station;
import com.soham.railway_reservation_engine.train.entity.Train;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalTime;

/**
 * One stop of one train: the (train, station, sequenceNo) triple describes where a train
 * halts, in what order, at what times, and how far along the journey that stop is.
 *
 * <p>This is static schedule metadata (independent of any travel date); the per-DATE journey is a
 * {@code Schedule}. {@code sequenceNo} orders the stops (1 = origin, N = destination);
 * {@code distanceKm} is cumulative, so the fare for a boarding/alighting pair is the difference
 * between the two stops' distances.
 */
@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
