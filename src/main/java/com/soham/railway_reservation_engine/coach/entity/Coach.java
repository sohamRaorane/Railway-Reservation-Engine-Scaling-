package com.soham.railway_reservation_engine.coach.entity;

import com.soham.railway_reservation_engine.common.enums.CoachType;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.seat.entity.Seat;
import com.soham.railway_reservation_engine.train.entity.Train;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "coaches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "train_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_coaches_train")
    )
    private Train  train;

    @Column(nullable = false , length = 20)
    private String coachNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoachType coachType;


    @Column(nullable = false)
    //here int not used since int can be 0 --> valid numeric value
    //Integer --> gives us null -> means not set which helps us to understand that whether the seat is been
    //selectd or not
    private Integer totalSeats;

    @OneToMany(mappedBy = "coach", fetch = FetchType.LAZY)
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "coach", fetch = FetchType.LAZY)
    private List<QuotaSeatAllocation> quotaSeatAllocations = new ArrayList<>();
}
