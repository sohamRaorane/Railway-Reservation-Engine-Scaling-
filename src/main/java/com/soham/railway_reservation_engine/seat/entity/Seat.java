package com.soham.railway_reservation_engine.seat.entity;

import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.common.enums.BerthType;
import com.soham.railway_reservation_engine.route.entity.Route;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
