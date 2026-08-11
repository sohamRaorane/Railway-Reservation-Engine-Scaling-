package com.soham.railway_reservation_engine.bookings.entity;


import com.soham.railway_reservation_engine.common.enums.BookingStatus;
import com.soham.railway_reservation_engine.common.enums.CoachType;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.payment.entity.Payment;
import com.soham.railway_reservation_engine.pnrStateHistory.entity.PnrStateHistory;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cglib.core.Local;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table( name= "bookings" )
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true , length = 15)
    private String pnr;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quota_id", nullable = false)
    private Quota quota;


    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus;


    @Column(name = "total_fare", nullable = false)
    private BigDecimal totalFare;

    @Enumerated(EnumType.STRING)
    private CoachType coachType;



    @Column(name = "idempotency_key" , unique = true)
    private String idempotencyKey;


    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY)
    private List<Passenger> passengers = new ArrayList<>();

    @OneToOne(mappedBy = "booking", fetch = FetchType.LAZY)
    private Payment payment;

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY)
    private List<PnrStateHistory> pnrStateHistories = new ArrayList<>();


}
