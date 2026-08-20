package com.soham.railway_reservation_engine.station.entity;

import com.soham.railway_reservation_engine.route.entity.Route;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


/**
 * A railway station referenced by train routes.
 *
 * <p>{@code code} is the canonical 3-letter station code used in search queries (e.g. {@code NDLS},
 * {@code BCT}); the unique constraint keeps lookups by code unambiguous. Static reference data —
 * a station exists independently of any train.
 */
@Entity
@Table(
        name = "stations"

)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String zone;

    @OneToMany(mappedBy = "station", fetch = FetchType.LAZY)
    private List<Route> routes = new ArrayList<>();

}
