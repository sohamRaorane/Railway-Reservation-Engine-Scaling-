package com.soham.railway_reservation_engine.seat.entity;

import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.common.enums.BerthType;
import com.soham.railway_reservation_engine.route.entity.Route;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A physical berth in a coach: identified by (coach, seatNumber) with a fixed {@code BerthType}.
 *
 * <p>Static inventory — the seat's <i>occupancy</i> for a given train-date is not stored here;
 * it's derived from passenger/booking state and the Redis hold, keeping this entity immutable
 * metadata. The berth type (LOWER, SIDE_LOWER, ...) is what RAC sharing and the berth-preference
 * logic reason about.
 */
@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "coach_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_seats_coach")
    )
    private Coach coach;

    @Column(nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BerthType berthType;


}
