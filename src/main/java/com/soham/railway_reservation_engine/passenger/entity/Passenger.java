package com.soham.railway_reservation_engine.passenger.entity;



import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.common.enums.BerthPreference;
import com.soham.railway_reservation_engine.common.enums.BerthType;
import com.soham.railway_reservation_engine.common.enums.Gender;
import com.soham.railway_reservation_engine.common.enums.PassengerStatus;
import com.soham.railway_reservation_engine.seat.entity.Seat;
import jakarta.persistence.*;
import lombok.*;


/**
 * One traveller within a booking — the unit of seat allocation.
 *
 * <p><b>Key modelling decision:</b> a booking has many passengers, and each passenger carries its
 * own {@code passengerStatus} (CONFIRMED / RAC / WAITLISTED / CANCELLED). This lets a single PNR
 * hold a mix of states (e.g. two confirmed, one waitlisted), mirroring real Indian Railways.
 *
 * <p><b>Lazy loading:</b> {@code booking} and {@code seat} are {@code FetchType.LAZY} — loaded only
 * when accessed, avoiding the N+1 cartesian blowup of eagerly fetching whole object graphs.
 * {@code seat} is null until a passenger is allocated one (nullability itself encodes state).
 * {@code berthPreference} is what the user asked for; the allocated berth lives on {@code Seat}.
 */
@Entity
@Table(name = "passengers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "berth_preference")
    private BerthPreference berthPreference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_status", nullable = false)
    private PassengerStatus passengerStatus;


    public PassengerStatus getStatus() {
        return passengerStatus;
    }
}
